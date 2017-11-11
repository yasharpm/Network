package com.yashoid.network;

import com.yashoid.network.OperationExecutor.OperationTask;
import com.yashoid.office.task.TaskManager;

public abstract class NetworkOperation implements OperationTypes {
	
	private int mType;
	private int mPriority;
	private String mForegroundSection;
	
	private OperationTask mTask;
	
	protected NetworkOperation(int type) {
		this(type, PRIORITY_DEFAULT);
	}
	
	protected NetworkOperation(int type, int priority) {
		this(type, priority, TaskManager.MAIN);
	}

	protected NetworkOperation(int type, int priority, String foregroundSection) {
		mType = type;
		mPriority = priority;
		mForegroundSection = foregroundSection;
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

	protected final String getForegroundSection() {
		return mForegroundSection;
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
