package com.yashoid.network;

import java.util.ArrayList;

public class OperationSelector {

	private ArrayList<NetworkOperation> mOperations;
	
	private Object lock = new Object();
	
	protected OperationSelector() {
		mOperations = new ArrayList<>(10);
	}
	
	protected void addOperation(NetworkOperation operation) {
		synchronized (lock) {
			mOperations.add(operation);
		}
	}
	
	protected boolean hasNext(int types) {
		synchronized (lock) {
			for (NetworkOperation operation: mOperations) {
				if ((operation.getType() & types) > 0) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	protected NetworkOperation getNextOperation(int types) {
		synchronized (lock) {
			NetworkOperation priorityOperation = null;
			
			for (NetworkOperation operation: mOperations) {
				if ((operation.getType() & types) > 0) {
					if (priorityOperation == null) {
						priorityOperation = operation;
					}
					else if (operation.getPriority() > priorityOperation.getPriority()) {
						priorityOperation = operation;
					}
				}
			}
			
			return priorityOperation;
		}
	}
	
	protected void remove(NetworkOperation operation) {
		synchronized (lock) {
			mOperations.remove(operation);
		}
	}
	
}
