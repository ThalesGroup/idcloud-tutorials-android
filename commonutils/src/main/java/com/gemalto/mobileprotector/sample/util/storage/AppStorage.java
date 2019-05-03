/*
 * MIT License
 *
 * Copyright (c) 2019 Thales DIS
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

package com.gemalto.mobileprotector.sample.util.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Application storage.
 */
public class AppStorage {
    /**
     * Gets the client id.
     *
     * @param context
     *         Android application context.
     * @return User id, or {@code null} if client id does not exist.
     */
    public static String getUserId(final Context context) {
        final SharedPreferences sharedPref = context.getSharedPreferences("AppStorage", Context.MODE_PRIVATE);
        return sharedPref.getString("UserId", null);
    }

    /**
     * Stores the client id.
     *
     * @param context
     *         Android application context.
     * @param userId
     *         User id.
     */
    public static void saveUserId(final Context context, final String userId) {
        final SharedPreferences sharedPref = context.getSharedPreferences("AppStorage", Context.MODE_PRIVATE);
        sharedPref.edit().putString("UserId", userId).apply();
    }

}
