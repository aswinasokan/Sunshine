package uk.co.latestarter.sunshine.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import uk.co.latestarter.sunshine.R;

public class CompassView extends View {
    private Paint circleBrush;
    private Paint indicatorBrush;
    private Paint textBrush;

    private float mDegrees;

    private int mCenterX;
    private int mCenterY;
    private int mRimRadius;
    private int mFaceRadius;
    private int mIndicatorRadius;

    private float mEdge;
    private float mPadding;

    public CompassView(Context context) {
        super(context);
        initTools();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTools();
    }

    public CompassView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initTools();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int hSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int myHeight = hSpecSize;

        if (hSpecMode == MeasureSpec.EXACTLY) {
            myHeight = hSpecSize;
        } else if (hSpecMode == MeasureSpec.AT_MOST) {
            // Wrap Content
        }

        int wSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int myWidth = wSpecSize;
        if (wSpecMode == MeasureSpec.EXACTLY) {
            myWidth = wSpecSize;
        } else if (wSpecMode == MeasureSpec.AT_MOST) {
            // Wrap Content
        }

        setMeasuredDimension(myWidth, myHeight);
        calculateDimensions();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircleBg2(canvas);
        drawArrowIndicator(canvas);
        drawDirections(canvas);
    }

    private void initTools() {
        circleBrush = new Paint();
        indicatorBrush = new Paint();
        textBrush = new Paint();

        circleBrush.setAntiAlias(true);
        indicatorBrush.setAntiAlias(true);
        textBrush.setAntiAlias(true);

        circleBrush.setColor(getResources().getColor(R.color.sunshine_blue));

        indicatorBrush.setColor(Color.RED);
        indicatorBrush.setStyle(Paint.Style.FILL);

        textBrush.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        textBrush.setColor(Color.BLACK);
        textBrush.setTextAlign(Paint.Align.CENTER);
    }

    private void calculateDimensions() {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Outer rim radius has to be half of smallest dimension
        mRimRadius = (width < height) ? width / 2 : height / 2;

        // Center point within the view
        mCenterX = mRimRadius;
        mCenterY = mRimRadius;

        // Inner face radius
        mFaceRadius = mRimRadius - (mRimRadius/10);

        // Indicator radius
        mIndicatorRadius = mFaceRadius / 10;

        // Text coordinates
        mEdge = (mRimRadius - mFaceRadius) * 2;
        mPadding = mIndicatorRadius * 0.7f;

        textBrush.setTextSize(mIndicatorRadius * 2f);
    }

    private void drawCircleBg2(Canvas canvas) {
            // Outer circle
        circleBrush.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(mCenterX, mCenterY, mRimRadius, circleBrush);

        // Inner circle with reduced radius
        circleBrush.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(mCenterX, mCenterY, mFaceRadius, circleBrush);
    }

    private void drawCircleBg(Canvas canvas) {
        // Outer circle
        circleBrush.setStyle(Paint.Style.FILL_AND_STROKE);
        int[] colors = {0xFFB0B0B0, 0xFFFFFFFF, 0xFF000000};
        circleBrush.setShader(new RadialGradient(mCenterX, mCenterY, mRimRadius, colors, null, Shader.TileMode.MIRROR));
        canvas.drawCircle(mCenterX, mCenterY, mRimRadius, circleBrush);

        // Inner circle with reduced radius
        int[] colors2 = {0xFF00A6F1, 0xFF0074CB, 0xFF0074CB, 0xFF0053B3};
        circleBrush.setShader(new RadialGradient(mCenterX, mCenterY, mFaceRadius, colors2, null, Shader.TileMode.MIRROR));
        canvas.drawCircle(mCenterX, mCenterY, mFaceRadius, circleBrush);
    }

    private void drawArrowIndicator(Canvas canvas) {
        Path arrow = new Path();
        // Needle circle in the middle
        arrow.addCircle(mCenterX, mCenterY, mIndicatorRadius, Path.Direction.CW);
        // Move to center to view
        arrow.moveTo(mCenterX, mCenterY);
        // Draw line from center to the left-edge of circle
        arrow.lineTo(mCenterX - mIndicatorRadius, mCenterY);
        // Draw line from left-edge of circle to the top-middle; y-axis top is 6 times the radius
        arrow.lineTo(mCenterX, mCenterY - (mIndicatorRadius * 6));
        // Draw line from top-middle to the right-edge of circle
        arrow.lineTo(mCenterX + mIndicatorRadius, mCenterY);
        // Close from right-edge of circle to the starting position
        arrow.close();
        arrow.computeBounds(new RectF(), true);

        // Rotate arrow as per direction
        Matrix mMatrix = new Matrix();
        mMatrix.setRotate(mDegrees, mCenterX, mCenterY);
        arrow.transform(mMatrix);

        canvas.drawPath(arrow, indicatorBrush);
    }

    private void drawDirections(Canvas canvas) {
        canvas.drawText("N", mCenterX, mEdge + mPadding, textBrush);
        canvas.drawText("W", mEdge, mCenterY + mPadding, textBrush);
        canvas.drawText("E", (mRimRadius * 2) - mEdge, mCenterY + mPadding, textBrush);
        canvas.drawText("S", mCenterX, (mRimRadius * 2) - mEdge + mPadding, textBrush);
    }

    public void updateDirection(float degrees) {
        mDegrees = degrees;
        invalidate();
    }
}
