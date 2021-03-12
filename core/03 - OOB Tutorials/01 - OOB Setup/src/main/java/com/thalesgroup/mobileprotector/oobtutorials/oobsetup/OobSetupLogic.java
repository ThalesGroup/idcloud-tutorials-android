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

import com.gemalto.idp.mobile.core.ApplicationContextHolder;
import com.gemalto.idp.mobile.core.IdpConfiguration;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.passwordmanager.PasswordManagerException;
import com.gemalto.idp.mobile.msp.MspConfiguration;
import com.gemalto.idp.mobile.oob.OobConfiguration;
import com.gemalto.idp.mobile.otp.OtpConfiguration;
import com.thalesgroup.gemalto.securelog.SecureLogConfig;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.tutorials.advancedsetup.AdvancedSetupConfig;

/**
 * Implementation of OOB registration and notification updates.
 */
public class OobSetupLogic extends AbstractBaseLogic {

    /**
     * Setups Mobile Protector SDK.
     */
    public static void setup() {
        if (!IdpCore.isConfigured()) {
            IdpCore.configureSecureLog(new SecureLogConfig.Builder(ApplicationContextHolder.getContext())
                    .publicKey(AdvancedSetupConfig.CFG_SLOG_MODULUS, AdvancedSetupConfig.CFG_SLOG_EXPONENT)
                    .build());

            IdpCore.configure(AdvancedSetupConfig.getActivationCode(), getModuleConfigurations());

            // Login so we can use secure storage, OOB etc..
            try {
                IdpCore.getInstance().getPasswordManager().login();
            } catch (final PasswordManagerException exception) {
                // Usually it means, that someone try to login with different password than last time.
                // Password was changes etc..
                throw new IllegalStateException(exception);
            }
        }
    }

    /**
     * Gets the configurations for all the required features.
     *
     * @return Feature configurations.
     */
    private static IdpConfiguration[] getModuleConfigurations() {
        // OTP module is required for token management and OTP calculation.
        final OtpConfiguration otpConfiguration = new OtpConfiguration.Builder().setRootPolicy(AdvancedSetupConfig.getTokenRootPolicy()).build();

        // OOB module is required for push notifications.
        final OobConfiguration oobConfiguration = new OobConfiguration.Builder()
                .setRootPolicy(OobSetupConfig.getOobRootPolicy())
                .setDeviceFingerprintSource(OobSetupConfig.getDeviceFingerPrintSource())
                .setTlsConfiguration(OobSetupConfig.getTlsConfiguration()).build();

        // Mobile Signing Protocol QR parsing, push messages etc..
        final MspConfiguration.Builder mspBuilder = new MspConfiguration.Builder();
        if (AdvancedSetupConfig.getMspObfuscationCode() != null) {
            mspBuilder.setObfuscationKeys(AdvancedSetupConfig.getMspObfuscationCode());
        }

        if (AdvancedSetupConfig.getMspSignKeys() != null) {
            mspBuilder.setSignatureKeys(AdvancedSetupConfig.getMspSignKeys());
        }

        // Return all configurations.
        return new IdpConfiguration[]{ otpConfiguration, oobConfiguration, mspBuilder.build() };
    }

}
