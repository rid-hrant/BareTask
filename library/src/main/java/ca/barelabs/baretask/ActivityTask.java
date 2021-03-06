package ca.barelabs.baretask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

public abstract class ActivityTask<Params, Progress, Result> {

    public static final int DEFAULT_TEST_DURATION = 5000;

    interface OnTaskUpdatedListener {
        void onTaskUpdated(long id);
    }

    private final Context mContext;
    private final InternalAsyncTask mInternalTask;
    private long mCallbackId;
    private long mTaskId;
    private OnTaskUpdatedListener mTaskListener;
    private Progress[] mProgress;
    private Result mResult;
    private Exception mException;
    private boolean mProgressPublished;
    private boolean mResultCompleted;
    private boolean mResultDelivered;

    public ActivityTask(Context context) {
        mContext = context.getApplicationContext();
        mInternalTask = new InternalAsyncTask();
    }

    public Context getContext() {
        return mContext;
    }

    public long getId() {
        return mCallbackId;
    }

    public boolean isSuccessful() {
        return mResultCompleted && mException == null;
    }

    public Exception getException() {
        return mException;
    }

    @SuppressWarnings("unchecked")
    public void execute(Params... params) {
        if (isStarted()) {
            throw new IllegalStateException("An ActivityTask can only be executed once!");
        }
        if (Build.VERSION.SDK_INT >= 11) {
            mInternalTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            mInternalTask.execute(params);
        }
    }

    @SuppressWarnings("unchecked")
    public abstract Result doInBackground(Params... params) throws Exception;

    public abstract void onCancelled(Result value);

    protected boolean isCancelled() {
        return mInternalTask.isCancelled();
    }

    @SuppressWarnings("unchecked")
    protected void publishProgress(Progress... progress) {
        mInternalTask.publishInternalProgress(progress);
    }

    protected void cancel(boolean mayInterruptIfRunning) {
        mInternalTask.cancel(mayInterruptIfRunning);
    }

    long getCallbackId() {
        return mCallbackId;
    }

    void setCallbackId(long id) {
        mCallbackId = id;
    }

    long getTaskId() {
        return mTaskId;
    }

    void registerListener(long taskId, OnTaskUpdatedListener taskListener) {
        mTaskId = taskId;
        mTaskListener = taskListener;
    }

    void unregisterListener() {
        mTaskListener = null;
    }

    boolean isStarted() {
        return mInternalTask.getStatus() != AsyncTask.Status.PENDING;
    }

    boolean isPendingResult() {
        return isStarted() && !mResultDelivered && !isCancelled();
    }

    void setResultDelivered() {
        mResultDelivered = true;
    }

    boolean isProgressPublished() {
        return mProgressPublished;
    }

    Progress[] getProgress() {
        return mProgress;
    }

    boolean isResultCompleted() {
        return mResultCompleted;
    }

    Result getResult() {
        return mResult;
    }

    @SuppressWarnings("unchecked")
    private void onTaskProgress(Progress... values) {
        mProgressPublished = true;
        mProgress = values;
        notifyListener();
    }

    private void onTaskComplete(Result result, Exception exception) {
        mResultCompleted = true;
        mResult = result;
        mException = exception;
        notifyListener();
    }

    private void onTaskCancelled(Result result) {
        mResultDelivered = true;
        onCancelled(result);
        notifyListener();
    }

    private void notifyListener() {
        if(mTaskListener != null) {
            mTaskListener.onTaskUpdated(mTaskId);
        }
    }


    private class InternalAsyncTask extends AsyncTask<Params, Progress, Result> {

        private Exception mException;

        @SuppressWarnings("unchecked")
        @Override
        protected Result doInBackground(Params... params) {
            try {
                return ActivityTask.this.doInBackground(params);
            } catch(Exception e) {
                mException = e;
                return null;
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onProgressUpdate(Progress... values) {
            ActivityTask.this.onTaskProgress(values);
        }

        @Override
        protected void onPostExecute(Result value) {
            ActivityTask.this.onTaskComplete(value, mException);
        }

        @Override
        protected void onCancelled(Result value) {
            ActivityTask.this.onTaskCancelled(value);
        }

        @SuppressWarnings("unchecked")
        protected void publishInternalProgress(Progress... value) {
            publishProgress(value);
        }
    }
}