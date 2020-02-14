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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthEnrollerCallback;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthFrameEvent;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthStatus;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.ErrorDialogFragment;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.ErrorMode;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.view.CircularProgressView;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.view.FaceMaskView.MaskMode;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.manager.FaceEnrollNoStepsManager;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.manager.FaceEnrollerUIListener;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.utils.logs.MyLog;
import com.gemalto.idp.mobile.authentication.mode.face.view.FaceView;
import com.gemalto.idp.mobile.core.IdpException;

/**
 * The new Simple mode Enroll Activity
 */
public class EnrollFragment extends DialogFragment implements ErrorDialogFragment.ErrorDialogFragmentListener {
    private static final String TAG = EnrollFragment.class.getSimpleName();

    public static final String EXTRA_TIMEOUT = "EXTRA_TIMEOUT";
    public static final String RETRIES = "RETRIES";

    private static final int TIMEOUT = 60000;
    private static boolean displayErrorDialog = false;

    private static final int TIME_MIN_PROCESSING = 2000;
    private int m_resultModeTempo;

    private EnrollmentCallback m_callback;

    private boolean m_bCanceled = false;

    private FaceView mFaceView;
    private CircularProgressView mProgressStepView;
    private ImageView mIvRegistredUser;
    private RelativeLayout mLayoutRegistredUser;
    private TextView mTvInstructions;

    private Button mBtnCancel, mBtnStart, mBtnSuccess;

    private static int m_nbRetries = 0;
    private static int MAX_RETRY = 5;
    private boolean shouldStop = false;

    protected static EnrollFragment newInstance(int timeout, int retries){
        EnrollFragment fragment = new EnrollFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){
        View rootView = inflater.inflate(R.layout.activity_enroll, container, false);

        // FaceView is provided by the Ezio SDK
        mFaceView = (FaceView) rootView.findViewById(R.id.nFaceView);

        // Animated circular progress view to show the progress, provided by UI SDK
        mProgressStepView = (CircularProgressView) rootView.findViewById(R.id.stepProgressView);

        mIvRegistredUser = (ImageView) rootView.findViewById(R.id.imageViewRegistredUser);
        mLayoutRegistredUser = (RelativeLayout) rootView.findViewById(R.id.layoutRegistredUser);
        mTvInstructions = (TextView) rootView.findViewById(R.id.textViewInstruction);

        /**
         * Setup buttons
         */
        mBtnCancel = (Button) rootView.findViewById(R.id.buttonCancel);
        mBtnCancel.setEnabled(false);
        mBtnCancel.setOnClickListener(v -> {
            m_bCanceled = true;
            shouldStop = true;
            if(m_callback!=null) {
                m_callback.onCancel();
            }
        });
        
        mBtnStart = (Button) rootView.findViewById(R.id.buttonStart);
        mBtnStart.setEnabled(false);
        mBtnStart.setOnClickListener(v -> {
            mBtnStart.setVisibility(View.GONE);
            FaceEnrollNoStepsManager.getInstance().startEnrollValidated();
        });

        mBtnSuccess = (Button) rootView.findViewById(R.id.buttonSuccess);
        mBtnSuccess.setOnClickListener(v -> {
            shouldStop = true;
            if(m_callback!=null) {
                m_callback.onEnrollmentSuccess();
            }
        });

        return rootView;
    }

    @Override
    public void onPause() {
        MyLog.i(TAG, "onPause");
        FaceEnrollNoStepsManager.getInstance().cancel();
        m_callback = null;
        super.onPause();
    }

    @Override
    public void onResume() {
        MyLog.i(TAG, "onResume");
        m_nbRetries = 0;
        // Start the enrollment when the fragment is attached to a frameLayout
        // and becomes visible
        runEnroll();
        super.onResume();
    }

    // ===========================================================
    //  Set Callback to the App that uses the UI SDK
    // ===========================================================
    public void setEnrollmentCallback(EnrollmentCallback callback){
        this.m_callback = callback;
    }

