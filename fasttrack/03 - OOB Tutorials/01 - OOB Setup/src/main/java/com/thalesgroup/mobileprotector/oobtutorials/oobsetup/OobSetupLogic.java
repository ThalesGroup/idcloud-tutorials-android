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

import com.gemalto.idp.mobile.fasttrack.FastTrack;
import com.gemalto.idp.mobile.fasttrack.messenger.MobileMessenger;
import com.thalesgroup.mobileprotector.tutorials.transactionsign.TransactionSignLogic;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implementation of OOB registration and notification updates.
 */
public class OobSetupLogic extends TransactionSignLogic {

    private static MobileMessenger mobileMessenger;

    /**
     * Initializes {@link MobileMessenger} with parameters from {@link OobSetupConfig}, provided to application
     * developers.
     *
     * @return Returns {@link MobileMessenger} object.
     */
    public static MobileMessenger getMobileMessenger() {
        if (mobileMessenger == null) {
            try {
                MobileMessenger.Builder builder = FastTrack.getInstance().getMobileMessengerBuilder(
                        new URL(OobSetupConfig.getOobRegistrationUrl()),
                        OobSetupConfig.getOobDomain(),
                        OobSetupConfig.getOobAppId(),
                        OobSetupConfig.getOobRsaKeyExponent(),
                        OobSetupConfig.getOobRsaKeyModulus()
                );

                builder.withDeviceFingerprintSource(OobSetupConfig.getOobFingerprintSource())
                        .withTlsConfiguration(OobSetupConfig.getOobTLSConfiguration())
                        .withMessengerRootPolicy(OobSetupConfig.getMessengerRootPolicy());

                mobileMessenger = builder.build();
            } catch (MalformedURLException ex) {
                throw new IllegalStateException(ex);
            }
        }

        return mobileMessenger;
    }

}
