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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gemalto.a03_otp.R;
import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.core.IdpCore;
import com.gemalto.idp.mobile.core.IdpException;
import com.gemalto.idp.mobile.core.util.SecureString;
import com.gemalto.idp.mobile.otp.oath.soft.SoftOathToken;
import com.gemalto.mobileprotector.sample.MainViewController;

import java.util.Set;

/**
 * Provisioning fragment.
 */
public class OtpFragment extends Fragment {

    private MainViewController mMainViewController;

    /**
     * Creates a new {@code ProvisioningFragment}.
     *
     * @return {@code ProvisioningFragment}.
     */
    public static OtpFragment newInstance() {
        return new OtpFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_otp, container, false);

        final TextView otpTextView = view.findViewById(R.id.tv_otp_id);
        final ImageButton generateButton = view.findViewById(R.id.btn_generate_otp);
        final Button removeButton = view.findViewById(R.id.btn_remove_token);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                OtpLogic.getUserPin((AppCompatActivity) getActivity(), new OtpPinCallback() {
                    @Override
                    public void onPinSuccess(@NonNull final PinAuthInput pin) {
                        otpTextView.setText(getOtp(pin).toString());
                    }

                    @Override
                    public void onPinError(@NonNull final String errorMessage) {
                        Toast.makeText(getActivity(), "Error during get user PIN: " + errorMessage, Toast.LENGTH_LONG)
                             .show();
                    }
                });
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Set<String> tokenNames = OtpLogic.getTokenNames();
                if (tokenNames != null && !tokenNames.isEmpty()) {
                    try {
                        if (OtpLogic.removeToken(tokenNames.iterator().next())) {
                            mMainViewController.showProvisionFragment();
                        } else {
                            Toast.makeText(getActivity(), "Error during remove token ", Toast.LENGTH_LONG).show();
                        }
                    } catch (final IdpException exception) {
                        Toast.makeText(getActivity(),
                                       "Error during remove token " + exception.getMessage(),
                                       Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        mMainViewController = (MainViewController) context;
    }

    /**
     * Generates an OTP.
     *
     * @param pin
     *         User authentication.
     * @return Generated OTP.
     */
    private SecureString getOtp(@NonNull final PinAuthInput pin) {
        final Set<String> tokenNames = OtpLogic.getTokenNames();
        if (tokenNames != null && !tokenNames.isEmpty()) {
            final String tokenName = tokenNames.iterator().next();
            try {
                final SoftOathToken token = OtpLogic.getToken(tokenName);
                return OtpLogic.generateOtp(token, pin);
            } catch (IdpException exception) {
                Toast.makeText(getActivity(),
                               "Domain: " + exception.getDomain() + " Code: " + exception.getCode() + " Message: "
                               + exception.getMessage(),
                               Toast.LENGTH_LONG).show();
            }
        }

        return IdpCore.getInstance().getSecureContainerFactory().fromString("");
    }
}
