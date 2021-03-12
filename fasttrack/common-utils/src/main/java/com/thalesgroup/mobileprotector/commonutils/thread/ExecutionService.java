/*
 * MIT License
 *
 * Copyright (c) 2020 Thales DIS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * IMPORTANT: This source code is intended to serve training information purposes only.
 *            Please make sure to review our IdCloud documentation, including security guidelines.
 */

package com.thalesgroup.mobileprotector.commonutils.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Execution service used for thread execution and to execute callbacks on the main thread.
 */
public final class ExecutionService {
    private static ExecutionService sExecutionService;

    private final ThreadPoolExecutor mExecutor;
    private final Handler mHandler;

    /**
     * Creates a new {@code ExecutionService} instance.
     */
    private ExecutionService() {
        mExecutor = new ThreadPoolExecutor(1, 1, 0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()) {
            @Override
            protected void afterExecute(Runnable runnable, Throwable throwable) {
                if (throwable != null) {
                    // Original executor implementation hides uncaught exception
                    // Which is undesired for us as it would also hide the potential issues
                    throw new RuntimeException(throwable); // NOPMD
                }
            }
        };

        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Gets singleton the instance of {@code ExecutionService}.
     *
     * @return Singleton the instance of {@code ExecutionService}.
     */
    public static ExecutionService getExecutionService() {
        synchronized (ExecutionService.class) {
            if (sExecutionService == null) {
                sExecutionService = new ExecutionService();
            }
        }

        return sExecutionService;
    }

    /**
     * Executes a {@code Runnable} on a background thread, all runnbles are queued.
     *
     * @param runnable {@code Runnable} to run on background thread.
     */
    public void runOnBackgroundThread(Runnable runnable) {
        mExecutor.execute(runnable);
    }

    /**
     * Executes a {@code Runnable} on the main UI thread, all runnbles are queued.
     *
     * @param runnable {@code Runnable} to run on main UI thread.
     */
    public void runOnMainUiThread(Runnable runnable) {
        mHandler.post(runnable);
    }
}
