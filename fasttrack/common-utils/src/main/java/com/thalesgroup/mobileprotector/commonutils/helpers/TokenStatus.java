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

public class TokenStatus {
    // Whenever is System Touch ID supported by device and SDK
    private boolean mTouchSupported;
    // Whenever is Mobile Protector Face ID supported by device and SDK
    private boolean mProtectorFaceSupported;
    // Whenever is System Touch ID supported and enabled
    private boolean mTouchEnabled;
    // Whenever is Mobile Protector Face ID supported and enabled
    private boolean mProtectorFaceEnabled;

    public boolean isTouchSupported() {
        return mTouchSupported;
    }

    public void setTouchSupported(final boolean value) {
        mTouchSupported = value;
    }

    public boolean isProtectorFaceSupported() {
        return mProtectorFaceSupported;
    }

    public void setProtectorFaceSupported(final boolean value) {
        mProtectorFaceSupported = value;
    }

    public boolean isTouchEnabled() {
        return mTouchEnabled;
    }

    public void setTouchEnabled(final boolean value) {
        mTouchEnabled = value;
    }

    public boolean isProtectorFaceEnabled() {
        return mProtectorFaceEnabled;
    }

    public void setProtectorFaceEnabled(final boolean protectorFaceEnabled) {
        mProtectorFaceEnabled = protectorFaceEnabled;
    }
}
