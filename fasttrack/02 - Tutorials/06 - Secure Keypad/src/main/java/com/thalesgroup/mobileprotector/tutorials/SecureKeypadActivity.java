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

import android.widget.ImageButton;

import com.gemalto.idp.mobile.authentication.mode.pin.PinAuthInput;
import com.gemalto.idp.mobile.ui.UiModule;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputBuilder;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputService;
import com.gemalto.idp.mobile.ui.secureinput.SecureInputUi;
import com.gemalto.idp.mobile.ui.secureinput.SecureKeypadListener;
import com.thalesgroup.mobileprotector.tutorials.securekeypad.R;

import java.util.Arrays;
import java.util.List;

/**
 * Main entry point of the application for current flavor.
 */
public class SecureKeypadActivity extends InBandVerificationActivity {

    //region Declaration

    private static final String DIALOG_TAG_KEYPAD_VARIANT_01 = "DIALOG_TAG_KEYPAD_VARIANT_01";
    private static final String DIALOG_TAG_KEYPAD_VARIANT_02 = "DIALOG_TAG_KEYPAD_VARIANT_02";
    private static final String DIALOG_TAG_KEYPAD_VARIANT_03 = "DIALOG_TAG_KEYPAD_VARIANT_03";
    private static final String DIALOG_TAG_KEYPAD_VARIANT_04 = "DIALOG_TAG_KEYPAD_VARIANT_04";
    private SecureInputBuilder builder;

    //endregion

    //region AbstractBaseActivity


    @Override
    protected int contentViewID() {
        return R.layout.activity_securekeypad;
    }

    @Override
    protected int caption() {
        return R.string.tutorials_secure_keypad_caption;
    }

    @Override
    protected void setupFragments() {
        super.setupFragments();

        // Assign all module specific UI element.
        ImageButton btnVariant = findViewById(R.id.activity_securekeypad_btn_variant_01);
        if (btnVariant != null) {
            btnVariant.setOnClickListener(sender -> onButtonPressedVariant01());
        }

        btnVariant = findViewById(R.id.activity_securekeypad_btn_variant_02);
        if (btnVariant != null) {
            btnVariant.setOnClickListener(sender -> onButtonPressedVariant02());
        }

        btnVariant = findViewById(R.id.activity_securekeypad_btn_variant_03);
        if (btnVariant != null) {
            btnVariant.setOnClickListener(sender -> onButtonPressedVariant03());
        }

        btnVariant = findViewById(R.id.activity_securekeypad_btn_variant_04);
        if (btnVariant != null) {
            btnVariant.setOnClickListener(sender -> onButtonPressedVariant04());
        }

    }

    //endregion

    //region Private Helpers

    private SecureKeypadListener commonHandler() {
        return new SecureKeypadListener() {
            @Override
            public void onKeyPressedCountChanged(int count, int inputField) {
                // Handle on key pressed.
            }

            @Override
            public void onInputFieldSelected(int inputField) {
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
            public void onFinish(PinAuthInput pinAuthInput, PinAuthInput pinAuthInput1) {
                dialogFragmentHide();
            }

            @Override
            public void onError(String errorMessage) {
                dialogFragmentHide();

                // Notify user about possible error.
                displayMessageDialog(errorMessage);
            }
        };
    }

    //endregion

    //region User Interface

    private void onButtonPressedVariant01() {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();

        builder.setScreenBackgroundColor(getResources().getColor(R.color.skcScreenBackgroundColor));
        builder.setOkButtonText(" ");
        builder.setDeleteButtonText(" ");
        builder.setDistanceBetweenKeyAndSubscript(2);
        builder.setKeypadGridGradientColors(
                getResources().getColor(R.color.skcKeypadGridGradientColorStart_01),
                getResources().getColor(R.color.skcKeypadGridGradientColorEnd_01));
        builder.setButtonPressVisibility(true);
        builder.setOkButtonImage(R.drawable.outline_done_outline_black_48);
        builder.setOkButtonImageOpacity(SecureInputBuilder.UiControlState.NORMAL, 0.75f);
        builder.setOkButtonImageOpacity(SecureInputBuilder.UiControlState.SELECTED, 0.5f);
        builder.setOkButtonImageOpacity(SecureInputBuilder.UiControlState.DISABLED, 0.25f);
        builder.setDeleteButtonImage(R.drawable.outline_delete_outline_black_48);
        builder.setDeleteButtonImageOpacity(SecureInputBuilder.UiControlState.NORMAL, 0.75f);
        builder.setDeleteButtonImageOpacity(SecureInputBuilder.UiControlState.SELECTED, 0.5f);
        builder.setDeleteButtonImageOpacity(SecureInputBuilder.UiControlState.DISABLED, 0.25f);
        builder.setButtonGradientColor(SecureInputBuilder.UiControlState.NORMAL,
                getResources().getColor(R.color.skcButtonGradientColorNormalStart),
                getResources().getColor(R.color.skcButtonGradientColorNormalEnd));
        builder.setButtonGradientColor(SecureInputBuilder.UiControlState.SELECTED,
                getResources().getColor(R.color.skcButtonGradientColorSelectedStart),
                getResources().getColor(R.color.skcButtonGradientColorSelectedEnd));

        SecureInputUi secureInputUi = builder.buildKeypad(false, false, false, commonHandler());

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_KEYPAD_VARIANT_01, false);
    }

    private void onButtonPressedVariant02() {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();

        builder.setKeypadMatrix(4, 4);
        builder.setKeypadHeightRatio(0.25f);
        builder.setButtonBorderWidth(3);
        builder.setKeypadFrameColor(getResources().getColor(R.color.skcKeypadFrameColor));
        builder.setKeypadGridGradientColors(
                getResources().getColor(R.color.skcKeypadGridGradientColorStart_02),
                getResources().getColor(R.color.skcKeypadGridGradientColorEnd_02));
        SecureInputUi secureInputUi = builder.buildKeypad(true, true, false, commonHandler());

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_KEYPAD_VARIANT_02, false);
    }

    private void onButtonPressedVariant03() {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();

        builder.setFirstLabel("Old Pin");
        builder.setSecondLabel("New Pin");
        builder.setLabelColor(getResources().getColor(R.color.skcLabelColor));
        builder.setLabelFontSize(30);
        builder.setLabelAlignment(SecureInputBuilder.LabelAlignment.LEFT);
        builder.setInputFieldBackgroundColor(SecureInputBuilder.UiControlFocusState.FOCUSED, getResources().getColor(R.color.skcInputFieldBackgroundColorFocused));
        SecureInputUi secureInputUi = builder.buildKeypad(false, true, false, commonHandler());

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_KEYPAD_VARIANT_03, false);
    }

    private void onButtonPressedVariant04() {
        builder = SecureInputService.create(UiModule.create()).getSecureInputBuilder();

        builder.setFirstLabel("Enter Password");
        builder.setMaximumAndMinimumInputLength(16, 6);
        builder.setKeypadMatrix(6, 6);
        List<Character> mainKeys = Arrays.asList(
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        );
        builder.setKeys(mainKeys, null);
        builder.setIsDeleteButtonAlwaysEnabled(true);
        SecureInputUi secureInputUi = builder.buildKeypad(true, false, false, commonHandler());

        // Display dialog using common method.
        dialogFragmentShow(secureInputUi.getDialogFragment(), DIALOG_TAG_KEYPAD_VARIANT_04, false);
    }

    //endregion

}
