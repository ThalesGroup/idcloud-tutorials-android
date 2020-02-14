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

import android.content.Context;

import com.gemalto.idp.mobile.authentication.Authenticatable;
import com.gemalto.idp.mobile.authentication.AuthenticationModule;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthEnroller;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthEnrollerSettings;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthFactory;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthService;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthVerifier;
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthVerifierSettings;
import com.gemalto.idp.mobile.authentication.mode.face.ui.internal.utils.logs.MyLog;

/**
 * The Facial UI public manager used to configure, start verify or enroll activities
 */
public class FaceManager {
    private static final String TAG = FaceManager.class.getSimpleName();

    private static FaceManager s_instance;
    private FaceAuthService m_FaceAuthService;
    private FaceAuthFactory m_FaceAuthFactory;
    private static boolean isInitialized = false;

    private FaceAuthEnrollerSettings mFaceAuthEnrollerSettings;
    private FaceAuthVerifierSettings mFaceAuthVerifierSettings;

    /**
     * Init the singleton instance of the Face UI manager
     *
     * @return FaceManager
     */
    public static synchronized FaceManager initInstance() {
        if (s_instance == null) {
            s_instance = new FaceManager();
        }
        return s_instance;
    }

    /**
     * Get the singleton instance of the Face UI manager
     *
     * @return FaceManager
     */
    public static synchronized FaceManager getInstance() {
        return s_instance;
    }

    private FaceManager() {
        MyLog.init(MyLog.NOTHING); // Disable logs by default

        AuthenticationModule faceMod = AuthenticationModule.create();
        m_FaceAuthService = FaceAuthService.create(faceMod);
        m_FaceAuthFactory = m_FaceAuthService.getFaceAuthFactory();
        mFaceAuthEnrollerSettings = m_FaceAuthFactory.createFaceAuthEnrollerSettings();
        mFaceAuthVerifierSettings = m_FaceAuthFactory.createFaceAuthVerifierSettings();
    }

    /**
     * Load the internal Face service. Could be called multiple times.
     *
     * @param context An activity context (Not Null)
     */
    public void load(Context context) {
        MyLog.i(TAG, "load");
        if (context == null) {
            throw new NullPointerException("context must be not null");
        }
    }

    public static boolean isInitialized() {
        return isInitialized;
    }


    public FaceAuthEnroller getFaceAuthEnroller() {
        return m_FaceAuthFactory.createFaceAuthEnroller(getEnrollerSettings());
    }

    /**
     * Get the FaceAuthEnrollmentSettings for internal use
     *
     * @return FaceAuthEnrollmentSettings
     */
    public FaceAuthEnrollerSettings getEnrollerSettings() {
        if (mFaceAuthEnrollerSettings == null) {
            mFaceAuthEnrollerSettings = m_FaceAuthFactory.createFaceAuthEnrollerSettings();
        }
        return mFaceAuthEnrollerSettings;
    }

    public void updateAuthEnrollerSettings(FaceAuthEnrollerSettings faceAuthEnrollerSettings) {
        mFaceAuthEnrollerSettings = faceAuthEnrollerSettings;
    }

    /**
     * Update the FaceSettings
     */
    public void updateAuthVerifierSettings(FaceAuthVerifierSettings faceAuthVerifierSettings) {
        mFaceAuthVerifierSettings = faceAuthVerifierSettings;
    }

    /**
     * Get the FaceAuthEnrollmentSettings for internal use
     *
     * @return FaceAuthEnrollmentSettings
     */
    public FaceAuthVerifierSettings getVerifierSettings() {
        if (mFaceAuthVerifierSettings == null) {
            mFaceAuthVerifierSettings = m_FaceAuthFactory.createFaceAuthVerifierSettings();
        }
        return mFaceAuthVerifierSettings;
    }

    public FaceAuthVerifier getFaceAuthVerifier() {
        return m_FaceAuthFactory.createFaceAuthVerifer(getVerifierSettings());
    }

    /**
     * Get the FaceAuthService for internal use
     *
     * @return FaceAuthService
     */
    public FaceAuthService getFaceAuthService() {
        return m_FaceAuthService;
    }

    /**
     * Get the Enrollment Fragment -- Simple mode
     *
     * @param resultDuration The duration in ms of Enroll success result before auto finishing activity.
     *                       Use -1 to manually finish with a continue button. Use 0 for immediate return. (default 2000)
     */
    public EnrollFragment getEnrollmentFragment(final int resultDuration, final int retries) {
        EnrollFragment base = EnrollFragment.newInstance(resultDuration, retries);
        return base;
    }

    /**
     * Get the Verification Fragment
     *
     * @param resultDuration The duration in ms of Enroll success result before auto finishing activity.
     *                       Use -1 to manually finish with a continue button. Use 0 for immediate return. (default 2000)
     */
    public VerifyFragment getVerificationFragment(final Authenticatable authenticatable, final int resultDuration, final int retries) {
        VerifyFragment base = VerifyFragment.newInstance(resultDuration, retries);
        base.setAuthenticatable(authenticatable);
        return base;
    }
}	
