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

package com.thalesgroup.mobileprotector.commonutils.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import androidx.annotation.Nullable;

public class BaseLogic {

    private static WeakReference<Context> mContext;

    public static void setContext(Context context) {
        mContext = new WeakReference<>(context);
    }

    /**
     * Common helper to get translated string.
     *
     * @param resId Resource id of desired string.
     * @return String value.
     */
    protected static String getString(int resId) {
        // Try to get currently stored context.
        if (mContext == null)
            throw new IllegalStateException();

        Context context = mContext.get();
        if (context == null)
            throw new IllegalStateException();

        // Return translation.
        return context.getString(resId);
    }

    protected static @Nullable
    Context getContext() {
        return mContext != null ? mContext.get() : null;
    }

    protected static void logError(Exception exception) {
        Context context = getContext();
        if (context == null) return;

        Toast.makeText(context.getApplicationContext(),
                exception.getCause() + " : " + exception.getMessage(),
                Toast.LENGTH_LONG).show();
    }

    //region Storage helper
    private static final String STORAGE = "STORAGE";

    /**
     * Retrieves shared preferences.
     *
     * @return Shared preferences.
     */
    protected static SharedPreferences getSharedPrefs() {
        Context context = getContext();
        return (context != null ? context.getSharedPreferences(STORAGE, Context.MODE_PRIVATE) : null);
    }
    //endregion
}
