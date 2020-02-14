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

package com.thalesgroup.mobileprotector.tutorials.advancedsetup;

import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.msp.MspSignatureKey;
import com.gemalto.idp.mobile.otp.OtpConfiguration;

import java.util.List;

/**
 * Configuration values needed for Mobile Protector SDK setup. This setup is used for advanced Mobile Protector features
 * (FaceID, OOB, MSP).
 */
public class AdvancedSetupConfig {

    public enum ProtectorFaceIdState {
        /**
         * Face Id service was not even started.
         */
        ProtectorFaceIdStateUndefined,
        /**
         * Face id is not supported
         */
        ProtectorFaceIdStateNotSupported,
        /**
         * Failed to registered.
         */
        ProtectorFaceIdStateUnlicensed,
        /**
         * Successfully registered.
         */
        ProtectorFaceIdStateLicensed,
        /**
         * Failed to init service.
         */
        ProtectorFaceIdStateInitFailed,
        /**
         * Registered and initialised.
         */
        ProtectorFaceIdStateInitialized,
        /**
         * Registered, initialised and configured with at least one user enrolled.
         */
        ProtectorFaceIdStateReadyToUse
    }

    private static final byte[] ACTIVATION_CODE = {
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00
    };

    /**
     * Retrieves the activation code.
     *
     * @return Activation code.
     */
    @NonNull
    public static byte[] getActivationCode() {
        return ACTIVATION_CODE.clone();
    }

    /**
     * Returns the Token root policy - alters the rooted behaviour in a predefined way during OTP related scenarios.
     *
     * @return Token root policy.
     */
    @NonNull
    public static OtpConfiguration.TokenRootPolicy getTokenRootPolicy() {
        return OtpConfiguration.TokenRootPolicy.IGNORE;
    }

    /**
     * Returns the MSP obfuscation code - code used for obfuscation in the Mobile Signing Protocol.
     *
     * @return MSP obfuscation code.
     */
    public static List<byte[]> getMspObfuscationCode() {
        return null;
    }

    /**
     * Returns the MSP signing keys - keys used for signing in the Mobile Signing Protocol.
     *
     * @return MSP signing keys.
     */
    public static List<MspSignatureKey> getMspSignKeys() {
        return null;
    }

    /**
     * Returns the Face ID product key.
     *
     * @return Face ID product key.
     */
    @NonNull
    static String getFaceIdProductKey() {
        return "";
    }

    /**
     * Returns the Face ID server URL.
     *
     * @return Face ID server URL.
     */
    @NonNull
    static String getFaceIdServerUrl() {
        return "";
    }
}
