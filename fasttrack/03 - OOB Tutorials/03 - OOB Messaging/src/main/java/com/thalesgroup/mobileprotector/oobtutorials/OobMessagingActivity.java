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

import android.app.AlertDialog;
import android.os.Build;
import android.widget.Button;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.commonutils.thread.ExecutionService;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.OobMessageListener;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.OobMessagingLogic;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.R;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * Main entry point of the application for current flavor.
 */
public class OobMessagingActivity extends OobRegistrationActivity implements OobMessageListener {

    //region Declaration

    private Button mBtnFetchMessage;

    //endregion

    //region AbstractBaseActivity

    @Override
    protected int contentViewID() {
        return R.layout.activity_oobmessaging;
    }

    @Override
    protected int caption() {
        return R.string.tutorials_oob_messaging_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        mBtnFetchMessage = findViewById(R.id.activity_oobmessaging_btn_fetch_message);
        if (mBtnFetchMessage != null)
            mBtnFetchMessage.setOnClickListener(sender -> OobMessagingLogic.fetchMessages());
    }

    @Nullable
    @Override
    protected OathTokenDevice updateGui() {
        // Get stored token
        OathTokenDevice token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnFetchMessage != null)
            mBtnFetchMessage.setEnabled(token != null);

        return token;
    }

    //endregion

    //region Life Cycle

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();

        OobMessagingLogic.setCurrentOobListener(this);

        OobMessagingLogic.sOobPushCallback = data -> {
            // Sample implementation
        };
    }

    @Override
    public void onPause() {
        super.onPause();

        OobMessagingLogic.setCurrentOobListener(null);
        OobMessagingLogic.sOobPushCallback = null;
    }

    //endregion

    //region OobMessageListener

    @Override
    public void onOobLoadingShow(int resId) {
        loadingBarShow(resId);
    }

    @Override
    public void onOobLoadingHide() {
        loadingBarHide();
    }

    @Override
    public void onOobDisplayMessage(String message) {
        displayMessageDialog(message);
    }

    @Override
    public void onOobDisplayMessage(int resId) {
        displayMessageDialog(resId);
    }

    @Override
    public void onOobApproveMessage(
            String message,
            String serverChallenge,
            ApproveMessageResponse handler
    ) {

        // Prepare dialog to display.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.push_approve_question);
        builder.setMessage(message);

        // Approve request.
        builder.setPositiveButton(R.string.push_approve_question_approve, (dialog, position) -> {
            // Hide dialog.
            dialog.dismiss();

            // Continue approval process.
            approveMessage(serverChallenge, handler);
        });

        // Deny request.
        builder.setNegativeButton(R.string.push_approve_question_deny, (dialog, position) -> {
            // Hide dialog.
            dialog.dismiss();

            // Response without otp = Deny.
            handler.onFinished(null);
        });

        // Display dialog in UI thread.
        ExecutionService.getExecutionService().runOnMainUiThread(builder::show);
    }

    //endregion

    //region Private Helpers

    private void approveMessage(
            String serverChallenge,
            ApproveMessageResponse handler
    ) {
        // Get currently provisioned token.
        OathTokenDevice token = ProvisioningLogic.getToken();
        if (token == null)
            throw new IllegalStateException(getString(R.string.token_not_provisioned));

        // Get pin from user.
        userPin((pin, error) -> {
                    if (error != null) {
                        displayMessageDialog(error);
                        return;
                    }

                    OtpValue otpValue = null;
                    try {

                        if (serverChallenge == null)
                            otpValue = OtpLogic.generateOtp(token, pin);
                        else
                            otpValue = OobMessagingLogic.generateOtp(token, pin, serverChallenge);

                    } catch (FastTrackException ex) {
                        displayMessageDialog(ex);
                    }

                    // We can return control to handler.
                    // Even error happened we still need to send response to server
                    handler.onFinished(otpValue);
                }
        );
    }

    //endregion
}
