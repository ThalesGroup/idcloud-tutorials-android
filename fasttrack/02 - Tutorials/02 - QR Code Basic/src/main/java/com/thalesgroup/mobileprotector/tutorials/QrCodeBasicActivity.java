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

package com.thalesgroup.mobileprotector.tutorials;

import com.thalesgroup.mobileprotector.tutorials.changepinbasic.QrCodeBasicLogic;
import com.thalesgroup.mobileprotector.tutorials.qrcodebasic.R;
import com.thalesgroup.mobileprotector.uicomponents.QrCodeReaderFragment;
import com.thalesgroup.mobileprotector.uicomponents.QrCodeReaderFragmentDelegate;

/**
 * Main entry point of the application for current flavor.
 */
public class QrCodeBasicActivity extends AdvancedSetupActivity implements QrCodeReaderFragmentDelegate {

    //region Declaration

    private static final String DIALOG_TAG_QR_CODE_READER = "DIALOG_TAG_QR_CODE_READER";

    //endregion

    //region AbstractBaseActivity


    @Override
    protected int contentViewID() {
        return R.layout.activity_qrcodebasic;
    }

    @Override
    protected int caption() {
        return R.string.tutorials_qr_code_basic_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Add QR Code provision button since this module.
        setQrCodeButtonVisible();
    }

    //endregion

    //region ProvisioningFragmentDelegate

    @Override
    public void onProvisionUsingQr() {
        final QrCodeReaderFragment qrCodeReaderFragment = new QrCodeReaderFragment();
        qrCodeReaderFragment.setDelegate(this);

        dialogFragmentShow(qrCodeReaderFragment, DIALOG_TAG_QR_CODE_READER, true);
    }

    //endregion

    //region QrCodeReaderFragmentDelegate

    @Override
    public void onQRCodeProvided(final String qrCode) {
        // Hide reader.
        dialogFragmentHide();

        // Parse provided QR data to get user id and registration code.
        QrCodeBasicLogic.parseQRCode(qrCode, (successful, userId, regCode, error) -> {
            // QR Data was successfully parsed. Continue with provision process as usual.
            if (successful) {
                onProvision(userId, regCode);
            } else {
                displayMessageDialog(error);
            }
        });
    }

    //endregion

}
