/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.virtualart.virtualart;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.virtualart.virtualart.util.FileHelper;

public class PaintActivity extends Activity
        implements ColorPickerDialog.OnColorChangedListener {  
		private MyView myView;
	
	public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		myView = new MyView(this);
        setContentView(myView);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
        
        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                                       0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }
    
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter  mBlur;
    
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    public class MyView extends View {
        
        private static final float MINP = 0.25f;
        private static final float MAXP = 0.75f;
        
        private Bitmap  mBitmap;
        private Canvas  mCanvas;
        private Path    mPath;
        private Paint   mBitmapPaint;
        
        public MyView(Context c) {
            super(c);
            
            mBitmap = Bitmap.createBitmap(320, 480, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }

		public Bitmap getBitmapCopy() {
			//public Bitmap copy (Bitmap.Config config, boolean isMutable)
			return mBitmap.copy(
				mBitmap.getConfig(),
				false
			);
		}

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(0xFFAAAAAA);
            
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            
            canvas.drawPath(mPath, mPaint);
        }
        
        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;
        
        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }
        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;
            }
        }
        private void touch_up() {
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
            // kill this so we don't double draw
            mPath.reset();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
    
    private static final int COLOR_MENU_ID   = Menu.FIRST;
    private static final int EMBOSS_MENU_ID  = Menu.FIRST + 1;
    private static final int BLUR_MENU_ID    = Menu.FIRST + 2;
    private static final int ERASE_MENU_ID   = Menu.FIRST + 3;
    private static final int SRCATOP_MENU_ID = Menu.FIRST + 4;
    private static final int SAVE_MENU_ID    = Menu.FIRST + 5;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        menu.add(0, COLOR_MENU_ID, 0, "Color").setShortcut('3', 'c');
        menu.add(0, EMBOSS_MENU_ID, 0, "Emboss").setShortcut('4', 's');
        menu.add(0, BLUR_MENU_ID, 0, "Blur").setShortcut('5', 'z');
        menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
        menu.add(0, SRCATOP_MENU_ID, 0, "SrcATop").setShortcut('5', 'z');
        menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('6', 'x');

        /****   Is this the mechanism to extend with filter effects?
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
                              Menu.ALTERNATIVE, 0,
                              new ComponentName(this, NotesList.class),
                              null, intent, 0, null);
        *****/
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case COLOR_MENU_ID:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                return true;
            case EMBOSS_MENU_ID:
                if (mPaint.getMaskFilter() != mEmboss) {
                    mPaint.setMaskFilter(mEmboss);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case BLUR_MENU_ID:
                if (mPaint.getMaskFilter() != mBlur) {
                    mPaint.setMaskFilter(mBlur);
                } else {
                    mPaint.setMaskFilter(null);
                }
                return true;
            case ERASE_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                        PorterDuff.Mode.CLEAR));
                return true;
            case SRCATOP_MENU_ID:
                mPaint.setXfermode(new PorterDuffXfermode(
                                                    PorterDuff.Mode.SRC_ATOP));
                mPaint.setAlpha(0x80);
                return true;
            case SAVE_MENU_ID:
				Log.v("PaintActivity", "saveButtonPresed");
				//Save current as bitmap
				//String filename = "/sdcard/mypic.png";
				String filename = "mypic.png";
				FileHelper.saveBitmapWithFilename(
					myView.getBitmapCopy(),
					filename
				);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
