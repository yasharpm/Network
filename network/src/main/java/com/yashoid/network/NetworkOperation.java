package com.yashoid.network;

import com.yashoid.network.OperationExecutor.OperationTask;

public abstract class NetworkOperation {

	public static final int PRIORITY_DEFAULT = 0;
	public static final int PRIORITY_HIGH = 1;
	public static final int PRIORITY_LOW = -1;
	public static final int PRIORITY_MAX = 2;
	public static final int PRIORITY_MIN = -2;
	
	private int mType;
	private int mPriority;
	
	private OperationTask mTask;
	
	protected NetworkOperation(int type) {
		mType = type;
		mPriority = PRIORITY_DEFAULT;
	}
	
	protected NetworkOperation(int type, int priority) {
		mType = type;
		mPriority = priority;
	}
	
	protected final int getType() {
		return mType;
	}
	
	protected void setPriority(int priority) {
		mPriority = priority;
	}
	
	protected int getPriority() {
		return mPriority;
	}
	
	protected final void execute(OperationTask task) {
		mTask = task;
		
		operate();
	}
	
	/**
	 * This method is called inside a Thread.
	 */
	abstract protected void operate();
	
	protected final void publishProgress(Object object) {
		mTask.publishProgress(object);
	}
	
	protected void onProgressUpdate(Object object) {
		
	}
	
	/**
	 * This method is called on the main Thread.
	 */
	protected void onOperationFinished() {
		
	}
	
}
