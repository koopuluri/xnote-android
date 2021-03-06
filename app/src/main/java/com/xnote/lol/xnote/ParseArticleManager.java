package com.xnote.lol.xnote;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.xnote.lol.xnote.runnables.ParseArticleRunnable;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * adapted from: https://developer.android.com/training/multiple-threads/create-threadpool.html
 * and: http://codetheory.in/android-java-executor-framework/
 * Created by koopuluri on 3/11/15.
 */
public class ParseArticleManager {
    public static final String TAG = "ParseArticleManager";
    static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    static final int TASK_COMPLETED = 233;
    static final int TASK_NOT_COMPLETED = 232;
    static ParseArticleManager sInstance;
    static ThreadPoolExecutor mParseArticleThreadPool;
    Handler mHandler;
    private static BlockingQueue<Runnable> mWorkQueue = null;

    static {
        // create single instance of ParseArticleManager
        sInstance = new ParseArticleManager();
    }

    public static ParseArticleManager getInstance() {
        return sInstance;
    }

    static public ParseArticleTask startParsing(String articleId, ParseCallback callback) {
        ParseArticleTask pTask = new ParseArticleTask(articleId, callback);
        sInstance.mParseArticleThreadPool
                .execute(new ParseArticleRunnable(pTask));
        return pTask;
    }

    /**
     * Interrupts all running threads.
     */
    public static void cancelAll() {
        /*
         * Creates an array of Runnables that's the same size as the
         * thread pool work queue
         */
        ParseArticleRunnable[] runnableArray =
                new ParseArticleRunnable[mParseArticleThreadPool.getQueue().size()];
        mWorkQueue.toArray(runnableArray);
        int len = runnableArray.length;
        /*
         * Iterates over the array of Runnables and interrupts each one's Thread.
         */
        synchronized (sInstance) {
            // Iterates over the array of tasks
            for (int runnableIndex = 0; runnableIndex < len; runnableIndex++) {
                // Gets the current thread
                Thread thread = runnableArray[runnableIndex].getThread();
                // if the Thread exists, post an interrupt to it
                if (null != thread) {
                    thread.interrupt();
                }
            }
        }
    }


    // private constructor: this enforces singleton behavior, therefore eliminating the need to
    // enclose the accesses to class in 'synchronized' block.
    private ParseArticleManager() {
        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {
                ParseArticleTask parseTask = (ParseArticleTask) inputMessage.obj;
                switch (inputMessage.what) {
                    case TASK_COMPLETED:
                        parseTask.onArticleParsed();
                    case TASK_NOT_COMPLETED:
                        parseTask.onArticleFailed();
                    default:
                        super.handleMessage(inputMessage);
                }
            }
        };

        mWorkQueue = new LinkedBlockingQueue<Runnable>();
        // initializing the ThreadPoolExecutor:
        mParseArticleThreadPool = new ThreadPoolExecutor(
                NUMBER_OF_CORES*2,
                NUMBER_OF_CORES*2,
                60L,
                TimeUnit.SECONDS,
                mWorkQueue
        );

    }

    public void handleState(ParseArticleTask parseArticleTask, int state) {
        switch (state) {
            case TASK_COMPLETED:
                /**
                 * Create message for Handler with the state and the task object:
                 */
                Message completeMessage =
                        mHandler.obtainMessage(state, parseArticleTask);
                completeMessage.sendToTarget();  // I wonder what this does!
                break;
            case TASK_NOT_COMPLETED:
                Message incompleteMessage = mHandler.obtainMessage(state, parseArticleTask);
                incompleteMessage.sendToTarget(); //I wonder what this does too!
        }
    }
}