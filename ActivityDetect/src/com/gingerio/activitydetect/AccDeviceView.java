package com.gingerio.activitydetect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.view.View;


public class AccDeviceView extends View {

    private Bitmap  mBitmap;
    private Paint   mPaint = new Paint();
    private Canvas  mCanvas = new Canvas();
    private float   mLastValues[] = new float[3*2];
    private int     mColors[] = new int[3*2];
    private float   mLastX;
    private float   mScale[] = new float[2];
    private float   mYOffset;
    private float   mMaxX;
    private float   mSpeed = 1.0f;
    private float   mWidth;
    private float   mHeight;
    
    public AccDeviceView(Context context) {
        super(context);
        mColors[0] = Color.RED;//Color.argb(192, 64, 255, 255);
        mColors[1] = Color.BLUE;//argb(192, 128, 64, 128);
        mColors[2] = Color.GREEN;//argb(192, 255, 255, 64);
        float stroke = (float) 2.0;
        mPaint.setStrokeWidth(stroke);
     /*   mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
        mPath.arcTo(mRect, 0, 180);*/
    }
    public AccDeviceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mColors[0] = Color.RED;//Color.argb(192, 64, 255, 255);
        mColors[1] = Color.BLUE;//argb(192, 128, 64, 128);
        mColors[2] = Color.GREEN;//argb(192, 255, 255, 64);
        float x = (float) 2.0;
        mPaint.setStrokeWidth(x);
     
    }
    
    public AccDeviceView(Context context, AttributeSet attrs,
 	       int defStyle) {
        super(context, attrs, defStyle);
        mColors[0] = Color.RED;//Color.argb(192, 64, 255, 255);
        mColors[1] = Color.BLUE;//argb(192, 128, 64, 128);
        mColors[2] = Color.GREEN;//argb(192, 255, 255, 64);
        float x = (float) 2.0;
        mPaint.setStrokeWidth(x);
     
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        mCanvas.setBitmap(mBitmap);
        mCanvas.drawColor(0xFFFFFFFF);
        mYOffset = h * 0.5f;
        mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
        mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
        mWidth = w;
        mHeight = h;
        if (mWidth < mHeight) {
            mMaxX = w;
        } else {
            mMaxX = w-50;
        }
        mLastX = mMaxX;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (this) {
            if (mBitmap != null) {
                final Paint paint = mPaint;
          //      final Path path = mPath;
          //      final int outer = 0xFFC0C0C0;
          //      final int inner = 0xFFff7010;

                if (mLastX >= mMaxX) {
                    mLastX = 0;
                    final Canvas cavas = mCanvas;
                    final float yoffset = mYOffset;
                    final float maxx = mMaxX;
                    final float oneG = SensorManager.STANDARD_GRAVITY * mScale[0];
                    paint.setColor(0xFFAAAAAA);
                    cavas.drawColor(0xFFFFFFFF);
                    cavas.drawLine(0, yoffset,      maxx, yoffset,      paint);
                    cavas.drawLine(0, yoffset+oneG, maxx, yoffset+oneG, paint);
                    cavas.drawLine(0, yoffset-oneG, maxx, yoffset-oneG, paint);
                }
                canvas.drawBitmap(mBitmap, 0, 0, null);
                }

            
        }
    }

    public void setValues(SensorEvent event){
    synchronized (this) {
            if (mBitmap != null) {
                final Canvas canvas = mCanvas;
                final Paint paint = mPaint;
                    float deltaX = mSpeed;
                    float newX = mLastX + deltaX;
                   
                    int j = (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) ? 1 : 0;
                    for (int i=0 ; i<3 ; i++) {
                        int k = i+j*3;
                        final float v = mYOffset + event.values[i] * mScale[j];
                        paint.setColor(mColors[k]);
                        canvas.drawLine(mLastX, mLastValues[k], newX, v, paint);
                        mLastValues[k] = v;
                    }
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                        mLastX += mSpeed;
                    }
                    
                invalidate();
            
        }
    }
    
   /* public void setGraphMag(double[] sigmag){
    	float max = getMax(sigmag);
		float min = getMin(sigmag);
		float diff = max - min;
		synchronized (this) {
    	if (mBitmap != null) {
    		if(max!=min){
            final Canvas canvas = mCanvas;
            final Paint paint = mPaint;
            float height = getHeight();
    		float width = getWidth() - 1;
    		float datalength = sigmag.length;
			float colwidth = width / datalength;
			float halfcol = colwidth / 2;
         //   float deltaX = mSpeed;
       //     float newX = mLastX + deltaX;
            float lasth = 0;
            
           // float newX = mLastX + deltaX;
    	for(int i =0; i<datalength;i++){
    		
    		float val = (float)sigmag[i] - min;
			float rat = val / diff;
			float h = height * rat;
    		paint.setColor(Color.BLACK);
    		if(i>0)
    		canvas.drawLine(((i - 1) * colwidth)+halfcol, mLastValues[3], (i * colwidth)+halfcol, (float)sigmag[i], paint);
    		lasth = h;
    		mLastValues[3] = (float)sigmag[i];
    	//	if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
         //       mLastX += mSpeed;
         //   }
    	//	mLastX += mSpeed;}
    			
    			}
    		}
    	 }
    	invalidate();
    	}
    }*/

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
    
  /*  private float getMax(double[] values) {
		float largest = Integer.MIN_VALUE;
		if(values!=null){
		for (int i = 0; i < values.length; i++)
			if (values[i] > largest)
				largest = (float)values[i];
		}
		return largest;
	}

	private float getMin(double[] values) {
		float smallest = Integer.MAX_VALUE;
		if(values!=null){
		for (int i = 0; i < values.length; i++)
			if (values[i] < smallest)
				smallest =(float)values[i];
		}
		return smallest;
	}*/
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(
                parentWidth/2, parentHeight);
    }


}
