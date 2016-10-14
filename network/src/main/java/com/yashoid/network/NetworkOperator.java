package com.yashoid.network;

import com.yashoid.network.OperationExecutor.OnExecutionFinishedListener;
import android.os.Handler;

public class NetworkOperator {

	public static final int TYPE_URGENT = 0;
	public static final int TYPE_UI_CONTENT = 1;
	public static final int TYPE_USER_ACTION = 2;
	public static final int TYPE_BACKGRUOND = 4;
	
	private static final int TYPE_ALL = 7;
	
	private static NetworkOperator mInstance = null;
	
	public static NetworkOperator getInstance() {
		if (mInstance==null) {
			mInstance = new NetworkOperator();
		}
		
		return mInstance;
	}
	
	private OperationSelector mSelector;
	
	private Handler mHandler;
	
	private int mTypesInProgress = 0;
	
	private Object mProgressStatusLock = new Object();
	
	private NetworkOperator() {
		mSelector = new OperationSelector();
		
		mHandler = new Handler();
	}
	
	/**
	 * It is safe to call this method from any Thread.
	 * @param operation
	 */
	public void post(NetworkOperation operation) {
		if (operation.getType()!=TYPE_URGENT) {
			mSelector.addOperation(operation);
			
			executeNextOperation();
		}
		else {
			mHandler.post(new OperationExecutor(operation, null));
		}
	}
	
	private void executeNextOperation() {
		synchronized (mProgressStatusLock) {
			if (mTypesInProgress==0) {
				executeNextOperation(TYPE_ALL);
			}
			else if ((mTypesInProgress&TYPE_UI_CONTENT)>0) {
				return;
			} else if (mSelector.hasNext(TYPE_UI_CONTENT)) {
				executeNextOperation(TYPE_UI_CONTENT);
			} else if ((mTypesInProgress&TYPE_USER_ACTION)>0) {
				return;
			} else if (mSelector.hasNext(TYPE_USER_ACTION)) {
				executeNextOperation(TYPE_USER_ACTION);
			} else if ((mTypesInProgress&TYPE_BACKGRUOND)>0) {
				return;
			} else if (mSelector.hasNext(TYPE_BACKGRUOND)) {
				executeNextOperation(TYPE_BACKGRUOND);
			}
		}
	}
	
	/** Must be called while having mProgressStatusLock lock.
	 * @param types
	 */
	private void executeNextOperation(int types) {
		NetworkOperation operation = mSelector.getNextOperation(types);
		
		if (operation!=null) {
			mSelector.remove(operation);
			
			mTypesInProgress |= operation.getType();
			
			mHandler.post(new OperationExecutor(operation, mOnExecutionFinishedListener));
		}
	}
	
	private OnExecutionFinishedListener mOnExecutionFinishedListener = new OnExecutionFinishedListener() {
		
		@Override
		public void onExecutionFinished(NetworkOperation operation) {
			if (operation.getType()!=TYPE_URGENT) {
				synchronized (mProgressStatusLock) {
					mTypesInProgress = mTypesInProgress&(~operation.getType());
				}
			}

			executeNextOperation();
		}
		
	};
	
}
