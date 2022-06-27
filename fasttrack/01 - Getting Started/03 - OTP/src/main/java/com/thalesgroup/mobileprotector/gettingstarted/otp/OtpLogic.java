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

import androidx.annotation.NonNull;

import com.gemalto.idp.mobile.fasttrack.FastTrack;
import com.gemalto.idp.mobile.fasttrack.FastTrackException;
import com.gemalto.idp.mobile.fasttrack.protector.ProtectorAuthInput;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.helpers.BaseLogic;
import com.thalesgroup.mobileprotector.commonutils.helpers.OtpValue;
import com.thalesgroup.mobileprotector.gettingstarted.provisioning.ProvisioningConfig;

/**
 * Mobile Protector SDK logic for OTP generation.
 */
public class OtpLogic extends BaseLogic {

    /**
     * Generates an OTP with PIN.
     *
     * @param token Token to be used for OTP generation.
     * @param pin   PIN.
     * @return Generated OTP.
     * @throws FastTrackException If error during OTP generation occurs.
     */
    public static OtpValue generateOtp(@NonNull final OathTokenDevice token,
                                       @NonNull final String pin) throws FastTrackException {
        return new OtpValue(token.getOtp(pin), token.getLastOtpLifeSpan(), ProvisioningConfig.getOtpLifespan());
    }

    /**
     * Generates an OTP with Authentication Input.
     *
     * @param token     Token to be used for OTP generation.
     * @param authInput Authentication input, e.g. Biometric or Secure Keypad.
     * @return Generated OTP.
     * @throws FastTrackException If error during OTP generation occurs.
     */
    public static OtpValue generateOtpWithAuthInput(@NonNull final OathTokenDevice token,
                                                    @NonNull final ProtectorAuthInput authInput) throws FastTrackException {
        return new OtpValue(token.getOtp(authInput), token.getLastOtpLifeSpan(), ProvisioningConfig.getOtpLifespan());
    }
}
