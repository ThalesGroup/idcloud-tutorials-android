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

package com.thalesgroup.mobileprotector.oobtutorials.oobsetup;

import com.gemalto.idp.mobile.fasttrack.MobileFingerprintSource;
import com.gemalto.idp.mobile.fasttrack.MobileTlsConfiguration;
import com.gemalto.idp.mobile.fasttrack.messenger.MobileMessenger;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupConfig;

import androidx.annotation.NonNull;

/**
 * OOB configuration.
 */
class OobSetupConfig extends AdvancedSetupConfig {

    //region OOB Server key info
    private static final byte[] OOB_RSA_KEY_EXPONENT = new byte[]{
            (byte) 0x00, (byte) 0x00, (byte) 0x00
    };

    private static final String OOB_RSA_KEY_MODULUS = "";
    //endregion

    /**
     * The URL of the OOB server API endpoint.
     *
     * @return OOB Registration URL.
     */
    static String getOobRegistrationUrl() {
        return "";
    }

    /**
     * OOB Domain.
     *
     * @return OOB Domain.
     */
    static String getOobDomain() {
        return "";
    }

    /**
     * App ID.
     *
     * @return App ID.
     */
    static String getOobAppId() {
        return "";
    }

    /**
     * RSA Key exponent.
     *
     * @return RSA Key exponent.
     */
    static byte[] getOobRsaKeyExponent() {
        return OOB_RSA_KEY_EXPONENT.clone();
    }

    /**
     * RSA Key modulus.
     *
     * @return RSA Key modulus.
     */
    static byte[] getOobRsaKeyModulus() {
        return compress(OOB_RSA_KEY_MODULUS);
    }

    /**
     * Optional value with custom finger print data. Used as input of encryption calculation
     *
     * @return The OOB fingerprint source
     */
    static MobileFingerprintSource getOobFingerprintSource() {
        return new MobileFingerprintSource(
                "FastTrackSDKExample".getBytes(),
                MobileFingerprintSource.Type.SOFT);
    }

    /**
     * Returns the Messenger root policy - alters the rooted behaviour in a predefined way during Messenger related scenarios.
     *
     * @return The Messenger root policy
     */
    @NonNull
    static MobileMessenger.RootPolicy getMessengerRootPolicy() {
        return MobileMessenger.RootPolicy.IGNORE;
    }

    /**
     * Retrieve TLS configuration.
     * For debug purposes we can weaken TLS configuration. In release mode all values must be set to NO. Otherwise it will
     * cause runtime exception.
     *
     * @return The TLS configuration
     */
    static MobileTlsConfiguration getOobTLSConfiguration() {
        return new MobileTlsConfiguration();
    }

    /**
     * @param hexString Hex String
     * @return byte array
     */
    @SuppressWarnings("all")
    private static byte[] compress(String hexString) {
        int slen = hexString.length();
        if (slen % 2 != 0)
            throw new IllegalArgumentException("Odd length");

        byte[] bs = new byte[slen / 2];
        for (int i = 0; i < slen / 2; i++) {
            String sub = hexString.substring(i * 2, (i * 2) + 2);
            bs[i] = (byte) Integer.parseInt(sub, 16);
        }

        return bs;
    }
}
