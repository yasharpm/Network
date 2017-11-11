package com.yashoid.network;

import com.yashoid.network.OperationExecutor.OnExecutionFinishedListener;
import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;

import android.os.Handler;

public class NetworkOperator implements OperationTypes {
	
	private static final int TYPE_ALL = 7;

	private static final int DEFAULT_UI_CONTENT_WORKERS = 5;
	private static final int DEFAULT_USER_ACTION_WORKERS = 1;
	private static final int DEFAULT_BACKGROUND_WORKERS = 2;

	private static NetworkOperator mInstance = null;
	
	public static NetworkOperator getInstance() {
		if (mInstance == null) {
			mInstance = new NetworkOperator(DefaultTaskManager.getInstance());
		}
		
		return mInstance;
	}

	private TaskManager mTaskManager;

	private OperationSelector mSelector;

	private int mTasksOnUiContent = 0;
	private int mTasksOnUserAction = 0;
	private int mTasksOnBackground = 0;
	
	private Object mProgressStatusLock = new Object();
	
	public NetworkOperator(TaskManager taskManager) {
		mTaskManager = taskManager;

		mTaskManager.addSection(SECTION_UI_CONTENT, DEFAULT_UI_CONTENT_WORKERS);
		mTaskManager.addSection(SECTION_USER_ACTION, DEFAULT_USER_ACTION_WORKERS);
		mTaskManager.addSection(SECTION_BACKGROUND, DEFAULT_BACKGROUND_WORKERS);

		mSelector = new OperationSelector();
	}
	
	/**
	 * It is safe to call this method from any Thread.
	 * @param operation
	 */
	public void post(NetworkOperation operation) {
		if (operation.getType() != TYPE_URGENT) {
			mSelector.addOperation(operation);
			
			executeNextOperation();
		}
		else {
			new OperationExecutor(mTaskManager, operation, null).run();
		}
	}
	
	private void executeNextOperation() {
		synchronized (mProgressStatusLock) {
			if (!hasAnyWorkerWorking()) {
				executeNextOperation(TYPE_ALL);
			}
			else if (mTasksOnUiContent >= DEFAULT_UI_CONTENT_WORKERS) {
				return;
			}
			else if (mSelector.hasNext(TYPE_UI_CONTENT)) {
				executeNextOperation(TYPE_UI_CONTENT);
			}
			else if (mTasksOnUserAction >= DEFAULT_USER_ACTION_WORKERS) {
				return;
			}
			else if (mSelector.hasNext(TYPE_USER_ACTION)) {
				executeNextOperation(TYPE_USER_ACTION);
			}
			else if (mTasksOnBackground >= DEFAULT_BACKGROUND_WORKERS) {
				return;
			}
			else if (mSelector.hasNext(TYPE_BACKGRUOND)) {
				executeNextOperation(TYPE_BACKGRUOND);
			}
		}
	}

	private boolean hasAnyWorkerWorking() {
		return mTasksOnUiContent > 0 || mTasksOnUserAction > 0 || mTasksOnBackground > 0;
	}
	
	/** Must be called while having mProgressStatusLock lock.
	 * @param types
	 */
	private void executeNextOperation(int types) {
		NetworkOperation operation = mSelector.getNextOperation(types);
		
		if (operation != null) {
			mSelector.remove(operation);

			addToWorkersCount(operation.getType());

			new OperationExecutor(mTaskManager, operation, mOnExecutionFinishedListener).run();
		}
	}
	
	private OnExecutionFinishedListener mOnExecutionFinishedListener = new OnExecutionFinishedListener() {
		
		@Override
		public void onExecutionFinished(NetworkOperation operation) {
			if (operation.getType() != TYPE_URGENT) {
				synchronized (mProgressStatusLock) {
					removeFromWorkersCount(operation.getType());
				}
			}

			executeNextOperation();
		}
		
	};

	private void addToWorkersCount(int type) {
		switch (type) {
			case TYPE_UI_CONTENT:
				mTasksOnUiContent++;
				return;
			case TYPE_USER_ACTION:
				mTasksOnUserAction++;
				return;
			case TYPE_BACKGRUOND:
				mTasksOnBackground++;
				return;
		}
	}

	private void removeFromWorkersCount(int type) {
		switch (type) {
			case TYPE_UI_CONTENT:
				mTasksOnUiContent--;
				return;
			case TYPE_USER_ACTION:
				mTasksOnUserAction--;
				return;
			case TYPE_BACKGRUOND:
				mTasksOnBackground--;
				return;
		}
	}
	
}
