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

package com.thalesgroup.mobileprotector.tutorials;

import com.thalesgroup.mobileprotector.gettingstarted.OtpActivity;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupLogic;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.R;

/**
 * Main entry point of the application for the Basic setup flavor.
 */
public class AdvancedSetupActivity extends OtpActivity {

    //region AbstractBaseActivity

    @Override
    protected int contentViewID() {
        return R.layout.activity_advancedsetup;
    }

    @Override
    public void setup() {
        // Initialise Mobile Protector SDK.
        AdvancedSetupLogic.setup();

        // Visual notification, since basic setup does not have more use cases.
        displayMessageDialog(R.string.setup_successful);
    }

    @Override
    protected int caption() {
        return R.string.tutorials_advanced_setup_caption;
    }

    //endregion
}
