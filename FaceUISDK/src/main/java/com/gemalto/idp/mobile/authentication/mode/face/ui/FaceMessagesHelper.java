package com.gemalto.idp.mobile.authentication.mode.face.ui;

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
import com.gemalto.idp.mobile.authentication.mode.face.FaceAuthStatus;

/**
 * This class help to display generic Facial UI messages
 */
public class FaceMessagesHelper {
	/**
	 * This method convert a FaceStatus error to a string ressource Id. Use this method if youd don't wan't to implement your own messages.
	 * @param error The FaceStatus error returned by the activity
	 * @return Message resource Id
	 */
	public static int getErrorMessageForErrorCode(FaceAuthStatus error) {
		if(error != null) {
			if (error == FaceAuthStatus.ERROR || error == FaceAuthStatus.ALREADY_EXTRACTING) {
				return R.string.error_UNKNOWN;
			} else if (error == FaceAuthStatus.BAD_QUALITY) {
				return R.string.error_BAD_QUALITY;
			} else if (error == FaceAuthStatus.CAMERA_NOT_FOUND) {
				return R.string.error_CAMERA_NOT_FOUND;
			} else if (error == FaceAuthStatus.MATCH_NOT_FOUND) {
				return R.string.error_MATCH_NOT_FOUND;
			} else if (error == FaceAuthStatus.USER_NOT_FOUND) {
				return R.string.error_USER_NOT_FOUND;
			} else if (error == FaceAuthStatus.USER_REENROLL_NEEDED) {
				return R.string.error_USER_REENROLL_NEEDED;
			} else if (error == FaceAuthStatus.USER_REENROLL_NEEDED) {
				return R.string.error_USER_REENROLL_NEEDED;
			} else if (error == FaceAuthStatus.LIVENESS_CHECK_FAILED) {
				return R.string.error_LIVENESS_CHECK_FAILED;
			} else if (error == FaceAuthStatus.TIMEOUT) {
				return R.string.error_TIMEOUT;
			}
		}
		return R.string.error_UNKNOWN;
	}
}
