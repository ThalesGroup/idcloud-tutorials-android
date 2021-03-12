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

package com.thalesgroup.mobileprotector.tutorials.transactionsign;

import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.root.RootDetector;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.otp.OtpConfig;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logic for user authentication/OTP verification.
 */
public class TransactionSignLogic extends AbstractBaseLogic {

    //region Declaration

    private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private static class KeyValue {
        private final String mKey, mValue;

        KeyValue(@NonNull final String key, @NonNull final String value) {
            mKey = key;
            mValue = value;
        }

        public String getKey() {
            return mKey;
        }

        public String getValue() {
            return mValue;
        }

        byte[] getKeyValueUTF8() {
            final String keyValue = mKey + ":" + mValue;
            return keyValue.getBytes(StandardCharsets.UTF_8);
        }
    }

    //endregion


    //region Public API

    /**
     * Generates an OTP for transaction signature.
     *
     * @param token       Token to be used for OTP generation.
     * @param pin         PIN.
     * @param amount      Amount to be signed.
     * @param beneficiary Beneficiary to be signed.
     * @return Generated OTP.
     * @throws IdpException If error during OTP generation occurs.
     */
    public static OtpValue generateOtp(@NonNull final SoftOathToken token,
                                       @NonNull final AuthInput pin,
                                       @NonNull final String amount,
                                       @NonNull final String beneficiary)
            throws IdpException {

        if (IdpCore.getInstance().getRootDetector().getRootStatus() != RootDetector.RootStatus.NOT_ROOTED) { //NOPMD
            // Handle root status according to app policy.
        }

        // Create service with respective settings
        final OathFactory oathFactory = OathService.create(OtpModule.create()).getFactory();
        final SoftOathSettings softOathSettings = oathFactory.createSoftOathSettings();
        softOathSettings.setOcraSuite(OtpConfig.getOcraSuite());

        // Create and use OATH device to calculate OTP
        final OathDevice oathDevice = oathFactory.createSoftOathDevice(token, softOathSettings);
        final SecureString otp = oathDevice.getOcraOtp(pin,
                getServerChallenge(amount, beneficiary),
                null,
                null,
                null);
        return new OtpValue(otp, oathDevice.getLastOtpLifespan(), OtpConfig.getOTPLifetime());
    }

    //endregion


    //region Private Helpers

    /**
     * Creates the challenge to be signed.
     *
     * @param amount      Amount to be signed.
     * @param beneficiary Beneficiary to be signed.
     * @return Calculated challenge based on IdCloud implementation.
     */
    private static SecureString getServerChallenge(final String amount, final String beneficiary) {
        final List<KeyValue> values = new ArrayList<>();
        values.add(new KeyValue("amount", amount));
        values.add(new KeyValue("beneficiary", beneficiary));

        return IdpCore.getInstance().getSecureContainerFactory().fromString((getOcraChallenge(values)));
    }

    /**
     * Calculate OCRA Challenge from array of key value objects.
     *
     * @param values List of key values object we want to use for ocra calculation.
     * @return SecureString representation of challenge or null in case of error.
     */
    private static String getOcraChallenge(final List<KeyValue> values) {
        String retValue = null;

        // Use builder to append TLV
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        // Go through all values, calculate and append TLV for each one of them.
        for (int index = 0; index < values.size(); index++) {
            // Convert keyvalue to UTF8 string
            final byte[] keyValueUTF8 = values.get(index).getKeyValueUTF8();

            // Build TLV.
            buffer.write(0xDF);
            buffer.write(0x71 + index);
            buffer.write((byte) keyValueUTF8.length);
            buffer.write(keyValueUTF8, 0, keyValueUTF8.length);
        }

        // Try to calculate digest from final string and build retValue.
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(buffer.toByteArray());

            // Server challenge expect hex string not byte array.
            retValue = bytesToHex(hash);
        } catch (final NoSuchAlgorithmException exception) {
            // Ignore. In worst case it will generate invalid ocra.
        }

        return retValue;
    }

    /**
     * Creates HEX string from bytes.
     *
     * @param bytes Bytes from which to create the hexa string.
     * @return Hexa string.
     */
    private static String bytesToHex(final byte[] bytes) {
        final char[] hexChars = new char[bytes.length * 2];
        for (int index = 0; index < bytes.length; index++) {
            final int value = bytes[index] & 0xFF;
            hexChars[index * 2] = HEX_ARRAY[value >>> 4];
            hexChars[index * 2 + 1] = HEX_ARRAY[value & 0x0F];
        }
        return new String(hexChars);
    }

    //endregion

}
