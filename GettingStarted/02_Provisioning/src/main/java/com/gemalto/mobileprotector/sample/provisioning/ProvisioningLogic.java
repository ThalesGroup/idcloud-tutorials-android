/*
 * MIT License
 *
 * Copyright (c) 2019 Thales DIS
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

package com.gemalto.mobileprotector.sample.provisioning;

import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.core.IdpNetworkException;
import com.gemalto.idp.mobile.core.IdpStorageException;
import com.gemalto.idp.mobile.core.devicefingerprint.DeviceFingerprintException;
import com.gemalto.idp.mobile.core.passwordmanager.PasswordManagerException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.idp.mobile.otp.oath.OathTokenManager;
import com.gemalto.idp.mobile.otp.provisioning.EpsConfigurationBuilder;
import com.gemalto.idp.mobile.otp.provisioning.MobileProvisioningProtocol;
import com.gemalto.idp.mobile.otp.provisioning.ProvisioningConfiguration;
import com.gemalto.mobileprotector.sample.util.thread.ExecutionService;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Logic for token provisioning using Mobile Protector SDK.
 */
public class ProvisioningLogic {

    /**
     * Provisions asynchronously a new token.
     *
     * @param userId
     *         User id.
     * @param registrationCode
     *         Registration code.
     * @param callback
     *         Callback back to the application - called on Main UI Thread.
     */
    public static void provision(@NonNull final String userId,
                                 @NonNull final SecureString registrationCode,
                                 @NonNull final ProvisioningCallback callback) {
        final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
        try {
            final ProvisioningConfiguration provisioningConfiguration = new EpsConfigurationBuilder(registrationCode,
                                                                                                    new URL(ProvisioningConfig
                                                                                                                    .getProvisioningUrl()),
                                                                                                    MobileProvisioningProtocol.PROVISIONING_PROTOCOL_V3,
                                                                                                    ProvisioningConfig
                                                                                                            .getRsaKeyId(),
                                                                                                    ProvisioningConfig
                                                                                                            .getRsaKeyExponent(),
                                                                                                    ProvisioningConfig
                                                                                                            .getRsaKeyModulus())
                    .setTlsConfiguration(ProvisioningConfig.getTlsConfiguration()).build();

            ExecutionService.getExecutionService().runOnBackgroudThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        final OathToken token = oathTokenManager.createToken(userId, provisioningConfiguration);
                        ExecutionService.getExecutionService().runOnMainUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProvisioningSuccess(token);
                            }
                        });
                    } catch (final IdpStorageException | PasswordManagerException | DeviceFingerprintException | IdpNetworkException e) {
                        ExecutionService.getExecutionService().runOnMainUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onProvisioningError(e);
                            }
                        });
                    } finally {
                        registrationCode.wipe();
                    }
                }
            });
        } catch (final MalformedURLException e) {
            // This should not happen.
            throw new IllegalStateException(e.getMessage());
        }
    }
}
