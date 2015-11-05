/*
 * Copyright (c) 2015 Layer. All rights reserved.
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
package com.layer.atlas;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.layer.atlas.Atlas.Tools;


/**
 * @author Oleg Orlov
 * @since  8 May 2015
 */
public class ShapedFrameLayout extends FrameLayout {

    private static final String TAG = ShapedFrameLayout.class.getSimpleName();
    private static final boolean debug = false;
    
    private float[] corners = new float[] { 0, 0, 0, 0 };
    private boolean refreshShape = true;
    private Path shaper = new Path();
    
    private RectF pathRect = new RectF();
    
    public ShapedFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareRendering();
    }

    public ShapedFrameLayout(Context context) {
        super(context);
        prepareRendering();
    }

    public ShapedFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        prepareRendering();
    }

    public void setCornersDp(float[] cornerRadii) {
        System.arraycopy(cornerRadii, 0, this.corners, 0, 4);
        refreshShape = true;
    }
    
    private void prepareRendering() {
        if (android.os.Build.VERSION.SDK_INT < 18) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            if (debug)  Log.d(TAG, "setSoftwareRendering() software rendering...");
        }
    }
    
    public void setCornerRadiusDp(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        this.corners[0] = topLeft;
        this.corners[1] = topRight;
        this.corners[2] = bottomRight;
        this.corners[3] = bottomLeft;
    }
    
    @Override
    protected void dispatchDraw(Canvas canvas) {
        // clipPath according to shape
        
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        
        if (debug)  Log.d(TAG, "dispatchDraw() drawSize: " + width + "x" + height 
                + ", measuredSize: " + getMeasuredWidth() + "x" + getMeasuredHeight()
                + ", size: " + getWidth() + "x" + getHeight()
                + ", resetShape: " + refreshShape);
        
        if (refreshShape || true) {
            shaper.reset();
            pathRect.set(0, 0, width, height);
            float[] roundRectRadii = Atlas.Tools.getRoundRectRadii(corners, getResources().getDisplayMetrics());
            shaper.addRoundRect(pathRect, roundRectRadii,  Direction.CW);
            
            refreshShape = false;
        }
        
        int saved = canvas.save();
        canvas.clipPath(shaper);
        
        super.dispatchDraw(canvas);
        
        canvas.restoreToCount(saved);
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        refreshShape = true;
    }
    
    /**
     * Android's [match_parent;match_parent] behaves weird - it will finally pass you [parent_width;0]
     * Non-zero results comes from minWidth/minHeight and background's padding, but not from parent. 
     * So if spec is UNSPECIFIED, we should never tell Android that [0;0] is OK for us
     * <pre>
     * FrameLayout 
     *      view1 [match_parent; match_parent]  ->  [parent_width; 0]
     * 
     * FrameLayout 
     *      view1 [match_parent; match_parent]  ->  [parent_width; parent_height]
     *      view2 [match_parent; match_parent]  ->  [parent_width; parent_height]
     * 
     * FrameLayout 
     *      view1 [match_parent; match_parent] + min[30dp; 30dp]  ->  [parent_width; 30dp]
     * 
     * FrameLayout 
     *      view1 [match_parent; match_parent]  ->  [parent_width; 0]
     *      view2 [30dp; 30dp]                  ->  [30dp; 30dp]
     * 
     * FrameLayout 
     *      view1 [30dp; 30dp]                  ->  [30dp; 30dp]
     *      view2 [match_parent; match_parent]  ->  [parent_width; 0]
     * </pre>
     *
     * <p>
     * Caused by        <a href="https://code.google.com/p/android/issues/detail?id=77225">bug in FrameLayout</a><br>
     * 
     * Commit:          <a href="https://android.googlesource.com/platform/frameworks/base/+/a174d7a0d5475dbae2b48f7359abf1637a882896%5E%21/#F0">https://android.googlesource.com/platform/frameworks/base/+/a174d7a0d5475dbae2b48f7359abf1637a882896%5E%21/#F0</a> <br>
     * Duplicate issue: <a href="https://code.google.com/p/android/issues/detail?id=136131">https://code.google.com/p/android/issues/detail?id=136131</a> 
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int mWidthBefore  = getMeasuredWidth();
        int mHeightBefore = getMeasuredHeight();
        if (debug) Log.w(TAG, "onMeasure() before: " + mWidthBefore + "x" + mHeightBefore
                + ", spec: " + Tools.toStringSpec(widthMeasureSpec, heightMeasureSpec));
        
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        superMeasure(widthMeasureSpec, heightMeasureSpec);
        
        int mWidthAfter = getMeasuredWidth();
        int mHeightAfter = getMeasuredHeight();
        int maxChildWidth = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getMeasuredWidth() > maxChildWidth) {
                if (debug) Log.w(TAG, "onMeasure() child: " + i + ", width: " + child.getMeasuredWidth() + " > maxWidth: " + maxChildWidth + ", visibility: " + child.getVisibility() + ", " + child);
                maxChildWidth = child.getMeasuredWidth();
            }
        }
        
        if (debug) Log.w(TAG, "onMeasure() after: " + mWidthAfter + "x" + mHeightAfter);
    }
    
    private final ArrayList<View> mMatchParentChildren = new ArrayList<View>(1);
    
    private void superMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;
        
        boolean mMeasureAllChildren = false;
        
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (mMeasureAllChildren || child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth  = Math.max(maxWidth,  child.getMeasuredWidth()  + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin  + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        // Account for padding too
        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
        maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(
                resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        for (int i = 0; i < count; i++) {
            final View child = mMatchParentChildren.get(i);

            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidthMeasureSpec;
            int childHeightMeasureSpec;
            
            if (lp.width == LayoutParams.MATCH_PARENT) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() -
                        getPaddingLeftWithForeground() - getPaddingRightWithForeground() -
                        lp.leftMargin - lp.rightMargin,
                        MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
                        getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
                        lp.leftMargin + lp.rightMargin,
                        lp.width);
            }
            
            if (lp.height == LayoutParams.MATCH_PARENT) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() -
                        getPaddingTopWithForeground() - getPaddingBottomWithForeground() -
                        lp.topMargin - lp.bottomMargin,
                        MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
                        getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
                        lp.topMargin + lp.bottomMargin,
                        lp.height);
            }

            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    private int getPaddingBottomWithForeground() {
        return 0;
    }

    private int getPaddingTopWithForeground() {
        return 0;
    }

    private int getPaddingRightWithForeground() {
        return 0;
    }

    private int getPaddingLeftWithForeground() {
        return 0;
    }

}
