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

package com.thalesgroup.mobileprotector.oobtutorials;

import com.gemalto.idp.mobile.core.util.SecureString;
import com.google.firebase.FirebaseApp;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.OobRegistrationLogic;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.R;
import com.thalesgroup.mobileprotector.oobtutorials.oobsetup.OobSetupLogic;

/**
 * Main entry point of the application for current flavor.
 */
public class OobRegistrationActivity extends OobSetupActivity {

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_oobregistration);

        // Initialise Mobile Protector SDK.
        OobSetupLogic.setup();

        // Initialize Firebase app
        FirebaseApp.initializeApp(this);
    }

    @Override
    protected int caption() {
        return R.string.tutorials_oob_registration_caption;
    }

    //endregion

    //region ProvisioningFragmentDelegate

    @Override
    public void onProvision(final String userId, final SecureString registrationCode) {
        // Show registration process.
        loadingBarShow(R.string.loading_registering);

        // Register to OOB first. Only then continue with token.
        OobRegistrationLogic.registerToMsm(userId, registrationCode, (success, result) -> {
            // Hide registration process.
            loadingBarHide();

            // OOB registration was successful.
            if (success) {
                // Registration code is wiped in super class once it's not needed.
                OobRegistrationActivity.super.onProvision(userId, registrationCode);
            } else {
                // Wipe code.
                registrationCode.wipe();
                displayMessageDialog(result);
            }
        });
    }

    @Override
    public void onRemoveToken() {
        // Show un-registration process.
        loadingBarShow(R.string.loading_unregistering);

        // First unregister from OOB, then continue with removing token
        OobRegistrationLogic.unregisterFromMsm((success, result) -> {
            // Hide un-registration process.
            loadingBarHide();

            if (success) {
                OobRegistrationActivity.super.onRemoveToken();
            } else {
                displayMessageDialog(result);
            }
        });
    }

    //endregion

}
