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

import android.support.annotation.NonNull;
import android.util.Base64;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericOtpHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.commonutils.thread.ExecutionService;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpLogic;

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
import java.util.Map;

/**
 * Logic for user authentication/OTP verification.
 */
public class InBandVerificationLogic extends AbstractBaseLogic {

    private static final String XML_TEMPLATE_AUTH = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "    <AuthenticationRequest>" + "    <UserID>%s</UserID>"
            + "    <OTP>%s</OTP>" + "    </AuthenticationRequest>";

    /**
     * Validates token with authentication server.
     *
     * @param token             Token to be verified.
     * @param authInput         Selected authentication input.
     * @param completionHandler Completion handler triggered in UI thread once operation is done.
     */
    public static void verifyWithToken(
            SoftOathToken token,
            AuthInput authInput,
            GenericOtpHandler completionHandler
    ) {
        try {
            verifyWithToken(token.getName(), OtpLogic.generateOtp(token, authInput), completionHandler);
        } catch (final IdpException exception) {
            completionHandler.onFinished(false, exception.getMessage(), null);
        }
    }

    /**
     * Validates generated OTP with authentication server.
     *
     * @param tokenName         User Id / Token Name
     * @param otpValue          Generated OTP.
     * @param completionHandler Callback to the application
     */
    private static void verifyWithToken(
            String tokenName,
            OtpValue otpValue,
            final GenericOtpHandler completionHandler
    ) {

        String toHash = String.format("%s:%s",
                InBandVerificationConfig.getBasicAuthenticationUsername(),
                InBandVerificationConfig.getBasicAuthenticationPassword());
        String hash = Base64.encodeToString(toHash.getBytes(StandardCharsets.UTF_8), Base64.DEFAULT);
        String body = String.format(XML_TEMPLATE_AUTH, tokenName, otpValue.getOtp().toString());
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", String.format("Basic %s", hash));

        // We don't need otp any more. Wipe it.
        otpValue.wipe();

        ExecutionService.getExecutionService().runOnBackgroudThread(() -> doPostRequest(InBandVerificationConfig.getAuthenticationUrl(),
                "text/xml", headers, body, (success, result) -> {
                    boolean valid = success && result.equalsIgnoreCase("Authentication succeeded");
                    completionHandler.onFinished(valid, result, otpValue.getLifespan());
                }));
    }

    private static HttpURLConnection createConnection(
            @NonNull String hostUrl,
            @NonNull String contentType,
            @NonNull Map<String, String> headers
    ) throws IOException {
        URL url = new URL(hostUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            connection.setRequestProperty(key, value);
        }

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true);
        connection.setUseCaches(false);
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(10000);

        return connection;
    }

    private static String convertStreamToString(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8), 1024);
                int numberOfCharacters = reader.read(buffer);
                while (numberOfCharacters != -1) {
                    writer.write(buffer, 0, numberOfCharacters);
                    numberOfCharacters = reader.read(buffer);
                }
            } finally {
                inputStream.close();
            }

            return writer.toString();
        }

        return "";
    }

    /**
     * Does a POST request.
     *
     * @param hostUrl     URL.
     * @param contentType Content type.
     * @param headers     Headers.
     * @param body        Body.
     * @param callback    Callback back to the application.
     */
    private static void doPostRequest(
            @NonNull final String hostUrl,
            @NonNull final String contentType,
            @NonNull final Map<String, String> headers,
            @NonNull final String body,
            @NonNull final GenericHandler callback
    ) {
        try {
            HttpURLConnection connection = createConnection(hostUrl, contentType, headers);

            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream())) {
                outputStreamWriter.write(body);
                outputStreamWriter.flush();
            }

            int statusCode = connection.getResponseCode();
            String responseBody; // NOPMD - no reason to turn final local variable in to field
            if (statusCode > 226) {
                responseBody = "";
            } else {
                responseBody = convertStreamToString(connection.getInputStream());
            }

            ExecutionService.getExecutionService().runOnMainUiThread(() -> callback.onFinished(true, responseBody));
        } catch (final IOException exception) {
            ExecutionService.getExecutionService().runOnMainUiThread(() -> callback.onFinished(false, exception.getMessage()));
        }
    }
}
