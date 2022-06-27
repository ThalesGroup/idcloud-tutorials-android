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

package com.thalesgroup.mobileprotector.tutorials.inbandverification;

import android.content.Context;

import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericOtpHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.commonutils.thread.ExecutionService;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningLogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Logic for user authentication/OTP verification.
 */
public class InBandVerificationLogic extends ProvisioningLogic {

    private static final String JSON_REQUEST = "{\n" +
            "    \"name\": \"Auth_OTP\",\n" +
            "    \"input\": {\n" +
            "        \"userId\": \"%s\",\n" +
            "        \"otp\" : \"%s\"\n" +
            "    }\n" +
            "}";

    /**
     * Validates token with authentication server.
     *
     * @param token             Token to be verified.
     * @param pin               PIN as String
     * @param completionHandler Completion handler triggered in UI thread once operation is done.
     */
    public static void verifyWithToken(final OathTokenDevice token,
                                       final String pin,
                                       final GenericOtpHandler completionHandler) {
        try {
            final OtpValue otpValue = OtpLogic.generateOtp(token, pin);
            final String userId = getIdCloudUserId();

            verifyOTP(userId, otpValue, completionHandler);
        } catch (final FastTrackException exception) {
            completionHandler.onFinished(false, exception.getMessage(), null);
        }
    }

    private static void verifyOTP(final String userId,
                                  final OtpValue otpValue,
                                  final GenericOtpHandler callback) {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format(Locale.US, "Bearer %s", InBandVerificationConfig.JWT));
        headers.put("X-API-KEY", InBandVerificationConfig.API_KEY);

        final String jsonRequest = String.format(Locale.US, JSON_REQUEST, userId, otpValue.getOtp());

        ExecutionService.getExecutionService().runOnBackgroundThread(
                () -> doPostRequest(
                        InBandVerificationConfig.getAuthenticationUrl(),
                        headers,
                        jsonRequest,
                        (success, result) -> {
                            final Context context = getContext();
                            assert context != null;

                            final boolean valid = success && result.contains("\"status\":\"Success\",");
                            if (!valid) {
                                result = context.getString(R.string.otp_verify_fail);
                            } else {
                                result = context.getString(R.string.otp_verify_success);
                            }

                            callback.onFinished(valid, result, otpValue.getLifespan());
                        }
                )
        );
    }

    private static HttpURLConnection createConnection(@NonNull final String hostUrl,
                                                      @NonNull final Map<String, String> headers) throws IOException {
        final URL url = new URL(hostUrl);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            connection.setRequestProperty(key, value);
        }

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);

        return connection;
    }

    private static String convertStreamToString(final InputStream inputStream) throws IOException {
        String response = "";

        do {
            if (inputStream == null) {
                break;
            }

            final Writer writer = new StringWriter();

            final char[] buffer = new char[1024];
            try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 1024)) {
                int numberOfCharacters = reader.read(buffer);
                while (numberOfCharacters != -1) {
                    writer.write(buffer, 0, numberOfCharacters);
                    numberOfCharacters = reader.read(buffer);
                }
            }

            response = writer.toString();
        } while (false);

        return response;
    }

    /**
     * Does a POST request.
     *
     * @param hostUrl  URL.
     * @param headers  Headers.
     * @param body     Body.
     * @param callback Callback back to the application.
     */
    private static void doPostRequest(@NonNull final String hostUrl,
                                      @NonNull final Map<String, String> headers,
                                      @NonNull final String body,
                                      @NonNull final GenericHandler callback) {
        final ExecutionService service = ExecutionService.getExecutionService();

        try {
            final HttpURLConnection connection = createConnection(hostUrl, headers);

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
                writer.write(body);
                writer.flush();
            }

            final int statusCode = connection.getResponseCode();
            String responseBody;
            if (statusCode > 226) {
                responseBody = "";
            } else {
                responseBody = convertStreamToString(connection.getInputStream());
            }

            service.runOnMainUiThread(() -> callback.onFinished(true, responseBody));
        } catch (final IOException exception) {
            service.runOnMainUiThread(() -> callback.onFinished(false, exception.getMessage()));
        }
    }
}
