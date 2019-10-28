package com.yashoid.network;

import com.yashoid.office.office.Office;
import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;
import com.yashoid.yashson.Yashson;

import java.util.ArrayList;
import java.util.List;

public class NetworkOperator implements Priorities {

    private static final int DEFAULT_HIGH_PRIORITY_WORKER_COUNT = 3;
    private static final int DEFAULT_MEDIUM_PRIORITY_WORKER_COUNT = 4;
    private static final int DEFAULT_LOW_PRIORITY_WORKER_COUNT = 2;

    private static final int DEFAULT_CONNECT_TIMEOUT = 4_000;
    private static final int DEFAULT_READ_TIMEOUT = 8_000;

    private static final String[] PRIORITIES = { HIGH, MEDIUM, LOW };

    public static class Builder {

        private Yashson mYashson = null;
        private RequestHeaderWriter mRequestHeaderWriter = null;
        private TaskManager mTaskManager = null;
        private int mHighPriorityWorkerCount = DEFAULT_HIGH_PRIORITY_WORKER_COUNT;
        private int mMediumPriorityWorkerCount = DEFAULT_MEDIUM_PRIORITY_WORKER_COUNT;
        private int mLowPriorityWorkerCount = DEFAULT_LOW_PRIORITY_WORKER_COUNT;
        private int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int mReadTimeout = DEFAULT_READ_TIMEOUT;

        public Builder yashson(Yashson yashson) {
            mYashson = yashson;

            return this;
        }

        public Builder requsetHeaderWriter(RequestHeaderWriter headerWriter) {
            mRequestHeaderWriter = headerWriter;

            return this;
        }

        public Builder taskManager(TaskManager taskManager) {
            mTaskManager = taskManager;

            return this;
        }

        public Builder highPriorityWorkerCount(int count) {
            mHighPriorityWorkerCount = count;

            return this;
        }

        public Builder mediumPriorityWorkerCount(int count) {
            mMediumPriorityWorkerCount = count;

            return this;
        }

        public Builder lowPriorityWorkerCount(int count) {
            mLowPriorityWorkerCount = count;

            return this;
        }

        public Builder connectTimeout(int timeout) {
            mConnectTimeout = timeout;

            return this;
        }

        public Builder readTimeout(int timeout) {
            mReadTimeout = timeout;

            return this;
        }

        public NetworkOperator build() {
            Yashson yashson = mYashson == null ? new Yashson() : mYashson;
            RequestHeaderWriter headerWriter = mRequestHeaderWriter;
            TaskManager taskManager = mTaskManager == null ? DefaultTaskManager.getInstance() : mTaskManager;

            boolean hasPrioritySections = false;

            for (Office.SectionDescription sectionDescription: taskManager.getSectionDescriptions()) {
                if (HIGH.equals(sectionDescription.name)) {
                    hasPrioritySections = true;
                    break;
                }
            }

            if (!hasPrioritySections) {
                taskManager.addSection(HIGH, mHighPriorityWorkerCount);
                taskManager.addSection(MEDIUM, mMediumPriorityWorkerCount);
                taskManager.addSection(LOW, mLowPriorityWorkerCount);
            }

            return new NetworkOperator(taskManager, headerWriter, yashson, mConnectTimeout, mReadTimeout);
        }

    }

    private TaskManager mTaskManager;

    private int[] mWorkerCounts = new int[PRIORITIES.length];

    private RequestHandler mRequestHandler;

    private RequestHeaderWriter mRequestHeaderWriter;

    private Yashson mYashson;

    private int mConnectTimeout;
    private int mReadTimeout;

    private NetworkOperator(TaskManager taskManager, RequestHeaderWriter requestHeaderWriter,
                            Yashson yashson, int connectTimeout, int readTimeout) {
        mTaskManager = taskManager;

        mRequestHeaderWriter = requestHeaderWriter;

        mYashson = yashson;

        for (Office.SectionDescription sectionDescription : taskManager.getSectionDescriptions()) {
            for (int i = 0; i < mWorkerCounts.length; i++) {
                if (PRIORITIES[i].equals(sectionDescription.name)) {
                    mWorkerCounts[i] = sectionDescription.employeeCount;
                }
            }
        }

        mRequestHandler = new RequestHandler();

        mConnectTimeout = connectTimeout;
        mReadTimeout = readTimeout;
    }

    protected int getConnectTimeout() {
        return mConnectTimeout;
    }

    protected int getReadTimeout() {
        return mReadTimeout;
    }

    public<T> void runRequest(PreparedRequest<T> request, String priority, RequestResponseCallback<T> callback) {
        PendingRequest pendingRequest = new PendingRequest<>(request, callback);

        mRequestHandler.onNewRequest(priority, pendingRequest);
    }

