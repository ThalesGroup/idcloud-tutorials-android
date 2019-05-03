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

package com.gemalto.mobileprotector.sample;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.gemalto.gettingstarted.R;
import com.gemalto.mobileprotector.sample.otp.OtpFragment;
import com.gemalto.mobileprotector.sample.otp.OtpLogic;
import com.gemalto.mobileprotector.sample.provisioning.ProvisioningFragment;
import com.gemalto.mobileprotector.sample.setup.SetupLogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base activity which holds all the common logic for all {@code MainActivity} classes in all flavors.
 */
public class BaseActivity extends AppCompatActivity implements MainViewController {

    private final static String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};

    private final static int PERMISSION_GRANTED = 1;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean doesHavePermissions = requestPermission();

        // Setup Mobile Protector SDK.
        SetupLogic.setup(this);

        if (doesHavePermissions) {
            displayFragment();
        }
    }

    /**
     * Requests needed runtime permissions.
     *
     * @return {@code True} if application already has all needed permissions, {@code false} if permissions need to be
     * requested.
     */
    protected boolean requestPermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        final List<String> permissionsToCheck = new ArrayList<>();

        for (final String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    Toast.makeText(this, "Requesting permission - " + permission, Toast.LENGTH_LONG).show();
                }

                permissionsToCheck.add(permission);
            }
        }

        if (!permissionsToCheck.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                                              permissionsToCheck.toArray(new String[permissionsToCheck.size()]),
                                              PERMISSION_GRANTED);
        }

        return permissionsToCheck.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        displayFragment();
    }

    /**
     * Clears the fragment back stack.
     */
    protected void clearFragmentBackStack() {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Checks if there are any tokens enrolled.
     *
     * @return {@code True} if at least one token is enrolled, else {@code false}.
     */
    protected boolean isTokenEnrolled() {
        final Set<String> tokenNames = OtpLogic.getTokenNames();
        return tokenNames != null && !tokenNames.isEmpty();
    }

    /**
     * Displays the correct fragment.
     */
    protected void displayFragment() {
        if (isTokenEnrolled()) {
            showOtpFragment();
        } else {
            showProvisionFragment();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showProvisionFragment() {
        clearFragmentBackStack();

        final Fragment fragment = ProvisioningFragment.newInstance();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showOtpFragment() {
        clearFragmentBackStack();

        final Fragment fragment = OtpFragment.newInstance();
        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
}
