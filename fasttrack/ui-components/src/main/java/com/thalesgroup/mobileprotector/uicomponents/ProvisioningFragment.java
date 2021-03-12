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

package com.thalesgroup.mobileprotector.uicomponents;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.ui.AbstractBaseFragment;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

/**
 * Provisioning fragment.
 */
public class ProvisioningFragment extends AbstractBaseFragment {

    //region Declaration

    private ProvisioningFragmentDelegate mDelegate;

    private EditText mEditUserId;
    private EditText mEditRegCode;

    private Button mBtnProvision;
    private Button mBtnRemoveToken;
    private Button mBtnProvisionUsingQr;

    //endregion

    //region Public API

    /**
     * Display "Provision using Qr code" button.
     */
    public void setQrCodeButtonVisible() {
        mBtnProvisionUsingQr.setVisibility(View.VISIBLE);
    }

    public void setDelegate(final ProvisioningFragmentDelegate delegate) {
        mDelegate = delegate;
    }

    //endregion

    //region Life Cycle

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_provisioning, container, false);

        mEditUserId = view.findViewById(R.id.fragment_provisioning_et_user_id);
        mEditRegCode = view.findViewById(R.id.fragment_provisioning_et_reg_code);

        mBtnProvision = view.findViewById(R.id.fragment_provisioning_btn_provision);
        mBtnProvision.setOnClickListener(view1 -> onButtonPressedProvision());

        mBtnProvisionUsingQr = view.findViewById(R.id.fragment_provisioning_btn_provision_using_qr);
        mBtnProvisionUsingQr.setOnClickListener(view12 -> onButtonPressedProvisionUsingQr());
        mBtnProvisionUsingQr.setVisibility(View.GONE);

        mBtnRemoveToken = view.findViewById(R.id.fragment_provisioning_btn_remove_token);
        mBtnRemoveToken.setOnClickListener(view13 -> onButtonPressedRemoveToken());

        return view;
    }

    //endregion

    //region AbstractBaseFragment

    @Override
    public void updateGui(@Nullable OathTokenDevice token) {
        boolean isTokenProvisioned = token != null;

        if (isTokenProvisioned) {
            mEditUserId.setText("------");
            mEditRegCode.setText("------");
        } else {
            mEditUserId.setText("");
            mEditRegCode.setText("");
        }

        mEditUserId.setEnabled(!isTokenProvisioned);
        mEditRegCode.setEnabled(!isTokenProvisioned);

        mBtnProvision.setEnabled(!isTokenProvisioned);
        mBtnProvisionUsingQr.setEnabled(!isTokenProvisioned);
        mBtnRemoveToken.setEnabled(isTokenProvisioned);
    }

    //endregion

    //region User Interface

    private void onButtonPressedRemoveToken() {
        Context context = getContext();
        assert context != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.remove_token_question);
        builder.setPositiveButton(R.string.remove_token_question_confirm, (dialog, id) -> {
            dialog.dismiss();
            if (mDelegate != null) {
                mDelegate.onRemoveToken();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, id) -> dialog.dismiss());

        builder.create().show();
    }

    private void onButtonPressedProvision() {
        hideKeyboard(mEditRegCode);

        String userId = mEditUserId.getText().toString();
        String regCode = mEditRegCode.getText().toString();

        if (userId.isEmpty() || regCode.isEmpty()) {
            Toast.makeText(getActivity(), R.string.empty_user_id_or_reg_code, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (mDelegate != null) {
            mDelegate.onProvision(userId, regCode);
        }
    }

    private void onButtonPressedProvisionUsingQr() {
        if (mDelegate != null) {
            mDelegate.onProvisionUsingQr();
        }
    }

    //endregion
}