    public<T> NetworkRequest<T> getRequest(Class<T> returnTypeClass, String method, String url) {
        return getRequest(returnTypeClass, method, url, null);
    }

    public<T> NetworkRequest<T> getRequest(Class<T> returnTypeClass, String method, String url,
                                           Object body, String... headers) {
        return new NetworkRequest<>(this, returnTypeClass, method, url, body,
                mRequestHeaderWriter, headers);
    }

    public Yashson getYashson() {
        return mYashson;
    }

    private class RequestHandler {

        private RequestSequence[] mSequences = new RequestSequence[PRIORITIES.length];

        protected RequestHandler() {
            for (int i = 0; i < PRIORITIES.length; i++) {
                RequestSequence sequence = new RequestSequence(PRIORITIES[i], mWorkerCounts[i]);

                mSequences[i] = sequence;

                if (i > 0) {
                    sequence.setPriorSequence(mSequences[i - 1]);

                    mSequences[i - 1].setNextSequence(sequence);
                }
            }
        }

        public<T> void onNewRequest(String priority, PendingRequest<T> request) {
            mSequences[getPriorityIndex(priority)].onNewRequest(request);
        }

    }

    private class RequestSequence {

        private RequestSequence mPriorSequence = null;
        private RequestSequence mNextSequence = null;

        private String mSectionName;

        private int mWorkerCount;

        private List<PendingRequest> mWaitingList = new ArrayList<>();
        private List<PendingRequest> mWorkingList = new ArrayList<>(3);

        protected RequestSequence(String sectionName, int workerCount) {
            mSectionName = sectionName;

            mWorkerCount = workerCount;
        }

        public void setPriorSequence(RequestSequence sequence) {
            mPriorSequence = sequence;
        }

        public void setNextSequence(RequestSequence sequence) {
            mNextSequence = sequence;
        }

        public<T> void onNewRequest(PendingRequest<T> request) {
            if (mPriorSequence != null && mPriorSequence.hasWorkToDo()) {
                mWaitingList.add(request);
                return;
            }

            if (mWorkingList.size() < mWorkerCount) {
                runRequest(request);
            }
            else {
                mWaitingList.add(request);
            }
        }

        private boolean hasWorkToDo() {
            return !mWorkingList.isEmpty() || !mWaitingList.isEmpty() || (mPriorSequence != null && mPriorSequence.hasWorkToDo());
        }

        private void startWorking() {
            if (mWaitingList.isEmpty()) {
                if (mWorkingList.isEmpty()) {
                    if (mNextSequence != null) {
                        mNextSequence.startWorking();
                    }
                    else {
                        // All is done.
                    }
                }
                else {
                    // We have nothing waiting. So just wait until everything is done.
                }
            }
            else {
                while (mWorkingList.size() < mWorkerCount && !mWaitingList.isEmpty()) {
                    runRequest(mWaitingList.get(0));
                }
            }
        }

        private<T> void runRequest(final PendingRequest<T> request) {
            mWaitingList.remove(request);
            mWorkingList.add(request);

            mTaskManager.runTask(mSectionName, new Runnable() {

                @Override
                public void run() {
                    final RequestResponse<T> response = request.request.call();

                    mTaskManager.runTask(TaskManager.MAIN, new Runnable() {

                        @Override
                        public void run() {
                            mWorkingList.remove(request);

                            if (mPriorSequence != null && mPriorSequence.hasWorkToDo()) {
                                // Don't try starting any new tasks.
                            }
                            else {
                                if (!mWaitingList.isEmpty()) {
                                    runRequest(mWaitingList.get(0));
                                }
                                else if (mWorkingList.isEmpty()) {
                                    if (mNextSequence != null) {
                                        mNextSequence.startWorking();
                                    }
                                    else {
                                        // All is done.
                                    }
                                }
                                else {
                                    // Other work is being done. We wait until everything is done.
                                }
                            }

                            request.callback.onRequestResponse(response);
                        }

                    }, 0);
                }

            }, 0);
        }

    }

    private class PendingRequest<T> {

        private final PreparedRequest<T> request;
        private final RequestResponseCallback<T> callback;

        public PendingRequest(PreparedRequest<T> request, RequestResponseCallback<T> callback) {
            this.request = request;
            this.callback = callback;
        }

    }

    private static int getPriorityIndex(String priority) {
        switch (priority) {
            case HIGH:
                return 0;
            case MEDIUM:
                return 1;
            case LOW:
                return 2;
        }

        throw new IllegalArgumentException("Unrecognized priority name '" + priority + "'.");
    }

}
