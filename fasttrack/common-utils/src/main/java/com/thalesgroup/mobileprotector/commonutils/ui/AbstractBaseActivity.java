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

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gemalto.commonutils.R;
import com.gemalto.idp.mobile.fasttrack.protector.oath.OathTokenDevice;
import com.thalesgroup.mobileprotector.commonutils.helpers.BaseLogic;
import com.thalesgroup.mobileprotector.commonutils.thread.ExecutionService;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

/**
 * Base activity which holds all the common logic for all {@code MainActivity} classes in all flavors.
 */
public abstract class AbstractBaseActivity extends AbstractBaseActivityPermission {

    private ProgressDialog mProgressDialog = null;
    private String mLastDialogFragmentTag = null;

    //region AbstractBaseActivity

    protected abstract int caption();

    protected abstract int contentViewID();

    protected void setupFragments() { // NOPMD
        // Override
    }

    /**
     * (Optional) Setups the application for the appropriate flavor.
     */
    protected void setup() {}

    /**
     * Called whenever logic / data layer changes to reflect it visually.
     *
     * @return Return currently provisioned token to reuse in sub classes.
     */
    protected @Nullable
    OathTokenDevice updateGui() { // NOPMD
        // Override
        return null;
    }

    /**
     * Common method to display information to user. Currently implemented as simple dialog with app name in caption.
     *
     * @param result Message to be displayed.
     */
    public void displayMessageDialog(String result) {
        // Allow to display simple toast from any threads.
        ExecutionService.getExecutionService()
                .runOnMainUiThread(
                        () -> Toast.makeText(AbstractBaseActivity.this.getApplicationContext(), result, Toast.LENGTH_LONG).show()
                );
    }

    public void displayMessageDialog(int resId) {
        displayMessageDialog(getString(resId));
    }

    /**
     * Display description of error if it's not nil using displayMessageDialog method.
     *
     * @param exception Application error or nil.
     */
    public void displayMessageDialog(Exception exception) {
        displayMessageDialog(exception.getMessage());
    }

    public void dialogFragmentShow(
            DialogFragment dialog,
            String dialogTag,
            boolean fullscreen
    ) {
        // Hide any previous dialogs if exists.
        dialogFragmentHide();

        // If desired make dialog appear in fullscreen.
        if (fullscreen) {
            dialog.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        }

        // Save last tag and display fragment.
        mLastDialogFragmentTag = dialogTag;
        dialog.show(getSupportFragmentManager(), mLastDialogFragmentTag);
    }

    public void dialogFragmentHide() {
        // Hide fragment if exists
        if (mLastDialogFragmentTag != null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(mLastDialogFragmentTag);
            if (fragment instanceof DialogFragment) {
                ((DialogFragment) fragment).dismiss();
            }
            mLastDialogFragmentTag = null; // NOPMD
        }
    }

    public void loadingBarShow(int caption) {
        loadingBarShow(getString(caption));
    }

    public void loadingBarShow(String caption) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(caption);
            mProgressDialog.show();
        } else {
            mProgressDialog.setMessage(caption);
        }
    }

    public void loadingBarHide() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null; // NOPMD
        }
    }

    //endregion

    //region Life Cycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar_layout);
        ((AppCompatTextView) findViewById(R.id.title_app_compat)).setText(caption());

        setContentView(contentViewID());

        BaseLogic.setContext(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();

        // Keep asking for permissions unless we are already on permission screen.
        // That screen will be prompted when user permanently block some permission
        if (mAppState == AppState.LOADING && checkMandatoryPermissions()) {
            initApplication();
        }
    }

    @Override
    protected void initApplication() {
        super.initApplication();

        setup();
        setupFragments();
        updateGui();
    }

    //endregion

    //region User Interface

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onButtonPressedPermissions(View view) {
        if (checkMandatoryPermissions()) {
            initApplication();
        }
    }

    protected void hideKeyboard(View view) {
        if (view == null) return;

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    //endregion
}
