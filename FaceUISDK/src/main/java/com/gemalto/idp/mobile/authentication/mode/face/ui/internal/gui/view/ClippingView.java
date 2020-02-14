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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.gemalto.idp.mobile.authentication.mode.face.ui.R;

/**
 * A view that crop an image circularly to give impression of a circular video display
 */
public class ClippingView extends View {
	//private final static float CLIP_RADIUS = 150; // dp
	private final static float WIDTH_RATIO = 0.7f;

	private Bitmap bitmapBuffer;
	private Canvas canvasBuffer;
	private Bitmap backgroundDrawable;
	private Paint transparentPaint;
	private float m_cx;
	private float m_cy;
	private float m_radius;

	private Rect m_rectDrawableSrc;

	private Rect m_rectDrawableDest;
	
	public ClippingView(Context context) {
	    super(context);
	    init(context);
	}

	public ClippingView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init(context);
	}

	public ClippingView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    init(context);
	}

	private void init(Context context) {
		backgroundDrawable = getBackgroundBitmap();
		
		transparentPaint = new Paint();
		transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
		transparentPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
		transparentPaint.setAntiAlias(true);
		
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	    super.onSizeChanged(w, h, oldw, oldh);
	    
	    if(bitmapBuffer != null) {
	    	bitmapBuffer.recycle();
	    	bitmapBuffer = null;
	    }
	    
	    bitmapBuffer = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	    bitmapBuffer.eraseColor(Color.TRANSPARENT);
	    canvasBuffer = new android.graphics.Canvas(bitmapBuffer);
	    
	    int paddBottom = getPaddingBottom();
	    
	    m_cx = w/2;
	    m_cy = h/2-paddBottom/2;
	    
		m_radius = WIDTH_RATIO*w/2;

		m_rectDrawableSrc = new Rect(0, 0, backgroundDrawable.getWidth()-1, backgroundDrawable.getHeight()-1);
		m_rectDrawableDest = new Rect(0, 0, w-1, h-1);
	}
	
	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		canvasBuffer.drawBitmap(backgroundDrawable, m_rectDrawableSrc, m_rectDrawableDest, null);
		canvasBuffer.drawCircle(m_cx, m_cy, m_radius, transparentPaint);
		canvas.drawBitmap(bitmapBuffer, 0, 0, null);
	}
	
	private Bitmap getBackgroundBitmap() {
		Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(),
	            R.drawable.background_process);
		return bmp;
	}
}