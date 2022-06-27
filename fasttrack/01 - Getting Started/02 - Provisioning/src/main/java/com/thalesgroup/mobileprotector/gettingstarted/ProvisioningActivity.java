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

package com.thalesgroup.mobileprotector.gettingstarted;

import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.R;
import com.thalesgroup.mobileprotector.uicomponents.ProvisioningFragment;
import com.thalesgroup.mobileprotector.uicomponents.ProvisioningFragmentDelegate;

import androidx.annotation.Nullable;

/**
 * Main entry point of the application for current flavor.
 */
public class ProvisioningActivity extends SetupActivity implements ProvisioningFragmentDelegate {

    //region Declaration

    /**
     * Fragment providing inputs for User Id, Registration Code and buttons to provision or
     * remove provisioned token.
     */
    private ProvisioningFragment mProvisioningFragment;

    //endregion

    //region AbstractBaseActivity

    @Override
    protected int contentViewID() {
        return R.layout.activity_provisioning;
    }

    @Override
    protected int caption() {
        return R.string.gettingstarted_provisioning_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mProvisioningFragment = (ProvisioningFragment) getSupportFragmentManager().findFragmentById(R.id.shared_fragment_provisioning);
        if (mProvisioningFragment != null) {
            mProvisioningFragment.setDelegate(this);
        }
    }

    @Nullable
    @Override
    protected OathTokenDevice updateGui() {

        // Get stored token
        final OathTokenDevice token = ProvisioningLogic.getToken();

        // Update all UI fragments if they are present.
        if (mProvisioningFragment != null) {
            mProvisioningFragment.updateGui(token);
        }

        return token;
    }

    //endregion

    //region Shared

    protected void setQrCodeButtonVisible() {
        if (mProvisioningFragment != null) {
            mProvisioningFragment.setQrCodeButtonVisible();
        }
    }

    //endregion

    //region ProvisioningFragmentDelegate

    @Override
    public void onProvision(final String userId, final String registrationCode) {
        loadingBarShow(R.string.loading_provisioning);

        ProvisioningLogic.provisionWithUserId(userId, registrationCode, (success, result) -> {
            // Hide loading overlay.
            loadingBarHide();

            // Display result.
            displayMessageDialog(result);

            updateGui();
        });
    }

    @Override
    public void onProvisionUsingQr() {
        // Qr code registration is explained in later tutorial.
    }

    @Override
    public void onRemoveToken() {
        if (ProvisioningLogic.removeToken()) {
            displayMessageDialog(R.string.token_remove_success);
            updateGui();
        } else {
            displayMessageDialog(R.string.token_remove_failed);
        }
    }

    //endregion

}
