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

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.thalesgroup.mobileprotector.commonutils.helpers.Lifespan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class ResultFragment extends Fragment {

    //region Declaration

    private TextView mLabelResult;
    private ProgressBar mProgressLifespan;

    //endregion

    //region Life Cycle

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_result, container, false);

        mLabelResult = view.findViewById(R.id.fragment_result_tv_value);
        mProgressLifespan = view.findViewById(R.id.fragment_result_progressbar);

        // Make it invisible by default without any values.
        hide();

        return view;
    }

    //endregion

    //region Public API

    /**
     * Hide all elements and disable all animations
     */
    public void hide() {
        mLabelResult.setVisibility(View.INVISIBLE);
        mProgressLifespan.setVisibility(View.INVISIBLE);
    }

    /**
     * Show value with animated countdown.
     *
     * @param message  Value to display.
     * @param lifespan Lifespan in seconds.
     */
    public void show(
            @NonNull String message,
            @NonNull Lifespan lifespan
    ) {
        mLabelResult.setVisibility(View.VISIBLE);
        mProgressLifespan.setVisibility(View.VISIBLE);

        mLabelResult.setText(message);
        mLabelResult.setAlpha(1.f);
        mProgressLifespan.setAlpha(1.f);
        mProgressLifespan.setMax(lifespan.getMax() * 10);
        mProgressLifespan.setProgress(lifespan.getCurrent() * 10);

        new CountDownTimer(lifespan.getCurrent() * 1000, 100) {
            @Override
            public void onTick(long millisRemaining) {
                mProgressLifespan.setProgress((int) millisRemaining / 100);
            }

            @Override
            public void onFinish() {
                mLabelResult.setAlpha(.5f);
                mProgressLifespan.setProgress(0);
                mProgressLifespan.setAlpha(.5f);
            }
        }.start();
    }

    //endregion
}
