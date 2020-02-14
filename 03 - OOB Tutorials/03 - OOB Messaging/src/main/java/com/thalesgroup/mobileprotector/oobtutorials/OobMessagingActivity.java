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

import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Button;

import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.commonutils.thread.ExecutionService;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.OobMessageListener;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.OobMessagingLogic;
import com.thalesgroup.mobileprotector.oobtutorials.oobmessaging.R;
import com.thalesgroup.mobileprotector.oobtutorials.oobsetup.OobSetupLogic;

/**
 * Main entry point of the application for current flavor.
 */
public class OobMessagingActivity extends OobRegistrationActivity implements OobMessageListener {

    //region Declaration

    private Button mBtnFetchMessage;

    //endregion

    //region AbstractBaseActivity

    @Override
    public void setup() {
        // Load flavour related view instead of basic one.
        setContentView(R.layout.activity_oobmessaging);

        // Initialise Mobile Protector SDK.
        OobSetupLogic.setup();
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
        if (mBtnFetchMessage != null) {
            mBtnFetchMessage.setOnClickListener(sender -> onButtonPressedFetchMessage());
        }
    }

    @Nullable
    @Override
    protected SoftOathToken updateGui() {
        // Get stored token
        final SoftOathToken token = super.updateGui();

        // To make demo simple we will just disable / enable UI.
        if (mBtnFetchMessage != null) {
            mBtnFetchMessage.setEnabled(token != null);
        }

        return token;
    }

    //endregion

    //region Life Cycle

    @Override
    public void onResume() {
        super.onResume();

        OobMessagingLogic.setCurrentOobListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        OobMessagingLogic.setCurrentOobListener(null);
    }

    //endregion

    //region OobMessageListener

    @Override
    public void onOobLoadingShow(final int resId) {
        loadingBarShow(resId);
    }

    @Override
    public void onOobLoadingHide() {
        loadingBarHide();
    }

    @Override
    public void onOobDisplayMessage(final String message) {
        displayMessageDialog(message);
    }

    @Override
    public void onOobApproveMessage(final String message,
                                    final SecureString serverChallenge,
                                    final ApproveMessageResponse handler) {

        // Prepare dialog to display.
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.push_approve_qeustion);
        builder.setMessage(message);

        // Approve request.
        builder.setPositiveButton(R.string.push_approve_qeustion_approve, (dialog, position) -> {
            // Hide dialog.
            dialog.dismiss();

            // Continue approval process.
            approveMessage(serverChallenge, handler);
        });

        // Deny request.
        builder.setNegativeButton(R.string.push_approve_qeustion_deny, (dialog, position) -> {
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

    private void approveMessage(final SecureString serverChallenge,
                                final ApproveMessageResponse handler) {
        // Get currently provisioned token.
        final SoftOathToken token = ProvisioningLogic.getToken();
        if (token == null) {
            throw new IllegalStateException(getString(R.string.token_not_provisioned));
        }

        // Get pin from user.
        userPin(pin -> {
            OtpValue otpValue = null;

            try {
                // Server challenge is used for ocra calculation during transaction sign.
                // Without challenge it's authentication request.
                if (serverChallenge == null) {
                    otpValue = OtpLogic.generateOtp(token, pin);
                } else {
                    otpValue = OobMessagingLogic.generateOtp(token, serverChallenge, pin);
                }
            } catch (final IdpException exception) {
                displayMessageDialog(exception);
            }

            // We can return control to handler.
            // If there is some error present and OTPValue is nil, we still want to send response to server. In this case reject.
            handler.onFinished(otpValue);

            // All secure containers should be wiped asap.
            pin.wipe();
            if (otpValue != null) {
                otpValue.wipe();
            }
        });
    }

    //endregion

    //region User Interface

    private void onButtonPressedFetchMessage() {
        // Any incoming messages are handled through OobMessageListener as normal push notification.
        OobMessagingLogic.fetchMessages();
    }

    //endregion
}
