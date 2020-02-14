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

package com.thalesgroup.mobileprotector.oobtutorials.oobmessaging;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.core.ApplicationContextHolder;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.root.RootDetector;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.msp.MspBaseAlgorithm;
import com.gemalto.idp.mobile.msp.MspData;
import com.gemalto.idp.mobile.msp.MspFactory;
import com.gemalto.idp.mobile.msp.MspField;
import com.gemalto.idp.mobile.msp.MspFrame;
import com.gemalto.idp.mobile.msp.MspOathData;
import com.gemalto.idp.mobile.msp.MspParser;
import com.gemalto.idp.mobile.msp.exception.MspException;
import com.gemalto.idp.mobile.oob.OobException;
import com.gemalto.idp.mobile.oob.OobManager;
import com.gemalto.idp.mobile.oob.message.OobFetchMessageResponse;
import com.gemalto.idp.mobile.oob.message.OobIncomingMessage;
import com.gemalto.idp.mobile.oob.message.OobIncomingMessageType;
import com.gemalto.idp.mobile.oob.message.OobMessageManager;
import com.gemalto.idp.mobile.oob.message.OobTransactionSigningRequest;
import com.gemalto.idp.mobile.oob.message.OobTransactionSigningResponse;
import com.gemalto.idp.mobile.oob.message.OobTransactionVerifyRequest;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpConfig;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.OobRegistrationConfig;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.OobRegistrationLogic;

import static com.gemalto.idp.mobile.oob.message.OobTransactionSigningResponse.OobTransactionSigningResponseValue.ACCEPTED;
import static com.gemalto.idp.mobile.oob.message.OobTransactionSigningResponse.OobTransactionSigningResponseValue.REJECTED;

/**
 * Implementation of OOB sign transaction logic.
 */
public class OobMessagingLogic extends AbstractBaseLogic {

    private static OobMessageListener sOobListener = null;

    public static void setCurrentOobListener(final OobMessageListener listener) {
        sOobListener = listener;
    }

    public static void fetchMessages() {
        // Fetching make sense only with registered listener.
        if (sOobListener == null) {
            throw new IllegalStateException(getString(R.string.oob_listener_not_set));
        }

        final String locClientId = OobRegistrationLogic.readClientId();

        // Prepare manager with current client and provider id.
        final OobManager manager = OobRegistrationLogic.initializeOOBManager();
        final OobMessageManager oobMessageManager = manager.getOobMessageManager(locClientId, OobRegistrationConfig.getOobProviderId());

        // Display loading bar to indicate message downloading.
        sOobListener.onOobLoadingShow(R.string.loading_fetch);

        // Try to fetch any possible messages on server.
        oobMessageManager.fetchMessage(30, oobFetchMessageResponse -> processFetchResponse(oobMessageManager, oobFetchMessageResponse));
    }

    public static OtpValue generateOtp(@NonNull final SoftOathToken token,
                                       @NonNull final SecureString serverChallenge,
                                       @NonNull final AuthInput pin) throws IdpException {

        // Detect root status.
        if (IdpCore.getInstance().getRootDetector().getRootStatus() != RootDetector.RootStatus.NOT_ROOTED) { //NOPMD
            // Handle root status according to app policy.
        }

        // Get oath factory and prepare oath settings.
        final OathFactory oathFactory = OathService.create(OtpModule.create()).getFactory();
        final SoftOathSettings softOathSettings = oathFactory.createSoftOathSettings();
        softOathSettings.setOcraSuite(OtpConfig.getOcraSuite());

        // Create device based on specific ocra suite.
        final OathDevice oathDevice = oathFactory.createSoftOathDevice(token, softOathSettings);
        final SecureString otp = oathDevice.getOcraOtp(pin, serverChallenge, null, null, null);

        return new OtpValue(otp, oathDevice.getLastOtpLifespan(), OtpConfig.getOTPLifetime());
    }

