/* -----------------------------------------------------------------------------
 *
 *     Copyright (c) 2016  -  GEMALTO DEVELOPMENT - R&D
 *
 * -----------------------------------------------------------------------------
 * GEMALTO MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. GEMALTO SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * THIS SOFTWARE IS NOT DESIGNED OR INTENDED FOR USE OR RESALE AS ON-LINE
 * CONTROL EQUIPMENT IN HAZARDOUS ENVIRONMENTS REQUIRING FAIL-SAFE
 * PERFORMANCE, SUCH AS IN THE OPERATION OF NUCLEAR FACILITIES, AIRCRAFT
 * NAVIGATION OR COMMUNICATION SYSTEMS, AIR TRAFFIC CONTROL, DIRECT LIFE
 * SUPPORT MACHINES, OR WEAPONS SYSTEMS, IN WHICH THE FAILURE OF THE
 * SOFTWARE COULD LEAD DIRECTLY TO DEATH, PERSONAL INJURY, OR SEVERE
 * PHYSICAL OR ENVIRONMENTAL DAMAGE ("HIGH RISK ACTIVITIES"). GEMALTO
 * SPECIFICALLY DISCLAIMS ANY EXPRESS OR IMPLIED WARRANTY OF FITNESS FOR
 * HIGH RISK ACTIVITIES.
 *
 * -----------------------------------------------------------------------------
 */
package com.gemalto.idp.mobile.authentication.mode.face.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gemalto.idp.mobile.authentication.AuthInput;
import com.gemalto.idp.mobile.authentication.Authenticatable;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthFrameEvent;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthInput;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthStatus;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthVerifierCallback;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.ErrorDialogFragment;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.ErrorMode;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.view.CircularProgressView;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.view.FaceMaskView;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.manager.FaceVerifierUIListener;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.manager.FaceVerifyManager;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.utils.logs.MyLog;
import com.gemalto.idp.mobile.authentication.mode.face.view.FaceView;
import com.gemalto.idp.mobile.core.IdpException;

/**
 * Activity of verification
 */
public class VerifyFragment extends DialogFragment implements ErrorDialogFragment.ErrorDialogFragmentListener {
    private static final String TAG = VerifyFragment.class.getSimpleName();

    public static final String EXTRA_TIMEOUT = "EXTRA_TIMEOUT";
    public static final String RETRIES = "RETRIES";

    private static final int TIMEOUT = 60000;
    private static boolean displayErrorDialog = false;

    private VerificationCallback m_callback = null;
    private Authenticatable authenticatable = null;
    private AuthInput authInput = null;

    private FaceView mFaceView;
    private CircularProgressView mProgressStepView;
    private FaceMaskView mFaceMaskView;
    private ImageView mIvRegistredUser;
    private RelativeLayout mLayoutRegistredUser;
    private TextView mTvLivenssActionPrompt;
    private Button mBtnCancel;
    private Button mBtnSuccess;

    private int m_resultModeTempo;

    private static int m_nbRetries = 0;
    private static int MAX_RETRY = 5;
    private boolean shouldStop = false;

