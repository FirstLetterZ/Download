
package com.zpf.download;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;
import android.util.SparseArray;


import com.zpf.util.DownloadInfoManager;

import static com.zpf.download.Constants.TAG;

/**
 * Service that hosts download jobs. Each active download job is handled as a
 * unique {@link DownloadThread} instance.
 * <p>
 * The majority of downloads should have ETag values to enable resuming, so if a
 * given download isn't able to finish in the normal job timeout (10 minutes),
 * we just reschedule the job and resume again in the future.
 */
public class DownloadJobService extends JobService {
    // @GuardedBy("mActiveThreads")
    private SparseArray<Thread> mActiveThreads = new SparseArray<>();

    @Override
    public void onCreate() {
        super.onCreate();

        // While someone is bound to us, watch for database changes that should
        // trigger notification updates.
//        getContentResolver().registerContentObserver(ALL_DOWNLOADS_CONTENT_URI, true, mObserver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        final int id = params.getJobId();

        // Spin up thread to handle this download
        final DownloadInfo info = DownloadInfo.queryDownloadInfo(this, id);
        if (info == null) {
            Log.w(TAG, "Odd, no details found for download " + id);
            return false;
        }

        final Thread thread;
        synchronized (mActiveThreads) {
            if (mActiveThreads.indexOfKey(id) >= 0) {
                Log.w(TAG, "Odd, already running download " + id);
                return false;
            }
            if (info.mMiui) {
                thread = new MiDownloadThread(this, params, info);
            } else {
                thread = new DownloadThread(this, params, info);
            }
            mActiveThreads.put(id, thread);
        }
        Log.w(TAG, "onStartJob==>" + id + "; use miui download thread : " + info.mMiui);
        thread.start();

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        final int id = params.getJobId();
//        Log.d(TAG, "onStopJob id=" + id + ", reason=" + params.getDebugStopReason());

        final Thread thread;
        synchronized (mActiveThreads) {
            thread = mActiveThreads.get(id);
            mActiveThreads.delete(id);
        }
        Log.w("DOWNLOAD_TAG", "onStopJob==>" + id + "; thread != null :" + (thread != null));

        if (thread != null) {
            // If the thread is still running, ask it to gracefully shutdown,
            // and reschedule ourselves to resume in the future.
            if (thread instanceof IShutdown) {
                ((IShutdown) thread).requestShutdown();
            }

            Helpers.scheduleJob(this, DownloadInfo.queryDownloadInfo(this, id));
        }
        return false;
    }

    public void jobFinishedInternal(JobParameters params, boolean needsReschedule) {
        final int id = params.getJobId();

        synchronized (mActiveThreads) {
            mActiveThreads.remove(params.getJobId());
        }
        Log.w("DOWNLOAD_TAG", "jobFinishedInternal==>" + needsReschedule);
        if (needsReschedule) {
            Helpers.scheduleJob(this, DownloadInfo.queryDownloadInfo(this, id));
        } else {
            DownloadInfoManager.INSTANCE.onFinish(id);
        }

        // Update notifications one last time while job is protecting us
//        mObserver.onChange(false);

        // We do our own rescheduling above
        jobFinished(params, false);
    }

//    private ContentObserver mObserver = new ContentObserver(Helpers.getAsyncHandler()) {
//        @Override
//        public void onChange(boolean selfChange) {
//            Helpers.getDownloadNotifier(DownloadJobService.this).update();
//        }
//    };
}