    private static String getStringByKeyName(final String aString) {
        final Context context = ApplicationContextHolder.getContext();
        final int resId = context.getResources().getIdentifier(aString, "string", context.getPackageName());
        if (resId == 0) {
            return context.getString(R.string.message_not_found);
        } else {
            return context.getString(resId);
        }
    }


    private static void processFetchResponse(final OobMessageManager manager,
                                             final OobFetchMessageResponse response) {
        // Downloading is done, we can hide dialog.
        sOobListener.onOobLoadingHide();

        // Check response code and either proccess incomming message or display error.
        if (response.isSucceeded() && response.getOobIncomingMessage() != null) {
            processIncommingMessage(manager, response.getOobIncomingMessage());
        } else if (response.getMessage() != null) {
            sOobListener.onOobDisplayMessage(response.getMessage());
        }

    }

    private static void processIncommingMessage(final OobMessageManager oobMessageManager,
                                                final OobIncomingMessage message) {

        if (message.getMessageType().equalsIgnoreCase(OobIncomingMessageType.TRANSACTION_SIGNING)) {
            // Sign request.
            processTransactionSigningRequest(oobMessageManager, (OobTransactionSigningRequest) message);
        } else if (message.getMessageType().equalsIgnoreCase(OobIncomingMessageType.TRANSACTION_VERIFY)) {
            // Verify request
            processTransactionVerifyRequest(oobMessageManager, (OobTransactionVerifyRequest) message);
        }
    }

    private static void processTransactionSigningRequest(final OobMessageManager oobMessageManager,
                                                         final OobTransactionSigningRequest request) {
//        NSError         *internalError  = nil;
        final MspParser parser = MspFactory.createMspParser();

        // Get message subject key and fill in all values.
        String subject = getStringByKeyName(request.getSubject().toString());
        for (final String key : request.getMeta().keySet()) {
            final String value = request.getMeta().get(key);
            if (value != null) {
                final String placeholder = String.format("%%%s", key);
                subject = subject.replace(placeholder, value);
            }
        }

        try {
            // Try to parse frame.
            final MspFrame frame = parser.parse(request.getMspFrame().toByteArray());
            final MspData data = parser.parseMspData(frame);

            // For purpose of this sample app we will support only OATH.
            if (data.getBaseAlgo() != MspBaseAlgorithm.BASE_OATH) {
                return;
            }

            // Server challenge is send only for transaction sign. Not authentication.
            final MspField ocraServerChallenge = ((MspOathData) data).getOcraServerChallenge();
            SecureString serverChallange = null;
            if (ocraServerChallenge != null) {
                serverChallange = ocraServerChallenge.getValue();
            }

            sOobListener.onOobApproveMessage(subject,
                    serverChallange,
                    otpValue -> {
                        // Display loading bar to indicate message sending.
                        sOobListener.onOobLoadingShow(R.string.loading_sending);

                        // If we get OTP it mean, that user did approved request.
                        try {
                            OobTransactionSigningResponse response;
                            if (otpValue == null) {
                                response = request.createResponse(REJECTED, null, null);
                            } else {
                                response = request.createResponse(ACCEPTED, otpValue.getOtp(), null);
                            }

                            // Send message and wait display result.
                            oobMessageManager.sendMessage(response, oobMessageResponse -> {
                                // Hide loading indicator in all cases, because sending is done.
                                sOobListener.onOobLoadingHide();
                                // Display response.
                                sOobListener.onOobDisplayMessage(oobMessageResponse.getMessage());
                            });
                        } catch (final OobException exception) {
                            sOobListener.onOobDisplayMessage(exception.getMessage());
                        }
                    });
        } catch (final MspException exception) {
            sOobListener.onOobDisplayMessage(exception.getMessage());
        }
    }

    private static void processTransactionVerifyRequest(final OobMessageManager oobMessageManager,
                                                        final OobTransactionVerifyRequest request) {
        // Empty sample method
    }

}
