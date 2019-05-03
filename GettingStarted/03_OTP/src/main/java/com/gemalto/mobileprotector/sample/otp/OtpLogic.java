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

package com.gemalto.mobileprotector.sample.otp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.IdpStorageException;
import com.gemalto.idp.mobile.core.root.RootDetector;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.OtpModule;
import com.gemalto.idp.mobile.otp.oath.OathDevice;
import com.gemalto.idp.mobile.otp.oath.OathFactory;
import com.gemalto.idp.mobile.otp.oath.OathService;
import com.gemalto.idp.mobile.otp.oath.OathTokenManager;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathSettings;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.idp.mobile.ui.UiModule;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputBuilderV2;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputService;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputUi;
import com.gemalto.idp.mobile.ui.secureinput.SecurePinpadListenerV2;

import java.util.Set;

/**
 * Mobile Protector SDK logic for OTP generation.
 */
public class OtpLogic {

    private static DialogFragment sDialogFragment;

    /**
     * Generates an OTP.
     *
     * @param token
     *         Token to be used for OTP generation.
     * @param pin
     *         PIN.
     * @return Generated OTP.
     * @throws IdpException
     *         If error during OTP generation occures.
     */
    public static SecureString generateOtp(@NonNull final SoftOathToken token, @NonNull final AuthInput pin)
            throws IdpException {

        try {
            if (IdpCore.getInstance().getRootDetector().getRootStatus() != RootDetector.RootStatus.NOT_ROOTED) {
                // Handle root status according to app policy.
            }

            final OathFactory oathFactory = OathService.create(OtpModule.create()).getFactory();
            final SoftOathSettings softOathSettings = oathFactory.createSoftOathSettings();
            softOathSettings.setOcraSuite(OtpConfig.getOcraSuite());

            final OathDevice oathDevice = oathFactory.createSoftOathDevice(token, softOathSettings);
            return oathDevice.getTotp(pin);
        } finally {
            pin.wipe();
        }
    }

    /**
     * Retrieves the PIN.
     *
     * @param activity
     *         Activity on which to show the {@code DialogFragment}.
     * @param callback
     *         Callback to receive the PIN.
     */
    public static void getUserPin(@NonNull final AppCompatActivity activity, @NonNull final OtpPinCallback callback) {
        final SecureInputBuilderV2 builder = SecureInputService.create(UiModule.create()).getSecureInputBuilderV2();
        final SecureInputUi secureInputUi = builder.buildPinpad(false, false, true, new SecurePinpadListenerV2() {
            @Override
            public void onKeyPressedCountChanged(final int count, final int inputField) {
                // Handle on key pressed.
            }

            @Override
            public void onInputFieldSelected(final int inputField) {
                // Handle on input field selected.
            }

            @Override
            public void onOkButtonPressed() {
                // Handle on Button pressed.
            }

            @Override
            public void onDeleteButtonPressed() {
                // Handle on delete button pressed.
            }

            @Override
            public void onFinish(final PinAuthInput pinAuthInput, final PinAuthInput pinAuthInput1) {
                callback.onPinSuccess(pinAuthInput);
                builder.wipe();
                sDialogFragment.dismiss();
            }

            @Override
            public void onError(final String errorMessage) {
                callback.onPinError(errorMessage);
                builder.wipe();
                sDialogFragment.dismiss();
            }
        });

        sDialogFragment = secureInputUi.getDialogFragment();
        sDialogFragment.show(activity.getSupportFragmentManager(), "SECURE PIN");
    }

    /**
     * Retrieves an already provisioned token.
     *
     * @param name
     *         Token name.
     * @return Token.
     * @throws IdpException
     *         If error occurred.
     */
    @Nullable
    public static SoftOathToken getToken(@NonNull final String name) throws IdpException {
        final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
        return oathTokenManager.getToken(name);
    }

    /**
     * Removes the token.
     *
     * @param name
     *         Token name.
     * @return Token.
     * @throws IdpException
     *         If error occurred.
     */
    public static boolean removeToken(@NonNull final String name) throws IdpException {
        final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
        return oathTokenManager.removeToken(name);
    }

    /**
     * Retrieves the token names.
     *
     * @return {@code Set} of token names.
     */
    @Nullable
    public static Set<String> getTokenNames() {
        final OathTokenManager oathTokenManager = OathService.create(OtpModule.create()).getTokenManager();
        try {
            return oathTokenManager.getTokenNames();
        } catch (IdpStorageException e) {
            return null;
        }
    }
}
