package com.yashoid.network;

import android.os.AsyncTask;

public class OperationExecutor implements Runnable {
	
	protected static interface OnExecutionFinishedListener {
		
		public void onExecutionFinished(NetworkOperation operation);
		
	}
	
	protected abstract static class OperationTask extends AsyncTask<Void, Object, Void> {

		protected void publishProgress(Object object) {
			publishProgress(new Object[] { object });
		}
		
	}
	
	private NetworkOperation mOperation;
	private OnExecutionFinishedListener mOnExecutionFinishedListener;
	
	protected OperationExecutor(NetworkOperation operation, OnExecutionFinishedListener listener) {
		mOperation = operation;
		mOnExecutionFinishedListener = listener;
	}
	
	@Override
	public void run() {
		new OperationTask() {
			
			@Override
			protected Void doInBackground(Void... params) {
				mOperation.execute(this);
				
				if (mOnExecutionFinishedListener!=null) {
					mOnExecutionFinishedListener.onExecutionFinished(mOperation);
				}
				
				return null;
			}
			
			@Override
			protected void onProgressUpdate(Object... values) {
				mOperation.onProgressUpdate(values[0]);
			};
			
			@Override
			protected void onPostExecute(Void result) {
				mOperation.onOperationFinished();
			};
			
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
	}

}
