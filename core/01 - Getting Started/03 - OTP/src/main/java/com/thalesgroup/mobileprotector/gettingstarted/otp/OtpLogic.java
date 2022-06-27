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

package com.thalesgroup.mobileprotector.gettingstarted.otp;

import android.support.annotation.NonNull;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.root.RootDetector;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;

/**
 * Mobile Protector SDK logic for OTP generation.
 */
public class OtpLogic extends AbstractBaseLogic {

    /**
     * Generates an OTP.
     *
     * @param token Token to be used for OTP generation.
     * @param pin   PIN.
     * @return Generated OTP.
     * @throws IdpException If error during OTP generation occurs.
     */
    public static OtpValue generateOtp(@NonNull final SoftOathToken token,
                                       @NonNull final AuthInput pin) throws IdpException {

        if (IdpCore.getInstance().getRootDetector().getRootStatus() != RootDetector.RootStatus.NOT_ROOTED) { //NOPMD
            // Handle root status according to app policy.
        }

        final OathFactory oathFactory = OathService.create(OtpModule.create()).getFactory();
        final SoftOathSettings softOathSettings = oathFactory.createSoftOathSettings();
        softOathSettings.setOcraSuite(OtpConfig.getOcraSuite());

        final OathDevice oathDevice = oathFactory.createSoftOathDevice(token, softOathSettings);
        return new OtpValue(oathDevice.getTotp(pin), oathDevice.getLastOtpLifespan(), OtpConfig.getOTPLifetime());
    }
}
