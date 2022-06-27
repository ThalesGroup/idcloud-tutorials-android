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

package com.thalesgroup.mobileprotector.gettingstarted.provisioning;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.devicefingerprint.DeviceFingerprintSource;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.Token;
import com.gemalto.idp.mobile.otp.TokenManager;
import com.gemalto.idp.mobile.otp.devicefingerprint.DeviceFingerprintTokenPolicy;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.OathTokenManager;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.idp.mobile.otp.provisioning.EpsConfigurationBuilder;
import com.gemalto.idp.mobile.otp.provisioning.MobileProvisioningProtocol;
import com.gemalto.idp.mobile.otp.provisioning.ProvisioningConfiguration;
import com.thalesgroup.mobileprotector.commonutils.callbacks.GenericHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

/**
 * Logic for token provisioning using Mobile Protector SDK.
 */
public class ProvisioningLogic extends AbstractBaseLogic {

    /**
     * Provisions asynchronously a new token.
     *
     * @param userId           User id.
     * @param registrationCode Registration code.
     * @param callback         Callback back to the application - called on Main UI Thread.
     */
    public static void provisionWithUserId(@NonNull final String userId,
                                           @NonNull final SecureString registrationCode,
                                           @NonNull final GenericHandler callback) {
        final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
        try {
            final ProvisioningConfiguration provisioningConfiguration = new EpsConfigurationBuilder(registrationCode,
                    new URL(ProvisioningConfig.getProvisioningUrl()),
                    ProvisioningConfig.getDomain(),
                    MobileProvisioningProtocol.PROVISIONING_PROTOCOL_V5,
                    ProvisioningConfig.getRsaKeyId(),
                    ProvisioningConfig.getRsaKeyExponent(),
                    ProvisioningConfig.getRsaKeyModulus())
                    .setTlsConfiguration(ProvisioningConfig.getTlsConfiguration()).build();

            // Prepare fingerprint policy.
            final DeviceFingerprintSource deviceFingerprintSource = new DeviceFingerprintSource(ProvisioningConfig.getCustomFingerprintData(),
                    DeviceFingerprintSource.Type.SOFT);
            final DeviceFingerprintTokenPolicy deviceFingerprintTokenPolicy = new DeviceFingerprintTokenPolicy(true,
                    deviceFingerprintSource);
            oathTokenManager.createToken(userId,
                    provisioningConfiguration,
                    deviceFingerprintTokenPolicy,
                    new TokenManager.TokenCreationCallback() {
                        @Override
                        public void onSuccess(final Token token,
                                              final Map<String, String> map) {
                            callback.onFinished(true, getString(R.string.token_provision_success));
                            registrationCode.wipe();
                        }

                        @Override
                        public void onError(final IdpException exception) {
                            callback.onFinished(false, exception.getMessage());
                            registrationCode.wipe();
                        }
                    });
        } catch (final MalformedURLException exception) {
            // This should not happen.
            throw new IllegalStateException(exception.getMessage());
        }
    }

    /**
     * Retrieves first token.
     *
     * @return Token.
     * If error occurred.
     */
    @Nullable
    public static SoftOathToken getToken() {
        SoftOathToken retValue = null;

        try {
            final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
            final Set<String> tokenNames = oathTokenManager.getTokenNames();
            if (!tokenNames.isEmpty()) {
                retValue = oathTokenManager.getToken(tokenNames.iterator().next(), ProvisioningConfig.getCustomFingerprintData());
            }
        } catch (final IdpException exception) {
            // Application might want to handle invalid token in specific way. For example remove old one and ask for new provision.
            // Sample code will simple throw exception and crash.
            // Error here usually mean wrong password or SDK configuration.
            throw new IllegalStateException(exception.getMessage());
        }

        return retValue;
    }

    /**
     * Removes the token.
     *
     * @return Token.
     */
    public static boolean removeToken() {
        try {
            final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
            return oathTokenManager.removeToken(ProvisioningLogic.getToken());
        } catch (final IdpException exception) {
            // Application might want to handle invalid token in specific way.
            // Error here usually mean wrong password or SDK configuration.
            throw new IllegalStateException(exception.getMessage());
        }
    }
}
