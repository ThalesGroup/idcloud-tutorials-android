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

package com.thalesgroup.mobileprotector.tutorials.changepinbasic;

import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.thalesgroup.mobileprotector.commonutils.callbacks.QrCodeHandler;
import com.thalesgroup.mobileprotector.commonutils.helpers.AbstractBaseLogic;

import java.math.BigInteger;

/**
 * Logic for handling QR code related use case.
 */
public class QrCodeBasicLogic extends AbstractBaseLogic {
    public static void parseQRCode(final String qrCode, final QrCodeHandler handler) {
        // Two components in frame are user id and registration code.
        try {
            final byte[] arr = new BigInteger(qrCode, 16).toByteArray();
            final String[] components = new String(arr).split(",");

            if (components.length == 2) {
                // Get actual values
                final String userId = components[0];
                final SecureString registrationCode = IdpCore.getInstance().getSecureContainerFactory().fromString(components[1]);

                handler.onFinished(true, userId, registrationCode, null);
            } else {
                handler.onFinished(false, null, null, "STRING_QR_CODE_BASIC_FAILED_TO_PARSE");
            }
        } catch (final NumberFormatException exception) {
            // Invalid not hex-string data from qr code.
            handler.onFinished(false, null, null, exception.getMessage());
        }
    }

}
