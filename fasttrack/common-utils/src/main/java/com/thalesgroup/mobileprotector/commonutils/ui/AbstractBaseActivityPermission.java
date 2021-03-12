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

package com.thalesgroup.mobileprotector.commonutils.ui;

import android.Manifest;
import android.os.Build;
import android.widget.Toast;

import com.gemalto.commonutils.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public abstract class AbstractBaseActivityPermission extends AppCompatActivity {
    protected enum AppState {
        LOADING,
        PERMISSIONS,
        INITED
    }

    private final static int PERMISSION_REQUEST_CODE = 1;
    protected AppState mAppState = AppState.LOADING;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("deprecation")
    protected boolean checkMandatoryPermissions() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            return requestPermission(Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
                    Manifest.permission.USE_BIOMETRIC);

        return requestPermission(Manifest.permission.CAMERA,
                Manifest.permission.INTERNET,
                Manifest.permission.USE_FINGERPRINT);
    }


    private boolean requestPermission(String... permissions) {
        // Old SDK version does not have dynamic permissions.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        List<String> permissionsToCheck = new ArrayList<>();

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PermissionChecker.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    Toast.makeText(getApplicationContext(), "Requesting permission - " + permission, Toast.LENGTH_LONG).show();
                }

                permissionsToCheck.add(permission);
            }
        }

        if (!permissionsToCheck.isEmpty()) {
            ActivityCompat
                    .requestPermissions(this, permissionsToCheck.toArray(new String[permissionsToCheck.size()]), PERMISSION_REQUEST_CODE);
        }

        return permissionsToCheck.isEmpty();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean allGranted = true;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int index = 0; index < permissions.length; index++) {
                // Check whenever current permission is granted.
                if (grantResults[index] != PermissionChecker.PERMISSION_GRANTED) {
                    allGranted = false;
                    // Make sure that permission is not permanently blocked.
                    if (mAppState == AppState.LOADING && !ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[index])) {
                        mAppState = AppState.PERMISSIONS;
                        setContentView(R.layout.activity_permissions);
                        return;
                    }
                }
            }
        }

        // All requested permissions are granted. We can continue loading application.
        if (allGranted) {
            initApplication();
        }
    }

    protected void initApplication() {
        mAppState = AppState.INITED;
    }

}
