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
package com.gemalto.idp.mobile.authentication.mode.face.ui.internal.utils.logs;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * To summarize the preceding code, the MyLog class is simply wrapping the calls to the android
 * Log class. The rest of your application can simply reference MyLog.d("MyApp", "Sample debug message");
 * and if the device/emulator running the app has the log level set to debug the message will
 * appear. The benefit here is you won't have to worry about removing print lines
 * or maintaining variables for levels that you might forget to change before building
 * your release apk.
 * Changing the log level on device is actually very simple.
 * Simply run : adb shell setprop log.tag.<YOUR_LOG_TAG> <LEVEL>
 *
 * MyLog.init();
 MyLog.setLogLevel(MyLog.ALL);
 MyLog.i(TAG, "onCreate");
 try {
 String filename = Environment.getExternalStorageDirectory() + "/eon_weez_logs.log";
 MyLog.printLogToFile2(filename);
 } catch (IOException e) {
 e.printStackTrace();
 }
 */
public class MyLog {

	// Field descriptor #8 I
	public static final int ALL = 0;
	// Field descriptor #8 I
	public static final int NOTHING = 255;
	// Field descriptor #8 I
	public static final int VERBOSE = 2;
	// Field descriptor #8 I
	public static final int DEBUG = 3;
	// Field descriptor #8 I
	public static final int INFO = 4;
	// Field descriptor #8 I
	public static final int WARN = 5;
	// Field descriptor #8 I
	public static final int ERROR = 6;


	private static int m_logLevel = MyLog.ALL;

	// use the classname for the logger, this way you can refactor
	private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);


	public static void init() {
		LOGGER.setLevel(Level.OFF);
	}

	public static void init(int level) {
		LOGGER.setLevel(Level.OFF);
		setLogLevel(level);
	}

	/**
	 * Set level to MyLog.NOTHING to disable logs
	 */
	public static void setLogLevel(int level)
	{
		m_logLevel = level;
	}

	/**
	 * Basic Log.isLoggable is not secure because you can enable it
	 *
	 * Checks to see whether or not a log for the specified tag is loggable at
	 * the specified level. The default level of any tag is set to INFO.
	 * This means that any level above and including INFO will be logged.
	 * Before you make any calls to a logging method you should check to see
	 * if your tag should be logged. You can change the default level by setting
	 * a system property: 'setprop log.tag.<YOUR_LOG_TAG> <LEVEL>'
	 * Where level is either VERBOSE, DEBUG, INFO, WARN, ERROR, ASSERT, or SUPPRESS.
	 * SUPPRESS will turn off all logging for your tag.
	 * You can also create a local.prop file that with the following in it:
	 * 'log.tag.<YOUR_LOG_TAG>=<LEVEL>' and place that in /data/local.prop.
	 */
	public static boolean isLoggable(String tag, int level)
	{
		return level >= m_logLevel;
		// return Log.isLoggable(tag, level);
	}

	public static boolean isDebug()
	{
		return m_logLevel <= DEBUG;
	}

	public static void v(String tag, String msg) {
		if (MyLog.isLoggable(tag, Log.VERBOSE)) {
			Log.v(tag, msg);
			LOGGER.finest(formatLogger(tag, msg));
		}
	}

	private static String formatLogger(String tag, String msg) {
		int p_Id = android.os.Process.myPid();
		StringBuffer buff = new StringBuffer();
		buff.append(tag);
		buff.append("( ");
		buff.append(p_Id);
		buff.append("): ");
		buff.append(msg);

		//return tag+"( 0): "+msg;
		return buff.toString();
	}


	public static void d(String tag, String msg) {
		if (MyLog.isLoggable(tag, Log.DEBUG)) {
			Log.d(tag, msg);
			LOGGER.fine(formatLogger(tag, msg));
		}
	}

	public static void i(String tag, String msg) {
		if (MyLog.isLoggable(tag, Log.INFO)) {
			Log.i(tag, msg);
			LOGGER.info(formatLogger(tag, msg));
		}
	}

	public static void w(String tag, String msg) {
		if (MyLog.isLoggable(tag, Log.WARN)) {
			Log.w(tag, msg);
			LOGGER.warning(formatLogger(tag, msg));
		}
	}

	public static void e(String tag, String msg) {
		if (MyLog.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg);
			LOGGER.severe(formatLogger(tag, msg));
		}
	}

	public static void e(String tag, String msg, Throwable tr) {
		if (MyLog.isLoggable(tag, Log.ERROR)) {
			Log.e(tag, msg, tr);
			LOGGER.severe(formatLogger(tag, msg + " "+tr.toString()));
		}
	}

	public static void printLogToFileRedirect(Context context, String filename){
		//String filename = context.getExternalFilesDir(null).getPath() + File.separator + "my_app.log";
		String command = "logcat -f "+ filename + " -v time -d *:V";

		Log.d("MyLog", "command: " + command);

		try{
			Runtime.getRuntime().exec(command);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 *  MyLog.d(TAG, MyLog.getExtraBundleDescription(intent.getExtras()));
	 * @param bundle
	 * @return
	 */
	public static String getExtraBundleDescription(Bundle bundle){
		String log = "";
		for (String key : bundle.keySet()) {
			Object value = bundle.get(key);
			log += String.format("%s=%s (%s)", key,
					value.toString(), value.getClass().getName()) + "\n";
		}
		return log;
	}
}