package com.yashoid.network;

import com.yashoid.office.AsyncOperation;
import com.yashoid.office.task.TaskManager;

public class OperationExecutor implements Runnable, OperationTypes {
	
	protected interface OnExecutionFinishedListener {
		
		void onExecutionFinished(NetworkOperation operation);
		
	}
	
	protected abstract static class OperationTask extends AsyncOperation {

		public OperationTask(TaskManager taskManager, String foregroundSection, String backgroundSection) {
			super(taskManager, foregroundSection, backgroundSection);
		}

		protected void publishProgress(Object object) {
			publishProgress(new Object[] { object });
		}
		
	}

	private TaskManager mTaskManager;

	private NetworkOperation mOperation;
	private OnExecutionFinishedListener mOnExecutionFinishedListener;
	
	protected OperationExecutor(TaskManager taskManager, NetworkOperation operation, OnExecutionFinishedListener listener) {
		mTaskManager = taskManager;

		mOperation = operation;
		mOnExecutionFinishedListener = listener;
	}
	
	@Override
	public void run() {
		new OperationTask(mTaskManager, mOperation.getForegroundSection(), getSectionForOperationType(mOperation.getType())) {

			@Override
			protected void doInBackground() {
				mOperation.execute(this);

				if (mOnExecutionFinishedListener!=null) {
					mOnExecutionFinishedListener.onExecutionFinished(mOperation);
				}
			}
			
			@Override
			protected void onProgressUpdate(Object value) {
				mOperation.onProgressUpdate(value);
			};
			
			@Override
			protected void onPostExecute() {
				mOperation.onOperationFinished();
			};
			
		}.execute();
	}

	private String getSectionForOperationType(int type) {
		switch (type) {
			case TYPE_UI_CONTENT:
				return SECTION_UI_CONTENT;
			case TYPE_USER_ACTION:
				return SECTION_USER_ACTION;
			case TYPE_BACKGRUOND:
				return SECTION_BACKGROUND;
			case TYPE_URGENT:
				return TaskManager.IMMEDIATELY;
		}

		throw new IllegalArgumentException("Unrecognized operation type " + type + ".");
	}

}