    // Start the enrolment UI and camera
	private void runEnroll() {
        m_bCanceled = false;
        shouldStop = false;
		MyLog.i(TAG, "runEnroll");
		mProgressStepView.setProgress(false, ErrorMode.NONE);
        mProgressStepView.setSurroundMode(true, ErrorMode.DISABLED);
        mTvInstructions.setVisibility(View.INVISIBLE);

        mFaceView.setVisibility(View.VISIBLE);
        mLayoutRegistredUser.setVisibility(View.GONE);

        mBtnCancel.setVisibility(View.VISIBLE);
        mBtnStart.setVisibility(View.VISIBLE);
        mBtnSuccess.setVisibility(View.GONE);

        if(m_nbRetries >= MAX_RETRY) {
            return;
        }

        FaceManager.getInstance().load(getActivity());

        final FaceEnrollerUIListener listener = setUpFaceEnrollerUIListener();
        final FaceAuthEnrollerCallback callback = setUpAuthEnrollerCallback(listener);

        FaceEnrollNoStepsManager.getInstance().startEnrollment(listener, callback, TIMEOUT, TIME_MIN_PROCESSING);
	}

    // Set up the listener for UI updates
    private FaceEnrollerUIListener setUpFaceEnrollerUIListener(){
        FaceEnrollerUIListener listener = new FaceEnrollerUIListener() {
            // Private Frame Event listener called from the Enroll Manager
            @Override
            public void onNewFrame(FaceAuthFrameEvent frameEvent) {
                MyLog.v(TAG, "onNewFrame");
                mBtnCancel.setEnabled(true);
                mFaceView.setFaceFrameEvent(frameEvent);
            }

            // Private step Changed Event listener called from the Enroll Manager
            @Override
            public void onStepChanged(int step, ErrorMode errorMode, MaskMode maskMode, boolean bSurroundMode) {
                final int stepF = step;
                final MaskMode maskModeF = maskMode;

                MyLog.d(TAG, "onStepChanged: step="+stepF+" maskMode="+maskModeF);
                if(stepF == FaceEnrollNoStepsManager.STEP_WAIT_FACE) { // Wait Face (Start Pressed)
                    mProgressStepView.setProgress(true, ErrorMode.NONE);
                }
                else if(stepF == FaceEnrollNoStepsManager.STEP_PROCESSING) { // Processing
                    mProgressStepView.setSurroundMode(true, ErrorMode.DISABLED);
                }
            }

            // Private face detection status called from the Enroll Manager
            @Override
            public void onFacePositionChanged(int step, ErrorMode faceMode) {
                final int stepF = step;
                final ErrorMode faceModeF = faceMode;

                MyLog.d(TAG, "onFacePositionChanged: step="+stepF+" lastFaceMode="+faceModeF);
                if(stepF == FaceEnrollNoStepsManager.STEP_WAIT_GO || stepF == FaceEnrollNoStepsManager.STEP_WAIT_FACE) {
                    // Wait TO GO
                    mBtnStart.setEnabled(faceModeF == ErrorMode.NONE);
                    mProgressStepView.setSurroundMode(mProgressStepView.getSurroundMode(), faceModeF);
                }
            }
        };

        return listener;
    }

