/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package uk.co.senab.bitmapcache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class CacheableImageView extends ImageView {

	private static final int FADE_DURATION = 200;
	
	private CacheableBitmapWrapper mDisplayedBitmapWrapper;
	
	private static BitmapDrawable mEmptyDrawable;

	public CacheableImageView(Context context) {
		super(context);
	}

	public CacheableImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Sets the current {@code CacheableBitmapWrapper}, and displays it Bitmap.
	 * 
	 * @param wrapper - Wrapper to display.s
	 */
	public void setImageCachedBitmap(final CacheableBitmapWrapper wrapper) {
		setImageCachedBitmap(wrapper, false);
	}
	
	/**
	 * Sets the current {@code CacheableBitmapWrapper}, and displays it Bitmap.
	 * 
	 * @param wrapper - Wrapper to display.s
	 * @param withFade - use fade in effect
	 */
	public void setImageCachedBitmap(final CacheableBitmapWrapper wrapper, boolean withFade) {
		if (null != wrapper) {
			wrapper.setBeingUsed(true);
			//setImageDrawable(new BitmapDrawable(getResources(), wrapper.getBitmap()), withFade);
			
			// be defensive before we attempt to draw
			if (!wrapper.getBitmap().isRecycled()) {
				setImageDrawable(new BitmapDrawable(getResources(), wrapper.getBitmap()), withFade);
			} else {
				Log.e(Constants.LOG_TAG, "Trying to draw a recycled bitmap!!! : " + wrapper.getUrl());
				setImageDrawable(null);
			}
			
		} else {
			setImageDrawable(null);
		}

		// Finally, set our new BitmapWrapper
		mDisplayedBitmapWrapper = wrapper;
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		setImageCachedBitmap(new CacheableBitmapWrapper(bm));
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		resetCachedDrawable();
	}
	
	public void setImageDrawable(Drawable drawable, boolean withFade) {
		if (withFade) {
			if (mEmptyDrawable == null) mEmptyDrawable = new BitmapDrawable(getResources());
			TransitionDrawable fadeInDrawable = new TransitionDrawable(new Drawable[] { mEmptyDrawable, drawable });
			setImageDrawable(fadeInDrawable);
			fadeInDrawable.startTransition(FADE_DURATION);
		} else {
			setImageDrawable(drawable);
		}
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		resetCachedDrawable();
	}

	public CacheableBitmapWrapper getCachedBitmapWrapper() {
		return mDisplayedBitmapWrapper;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		// Will cause displayed bitmap wrapper to be 'free-able'
		setImageDrawable(null);
	}

	/**
	 * Called when the current cached bitmap has been removed. This method will
	 * remove the displayed flag and remove this objects reference to it.
	 */
	private void resetCachedDrawable() {
		if (null != mDisplayedBitmapWrapper) {
			mDisplayedBitmapWrapper.setBeingUsed(false);
			mDisplayedBitmapWrapper = null;
		}
	}

}