    public static VerifyFragment newInstance(int timeout, int retries) {
        VerifyFragment fragment = new VerifyFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_TIMEOUT, timeout);
        args.putInt(RETRIES, retries);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_resultModeTempo = 2000;
        m_resultModeTempo = getArguments().getInt(EXTRA_TIMEOUT, 2000);
        MAX_RETRY = getArguments().getInt(RETRIES, 5);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_verify, container, false);

        mFaceView = (FaceView) rootView.findViewById(R.id.nFaceView);
        mProgressStepView = (CircularProgressView) rootView.findViewById(R.id.circularProgressView);

        mFaceMaskView = (FaceMaskView) rootView.findViewById(R.id.faceMaskView);
        mIvRegistredUser = (ImageView) rootView.findViewById(R.id.imageViewRegistredUser);
        mLayoutRegistredUser = (RelativeLayout) rootView.findViewById(R.id.layoutRegistredUser);


        mBtnCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        mBtnCancel.setEnabled(false);
        mBtnCancel.setOnClickListener(v -> {
            shouldStop = true;
            if (m_callback != null) {
                m_callback.onCancel();
            }
        });

        mBtnSuccess = (Button) rootView.findViewById(R.id.buttonSuccess);
        mBtnSuccess.setOnClickListener(v -> {
            shouldStop = true;
            if (m_callback != null) {
                m_callback.onVerificationSuccess(authInput);
            }
        });

        mTvLivenssActionPrompt = (TextView) rootView.findViewById(R.id.tvBlink);

        return rootView;
    }

    @Override
    public void onPause() {
        MyLog.i(TAG, "onPause");
        FaceVerifyManager.getInstance().cancel();
        m_callback = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        MyLog.i(TAG, "onResume");
        m_nbRetries = 0;

        runVerify();
        super.onResume();
    }

    public void setVerificationCallback(VerificationCallback callback) {
        this.m_callback = callback;
    }

    // Set the targeted authenticatable
    public void setAuthenticatable(Authenticatable authenticatable) {
        this.authenticatable = authenticatable;
    }

    private void runVerify() {
        MyLog.i(TAG, "runVerify");

        shouldStop = false;

        mProgressStepView.setProgress(false, ErrorMode.NONE);
        mProgressStepView.setSurroundMode(true, ErrorMode.DISABLED);

        mFaceView.setVisibility(View.VISIBLE);
        mLayoutRegistredUser.setVisibility(View.GONE);
        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnSuccess.setVisibility(View.GONE);
        mTvLivenssActionPrompt.setVisibility(View.GONE);
        mFaceMaskView.setMaskMode(FaceMaskView.MaskMode.BOTTOM);
        mFaceMaskView.setVisibility(View.GONE);

        if (m_nbRetries >= MAX_RETRY) {
            return;
        }

        FaceManager.getInstance().load(getActivity());

        final FaceVerifierUIListener verifierUIListener = setUpFaceVerifierUIListener();
        final FaceAuthVerifierCallback callback = setUpFaceVerifierCallback(verifierUIListener);

        FaceVerifyManager.getInstance().startAuth(authenticatable, verifierUIListener, callback, TIMEOUT);
    }

    private FaceVerifierUIListener setUpFaceVerifierUIListener() {
        FaceVerifierUIListener listener = new FaceVerifierUIListener() {
            @Override
            public void onNewFrame(FaceAuthFrameEvent frameEvent) {
                mBtnCancel.setEnabled(true);
                mFaceView.setFaceFrameEvent(frameEvent);
            }

            @Override
            public void onStepChanged(int step, ErrorMode errorMode, ErrorMode surroudMode) {
                final int stepF = step;

                MyLog.d(TAG, "onStepChanged: step=" + stepF);

                if (stepF < FaceVerifyManager.STEP_PROCESSING) {
                    mProgressStepView.setProgress(false, ErrorMode.NONE);
                }

                if (stepF == FaceVerifyManager.STEP_WAIT_FACE) {
                    mTvLivenssActionPrompt.setVisibility(View.GONE);
                } else if (stepF == FaceVerifyManager.STEP_KEEP_STILL) {
                    mTvLivenssActionPrompt.setVisibility(View.VISIBLE);
                    mTvLivenssActionPrompt.setText(R.string.verification_keepstill);
                } else if (stepF == FaceVerifyManager.STEP_BLINK) { //Blink
                    mTvLivenssActionPrompt.setText(R.string.verification_blinkLabel);
                    mTvLivenssActionPrompt.setVisibility(View.VISIBLE);
                } else if (stepF == FaceVerifyManager.STEP_PROCESSING) { // Processing Verify
                    mTvLivenssActionPrompt.setVisibility(View.VISIBLE);
                    mTvLivenssActionPrompt.setText(R.string.verification_process);
                    mProgressStepView.setSurroundMode(true, ErrorMode.NONE);
                    mProgressStepView.setProgress(true, ErrorMode.NONE);

                } else if (stepF == FaceVerifyManager.STEP_PROCESSED) { // SUCCESS
                    mTvLivenssActionPrompt.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFacePositionChanged(int step, boolean faceDetected) {
                final int stepF = step;
                final boolean bFaceDetectedF = faceDetected;

                MyLog.d(TAG, "onFacePositionChanged: face=" + bFaceDetectedF);//+" maskMode="+maskModeF
                if (stepF < FaceVerifyManager.STEP_PROCESSING) {
                    mProgressStepView.setSurroundMode(true, bFaceDetectedF ? ErrorMode.NONE : ErrorMode.ERROR);
                }
                if (stepF == FaceVerifyManager.STEP_BLINK || stepF == FaceVerifyManager.STEP_BLINK_STILL) {
                    mTvLivenssActionPrompt.setText(bFaceDetectedF ? R.string.verification_blinkLabel : R.string.verification_faceOut);
                }
            }
        };

        return listener;
    }

    private FaceAuthVerifierCallback setUpFaceVerifierCallback(final FaceVerifierUIListener verifierUIListener) {
        final FaceAuthVerifierCallback callback = new FaceAuthVerifierCallback() {

            @Override
            public void onVerifyFinish(FaceAuthInput auth) {

                FaceVerifyManager.getInstance().cancel();
                authInput = auth;
                onAuthSuccess(FaceVerifyManager.getInstance().getLoggedUserImage());
            }

            @Override
            public void onVerifyFail(FaceAuthStatus faceAuthStatus) {
                MyLog.i(TAG, "result= " + faceAuthStatus + " retries=" + m_nbRetries);
                m_nbRetries++;

                mProgressStepView.setProgress(false, ErrorMode.NONE);

                if (m_callback != null) {
                    ErrorDialogFragment.newInstance(VerifyFragment.this,
                            getActivity().getString(FaceMessagesHelper.getErrorMessageForErrorCode(faceAuthStatus)))
                            .showAllowingStateLoss(getActivity().getSupportFragmentManager(), "error");
                }

                if (m_nbRetries >= MAX_RETRY) {
                    FaceVerifyManager.getInstance().cleanAuth();
                    if (m_callback != null) {
                        m_callback.onVerificationFailed(faceAuthStatus);
                    }
                } else if (shouldStop) {
                    FaceVerifyManager.getInstance().cleanAuth();
                } else {
                    MyLog.d(TAG, "Face verification failed : Remaining retries: " + (MAX_RETRY - m_nbRetries));
                    FaceVerifyManager.getInstance().cleanAuth();
                    if (m_callback != null) {
                        m_callback.onVerificationRetry(faceAuthStatus, MAX_RETRY - m_nbRetries);
                    }
                    // Uncomment this if you wish to disable to error dialog and auto-retry
                    //FaceVerifyManager.getInstance().startAuth(authenticatable, verifierUIListener, this, TIMEOUT);
                }
            }

            @Override
            public void onVerifyError(IdpException e) {
                FaceVerifyManager.getInstance().cancel();
                if (m_callback != null) {
                    m_callback.onError(e);
                }
            }
        };

        return callback;
    }

    protected void onAuthSuccess(Bitmap image) {
        MyLog.i(TAG, "onAuthSuccess");

        mLayoutRegistredUser.setVisibility(View.VISIBLE);
        mFaceView.setVisibility(View.GONE);

        // Uncomment to display a round face image instead of placeholder
        //if(image != null) {
        //    Bitmap cover = ImageShapeTool.getRoundedCroppedBitmap(image, image.getWidth());
        //    mIvRegistredUser.setImageBitmap(cover);
        //}
        //else {
        // Always use the avatar for now as requested
        mIvRegistredUser.setImageResource(R.drawable.face_demo);
        //}

        mBtnCancel.setVisibility(View.GONE);

        if (m_resultModeTempo >= 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (m_callback != null) {
                    m_callback.onVerificationSuccess(authInput);
                }
            }, m_resultModeTempo);
        } else {
            mBtnSuccess.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Error Dialog Ok Listener
     */
    @Override
    public void onDialogOk() {
        MyLog.d(TAG, "onDialogOk");
        if (m_callback != null && !shouldStop) {
            runVerify();
        }
    }
}