    // setup the enrollment callbacks
    private FaceAuthEnrollerCallback setUpAuthEnrollerCallback(final FaceEnrollerUIListener faceEnrollerUIListener){
        FaceAuthEnrollerCallback callback = new FaceAuthEnrollerCallback() {
            /**
             * Invoked when enrollment is finished.
             * Check the status to see if enrollment is successful.
             * @param faceAuthStatus
             */
            @Override
            public void onEnrollFinish(FaceAuthStatus faceAuthStatus) {
                MyLog.w(TAG, "enrollBiometric: ENDED!!! status ="+faceAuthStatus);
                if(!m_bCanceled && faceAuthStatus==FaceAuthStatus.CANCELED) {
                    MyLog.e(TAG, "HACKY Fix of Neuro >> FORCE STATUS CANCEL TO BAD_QUALITY");
                    faceAuthStatus = FaceAuthStatus.BAD_QUALITY;
                }

                if(faceAuthStatus == FaceAuthStatus.SUCCESS) {
                    FaceEnrollNoStepsManager.getInstance().cleanAuth();
                    mProgressStepView.setProgress(true, ErrorMode.NONE);
                    onEnrollSuccess(FaceEnrollNoStepsManager.getInstance().getLoggedUserImage());
                }  else if(faceAuthStatus !=  FaceAuthStatus.CANCELED) {
                    MyLog.i(TAG, "result= "+faceAuthStatus+" retries="+m_nbRetries);
                    m_nbRetries++;

                    FaceEnrollNoStepsManager.getInstance().cleanAuth();
                    mProgressStepView.setProgress(true, ErrorMode.ERROR);

                    if(m_callback!=null) {
                        ErrorDialogFragment.newInstance(EnrollFragment.this,
                                getActivity().getString(FaceMessagesHelper.getErrorMessageForErrorCode(faceAuthStatus)))
                                .showAllowingStateLoss(getActivity().getSupportFragmentManager(), "error");
                    }

                    if(m_nbRetries >= MAX_RETRY){
                        FaceEnrollNoStepsManager.getInstance().cleanAuth();
                        if(m_callback!=null) {
                            m_callback.onEnrollmentFailed(faceAuthStatus);
                        }
                    } else if(shouldStop){
                        FaceEnrollNoStepsManager.getInstance().cleanAuth();
                    }else{
                        MyLog.d(TAG, "Face verification failed : Remaining retries: " + (MAX_RETRY-m_nbRetries));
                        FaceEnrollNoStepsManager.getInstance().cleanAuth();
                        if(m_callback!=null) {
                            m_callback.onEnrollmentRetry(faceAuthStatus, MAX_RETRY - m_nbRetries);
                        }
                        // Uncomment this if you wish to disable to error dialog and auto-retry
                        //FaceEnrollNoStepsManager.getInstance().startEnrollment(faceEnrollerUIListener, this, TIMEOUT, TIME_MIN_PROCESSING);
                    }
                }
            }

            /**
             * Invoked when error occurs before Face Enrolment can be started.
             * eg. Device camera support, required permission not granted etc.
             * @param e
             */
            @Override
            public void onEnrollError(IdpException e) {
                MyLog.w(TAG, "enrollBiometric: ENDED!!! "+e.getMessage());
                FaceEnrollNoStepsManager.getInstance().cancel();
                if(m_callback!=null) {
                    m_callback.onError(e);
                }
            }
        };

        return callback;
    }

    // ===========================================================
    //  end enrollment
    // ===========================================================
    private void onEnrollSuccess(Bitmap image) {
        MyLog.i(TAG, "onEnrollSuccess");
        mLayoutRegistredUser.setVisibility(View.VISIBLE);
        mFaceView.setVisibility(View.GONE);

        // Uncomment to display a round face image instead of placeholder
        //if(image != null) {
        //    Bitmap cover = ImageShapeTool.getRoundedCroppedBitmap(image, image.getWidth());
        //    mIvRegistredUser.setImageBitmap(cover);
        //} else {
            // Always use the avatar for now as requested
            mIvRegistredUser.setImageResource(R.drawable.face_demo);
        //}

        mBtnStart.setVisibility(View.GONE);
        mBtnCancel.setVisibility(View.GONE);

        if(m_resultModeTempo >= 0) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                if(m_callback!=null) {
                    m_callback.onEnrollmentSuccess();
                }
            }, m_resultModeTempo);
        }
        else {
            mBtnSuccess.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Error Dialog Ok Listener
     */
    @Override
    public void onDialogOk() {
        MyLog.d(TAG, "onDialogOk");
        if(m_callback!=null && !shouldStop) {
            runEnroll();
        }
    }
}
