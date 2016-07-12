package com.day1.dragdropselect;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DragDropSelect extends View {

    public interface TheObserver {
        void callback(int i);
    }

    public void registerObserver(TheObserver observer) {
        mObserver = observer;
    }

    private void pointSelected(int i) {
        if (mObserver!=null) {
            mObserver.callback(i);
        }
    }

    private TheObserver mObserver;
    private int mThumbX, mThumbY, mCircleCenterX, mCircleCenterY, mCircleRadius;
    private int mPadding, mThumbSize, mThumbColor, mBorderColor, mBorderThickness, mPointSize, mPointColor, mPointTotal;
    private double[] pointsX, pointsY;
    private double mDistanceX, mDistanceY;
    private boolean mIsThumbSelected = false;
    private Paint mPaint = new Paint();

    public DragDropSelect(Context context) {
        this(context, null);
    }

    public DragDropSelect(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragDropSelect(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    // common initializer method
    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DragDropSelect, defStyleAttr, 0);

        // retrieve variables
        int pointTotal = a.getInteger(R.styleable.DragDropSelect_point_total, 10);
        int pointSize = a.getDimensionPixelSize(R.styleable.DragDropSelect_point_size, 25);
        int pointColor = a.getColor(R.styleable.DragDropSelect_point_color, Color.YELLOW);
        int borderThickness = a.getDimensionPixelSize(R.styleable.DragDropSelect_border_thickness, 20);
        int borderColor = a.getColor(R.styleable.DragDropSelect_border_color, Color.RED);
        int thumbSize = a.getDimensionPixelSize(R.styleable.DragDropSelect_thumb_size, 50);
        int thumbColor = a.getColor(R.styleable.DragDropSelect_thumb_color, Color.GRAY);

        // save those to fields
        setPointTotal(pointTotal);
        setPointSize(pointSize);
        setPointColor(pointColor);
        setBorderThickness(borderThickness);
        setBorderColor(borderColor);
        setThumbSize(thumbSize);
        setThumbColor(thumbColor);

        // initialize node coordinates in array
        pointsX = new double[mPointTotal];
        pointsY = new double[mPointTotal];

        // assign padding - check for version because of RTL layout compatibility
        int padding;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            int all = getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop() + getPaddingEnd() + getPaddingStart();
            padding = all / 6;
        } else {
            padding = (getPaddingLeft() + getPaddingRight() + getPaddingBottom() + getPaddingTop()) / 4;
        }
        setPadding(padding);

        a.recycle();
    }

    public void setPointTotal(int pointTotal) {
        mPointTotal = pointTotal;
    }

    public void setPointSize(int pointSize) {
        mPointSize = pointSize;
    }

    public void setPointColor(int color) {
        mPointColor = color;
    }

    public void setBorderThickness(int circleBorderThickness) {
        mBorderThickness = circleBorderThickness;
    }

    public void setBorderColor(int color) {
        mBorderColor = color;
    }

    public void setThumbSize(int thumbSize) {
        mThumbSize = thumbSize;
    }

    public void setThumbColor(int color) {
        mThumbColor = color;
    }

    public void setPadding(int padding) {
        mPadding = padding;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // use smaller dimension for calculations (depends on parent size)
        int smallerDim = w > h ? h : w;

        // find circle's rectangle points
        int largestCenteredSquareLeft = (w - smallerDim) / 2;
        int largestCenteredSquareTop = (h - smallerDim) / 2;
        int largestCenteredSquareRight = largestCenteredSquareLeft + smallerDim;
        int largestCenteredSquareBottom = largestCenteredSquareTop + smallerDim;

        // save circle coordinates and radius in fields
        mCircleCenterX = largestCenteredSquareRight / 2 + (w - largestCenteredSquareRight) / 2;
        mCircleCenterY = largestCenteredSquareBottom / 2 + (h - largestCenteredSquareBottom) / 2;
        mCircleRadius = smallerDim / 2 - mBorderThickness / 2 - mPadding;

        // setup nodes
        for (int i=0;i<mPointTotal;i++) {
            pointsX[i] = mCircleCenterX+mCircleRadius*Math.sin((i)*(360/mPointTotal)*Math.PI/180);
            pointsY[i] = mCircleCenterY-mCircleRadius*Math.cos((i)*(360/mPointTotal)*Math.PI/180);
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // outer circle (ring)
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderThickness);
        mPaint.setAntiAlias(true);
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mPaint);

        // draw nodes
        mPaint.setColor(mPointColor);
        mPaint.setStyle(Paint.Style.FILL);
        for (int i=0;i<mPointTotal;i++) {
            mPaint.setColor(mPointColor);
            canvas.drawCircle((float) pointsX[i], (float) pointsY[i], mPointSize, mPaint);
        }

        // find thumb position
        mThumbX = (int) (mCircleCenterX+mDistanceX);
        mThumbY = (int) (mCircleCenterY+mDistanceY);

        /*
        Future addon for images in thumb... Ignore for now..
        if (mThumbImage != null) {
            mThumbImage.setBounds(mThumbX - mThumbSize / 2, mThumbY - mThumbSize / 2, mThumbX + mThumbSize / 2, mThumbY + mThumbSize / 2);
            mThumbImage.draw(canvas);
        } else {
        */

        // draw thumb circle
        mPaint.setColor(mThumbColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(mThumbX, mThumbY, mThumbSize, mPaint);
    }

    private void updateSliderState(int touchX, int touchY) {
        mDistanceX = touchX - (mCircleCenterX);
        mDistanceY = touchY - (mCircleCenterY);
    }

    private void checkPointSelect(int touchX, int touchY) {
        // checks if thumb was placed in a node and if so notifies observer
        for (int i=0;i<mPointTotal;i++) {
            if (touchX+mPointSize/2>pointsX[i] && touchX-mPointSize/2<pointsX[i]
                    && touchY+mPointSize/2>pointsY[i] && touchY-mPointSize/2<pointsY[i]) {
                mDistanceX = pointsX[i]-mCircleCenterX;
                mDistanceY = pointsY[i]-mCircleCenterY;
                pointSelected(i);
                return;
            }
        }
        // if thumb not placed in a node then thumb is reset
        mDistanceX = 0;
        mDistanceY = 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                // start moving the thumb (this is the first touch)
                int x = (int) ev.getX();
                int y = (int) ev.getY();

                if (x < mThumbX + mThumbSize && x > mThumbX - mThumbSize && y < mThumbY + mThumbSize && y > mThumbY - mThumbSize) {
                    mIsThumbSelected = true;
                    updateSliderState(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                // still moving the thumb (this is not the first touch)
                if (mIsThumbSelected) {
                    int x = (int) ev.getX();
                    int y = (int) ev.getY();
                    updateSliderState(x, y);
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                // finished moving (this is the last touch)
                mIsThumbSelected = false;
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                checkPointSelect(x, y);
                break;
            }
        }

        // redraw the whole component
        invalidate();
        return true;
    }
}
