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
package com.gemalto.idp.mobile.authentication.mode.face.ui.internal.gui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 *  A view used for FullEnrollMode to show user head position with an overlay on top of the video stream
 */
public class FaceMaskView extends View {
	private final static float FACE_MARGIN_RATIO = 0.05f; // %
	private final static float FACE_HEIGHT_RATIO = 0.60f; // %
	private final static float FACE_WIDTH_RATIO = 0.8f;
	
	private final static int COLOR_MASK = 0x88555555;
	private Bitmap m_bitmapBuffer;
	private Canvas m_canvasBuffer;

	private Paint m_transparentPaint;
	private RectF m_oval;
	
	@SuppressLint("RtlHardcoded")
	public enum MaskMode {
	    LEFT, TOP,
		RIGHT, BOTTOM
	}
	
	private final static int BLINK_DELAY = 1000;
	private Timer m_blinkTimer;
	private Handler mHandler = new Handler(Looper.getMainLooper());
	
	private MaskMode m_maskMode;
	
	public FaceMaskView(Context context) {
	    super(context);
	    init(context);
	}

	public FaceMaskView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init(context);
	}

	public FaceMaskView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init(context);
	}

	private void init(Context context) {
		m_maskMode = MaskMode.TOP;
		
		m_transparentPaint = new Paint();
		m_transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
		m_transparentPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		m_transparentPaint.setAntiAlias(true);
		
		m_oval = new RectF();
	}
	
	public void setMaskMode(MaskMode mode) {
		if(((m_maskMode == MaskMode.LEFT || m_maskMode == MaskMode.RIGHT) 
				&& (mode == MaskMode.TOP || mode == MaskMode.BOTTOM))
			|| ((m_maskMode == MaskMode.TOP || m_maskMode == MaskMode.BOTTOM) 
				&& (mode == MaskMode.LEFT || mode == MaskMode.RIGHT)) ){
			m_maskMode = mode;
			invalidate();
		}
		
	}
	
	public MaskMode getMaskMode() {
		return m_maskMode;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	    super.onSizeChanged(w, h, oldw, oldh);
	    
	    if(m_bitmapBuffer != null) {
	    	m_bitmapBuffer.recycle();
	    	m_bitmapBuffer = null;
	    }
	    m_bitmapBuffer = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
	    m_bitmapBuffer.eraseColor(Color.TRANSPARENT);
	    m_canvasBuffer = new android.graphics.Canvas(m_bitmapBuffer);
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	    super.onMeasure(widthMeasureSpec, widthMeasureSpec);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int w = getWidth();
		int h = getHeight();
		
		Resources r = getResources();
	    final float faceMaskHeight = FACE_HEIGHT_RATIO * w;
	    final float ovalH = faceMaskHeight;//TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, FACE_HEIGHT, r.getDisplayMetrics());
	    final float ovalW = ovalH*FACE_WIDTH_RATIO;
	    
	    final float faceMargin = FACE_MARGIN_RATIO * ovalH;
	    final float borderMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, faceMargin, r.getDisplayMetrics());
	    
	    
		float cx, cy;
		cx = cy = 0;
		if(m_maskMode == MaskMode.TOP) {
			cx = w/2;
			cy = 0 + ovalH/2 + borderMargin;
		}
		else if(m_maskMode == MaskMode.LEFT) {
			cx = ovalW/2 + borderMargin;
			cy = h/2;
		}
		else if(m_maskMode == MaskMode.RIGHT) {
			cx = w - ovalW/2 - borderMargin;
			cy = h/2;
		}
		else if(m_maskMode == MaskMode.BOTTOM) {
			cx = w/2;
			cy = h - ovalH/2 - borderMargin;
		}
		m_oval.set(cx-ovalW/2, cy-ovalH/2, cx+ovalW/2, cy+ovalH/2);
				
		// Draw
		m_canvasBuffer.drawColor(Color.TRANSPARENT, Mode.CLEAR); // clear Bitmap
		
		m_canvasBuffer.drawColor(COLOR_MASK);
		
		m_canvasBuffer.drawOval(m_oval, m_transparentPaint);

		canvas.drawBitmap(m_bitmapBuffer, 0, 0, null);
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		startBlinkTimer();
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		cancelBlinkTimer();
		super.onDetachedFromWindow();
	}
	
	protected void cancelBlinkTimer() {
		if(m_blinkTimer != null) {
			m_blinkTimer.cancel();
			m_blinkTimer = null;
		}
	}
	
	protected void startBlinkTimer() {
		cancelBlinkTimer();
		m_blinkTimer = new Timer();
		m_blinkTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(() -> {
					if(m_maskMode == MaskMode.TOP) {
						m_maskMode = MaskMode.BOTTOM;
					}
					else if(m_maskMode == MaskMode.LEFT) {
						m_maskMode = MaskMode.RIGHT;
					}
					else if(m_maskMode == MaskMode.RIGHT) {
						m_maskMode = MaskMode.LEFT;
					}
					else if(m_maskMode == MaskMode.BOTTOM) {
						m_maskMode = MaskMode.TOP;
					}
					invalidate();
				});
			}
		}, BLINK_DELAY, BLINK_DELAY);
	}
}