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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gemalto.a02_provisioning.R;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.oath.OathToken;
import com.gemalto.mobileprotector.sample.MainViewController;
import com.gemalto.mobileprotector.sample.util.storage.AppStorage;

/**
 * Provisioning fragment.
 */
public class ProvisioningFragment extends Fragment {

    private MainViewController mMainViewController;

    /**
     * Creates a new {@code ProvisioningFragment}.
     *
     * @return {@code ProvisioningFragment}.
     */
    public static ProvisioningFragment newInstance() {
        return new ProvisioningFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_provisioning, container, false);

        final EditText userId = view.findViewById(R.id.et_user_id);
        final EditText regCode = view.findViewById(R.id.et_reg_code);
        final Button provisionButton = view.findViewById(R.id.btn_provision);

        provisionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (userId.getText().toString().isEmpty() || regCode.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "User ID and Registration Code cannot be null.", Toast.LENGTH_LONG)
                         .show();
                    return;
                }

                final SecureString regCodeSecureString = IdpCore.getInstance().getSecureContainerFactory()
                                                                .fromString(regCode.getText().toString());
                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.show();
                ProvisioningLogic
                        .provision(userId.getText().toString(), regCodeSecureString, new ProvisioningCallback() {
                            @Override
                            public void onProvisioningSuccess(@NonNull final OathToken token) {
                                AppStorage.saveUserId(getActivity(), userId.getText().toString());
                                progressDialog.dismiss();
                                mMainViewController.showOtpFragment();

                                Toast.makeText(getActivity(), "Provisioning successful", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onProvisioningError(@NonNull final IdpException exception) {
                                Toast.makeText(getActivity(),
                                               "Domain: " + exception.getDomain() + " Code: " + exception.getCode()
                                               + " Message: " + exception.getMessage(),
                                               Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });
            }
        });

        return view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        mMainViewController = (MainViewController) context;
    }
}
