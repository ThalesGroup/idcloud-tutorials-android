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
import android.os.Handler;
import android.os.Looper;

import com.gemalto.idp.mobile.core.ApplicationContextHolder;
import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.messenger.MobileMessageManager;
import com.gemalto.idp.mobile.fasttrack.messenger.MobileMessenger;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.FetchMessageCallback;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.FetchResponse;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.SendMessageCallback;
import com.gemalto.idp.mobile.fasttrack.messenger.callback.SendResponse;
import com.gemalto.idp.mobile.fasttrack.messenger.message.IncomingMessage;
import com.gemalto.idp.mobile.fasttrack.messenger.message.OutgoingMessage;
import com.gemalto.idp.mobile.fasttrack.messenger.message.TransactionSigningRequest;
import com.gemalto.idp.mobile.fasttrack.messenger.message.TransactionSigningResponse;
import com.gemalto.idp.mobile.fasttrack.messenger.message.TransactionVerifyRequest;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningConfig;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.OobRegistrationConfig;
import com.thalesgroup.mobileprotector.oobtutorials.oobregistration.OobRegistrationLogic;

import java.nio.charset.StandardCharsets;

import androidx.annotation.NonNull;

/**
 * Implementation of OOB sign transaction logic.
 */
public class OobMessagingLogic extends OobRegistrationLogic {

    private static OobMessageListener sOobListener = null;

    public static void setCurrentOobListener(final OobMessageListener listener) {
        sOobListener = listener;
    }

    public static void fetchMessages() {
        // Fetching make sense only with registered listener.
        if (sOobListener == null) {
            throw new IllegalStateException(getString(R.string.oob_listener_not_set));
        }

        final String clientId = readClientId();
        if (clientId == null) {
            throw new IllegalStateException("ClientID is null!");
        }

        final MobileMessenger messenger = getMobileMessenger();

        // Display loading bar to indicate message downloading.
        sOobListener.onOobLoadingShow(R.string.loading_fetch);

        final MobileMessageManager messageManager = messenger.getMessageManager(clientId, OobRegistrationConfig.getOobProviderId());
        if (messageManager == null) {
            throw new IllegalStateException("Failed to retrieve MobileMessageManager");
        }

        final Handler handler = new Handler(Looper.getMainLooper());

        final FetchMessageCallback callback = new FetchMessageCallback() {
            @Override
            public void onFetchResponse(@NonNull final FetchResponse response) {
                handler.post(() -> {
                    sOobListener.onOobLoadingHide();

                    if (response.isSuccess()) {
                        processIncomingRequest(messageManager, response);
                    }
                });
            }

            @Override
            public void onError(@NonNull final FastTrackException exception) {
                handler.post(() -> {
                    sOobListener.onOobLoadingHide();
                    sOobListener.onOobDisplayMessage(exception.getLocalizedMessage());
                });
            }
        };

        // Try to fetch any possible messages on server.
        messageManager.fetchMessage(30, null, callback);
    }

    public static OtpValue generateOtp(@NonNull final OathTokenDevice token,
                                       @NonNull final String pin,
                                       final String serverChallenge) throws FastTrackException {

        return new OtpValue(token.getOcraOtp(pin, serverChallenge, null, null, null),
                token.getLastOtpLifeSpan(),
                ProvisioningConfig.getOtpLifespan()
        );
    }

    private static void processIncomingRequest(final MobileMessageManager messageManager,
                                               final FetchResponse response) {
        final IncomingMessage.Type type = response.getMessageType();
        switch (type) {
            case TRANSACTION_SIGNING_REQUEST: {
                final TransactionSigningRequest signingRequest = response.getTransactionSigningRequest();
                if (signingRequest == null) {
                    break;
                }

                processTransactionSigningRequest(messageManager, signingRequest);
                break;
            }

            case TRANSACTION_VERIFY_REQUEST: {
                final TransactionVerifyRequest verifyRequest = response.getTransactionVerifyRequest();
                if (verifyRequest == null) {
                    break;
                }

                // Sample implementation
                break;
            }

            default:
                sOobListener.onOobDisplayMessage(R.string.push_nothing_to_fetch);
                break;
        }
    }

    private static String getStringByKeyName(final String aString) {
        final Context context = ApplicationContextHolder.getContext();
        final int resId = context.getResources().getIdentifier(aString, "string", context.getPackageName());
        if (resId == 0) {
            return context.getString(R.string.message_not_found);
        }

        return context.getString(resId);
    }

    private static void processTransactionSigningRequest(final MobileMessageManager messageManager,
                                                         final TransactionSigningRequest request) {
        // Get message subject key and fill in all values.
        String subject = getStringByKeyName(request.getSubject());
        if (request.getMeta() != null) {
            for (final String key : request.getMeta().keySet()) {
                final String value = request.getMeta().get(key);
                if (value != null) {
                    final String placeholder = String.format("%%%s", key);
                    subject = subject.replace(placeholder, value);
                }
            }
        }

        final byte[] challengeBytes = request.getOcraServerChallenge();
        String serverChallenge = null;
        if (challengeBytes != null) {
            serverChallenge = new String(challengeBytes, StandardCharsets.UTF_8);
        }

        sOobListener.onOobApproveMessage(subject,
                serverChallenge,
                otpValue -> {
                    // Display loading bar to indicate message sending.
                    sOobListener.onOobLoadingShow(R.string.loading_sending);

                    final String otp = otpValue != null ? otpValue.getOtp() : null;

                    // If we get OTP it mean, that user did approved request.
                    try {
                        final OutgoingMessage responseMsg = request.createResponse(
                                otp != null ? TransactionSigningResponse.TransactionSigningResponseValue.ACCEPTED
                                        : TransactionSigningResponse.TransactionSigningResponseValue.REJECTED,
                                otp, null);

                        messageManager.sendMessage(responseMsg, null,
                                new SendMessageCallback() {
                                    @Override
                                    public void onSendMessageResponse(final SendResponse response) {
                                        // Hide the loading message
                                        sOobListener.onOobLoadingHide();

                                        sOobListener.onOobDisplayMessage(R.string.push_sent_message_to_server);
                                    }

                                    @Override
                                    public void onError(final FastTrackException exception) {
                                        // Hide the loading message
                                        sOobListener.onOobLoadingHide();

                                        sOobListener.onOobDisplayMessage(exception.getLocalizedMessage());
                                    }
                                });
                    } catch (final FastTrackException ex) {
                        // Hide the loading message
                        sOobListener.onOobLoadingHide();

                        sOobListener.onOobDisplayMessage(ex.getLocalizedMessage());
                    }
                });
    }

}
