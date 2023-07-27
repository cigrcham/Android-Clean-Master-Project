package com.phonecleaner.storagecleaner.cache.utils;//package com.example.android.filemanager.utils;
//
//import android.content.Context;
//import android.content.res.ColorStateList;
//import android.content.res.TypedArray;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Rect;
//import android.graphics.RectF;
//import android.util.AttributeSet;
//
//import androidx.annotation.ColorInt;
//import androidx.appcompat.widget.AppCompatTextView;
//
//import com.example.android.filemanager.R;
//
///**
// * Created by Thinhvh on 23/11/2022.
// * Phone: 0398477967
// * Email: thinhvh.fpt@gmail.com
// */
//
//public class CircleTextProgressbar extends AppCompatTextView {
//
//    private int outLineColor = Color.BLACK;
//    private int outLineWidth = 0;
//    private ColorStateList inCircleColors = ColorStateList.valueOf(Color.TRANSPARENT);
//    private int circleColor;
//    private int progressLineColor = Color.BLUE;
//    private int progressLineWidth = ViewUtils.INSTANCE.dpToPx(12);
//    private final Paint mPaint = new Paint();
//    private final RectF mArcRect = new RectF();
//    private int progress = 50;
//    private ProgressType mProgressType = ProgressType.COUNT_BACK;
//    private long timeMillis = 3000;
//    final Rect bounds = new Rect();
//    private OnCountdownProgressListener mCountdownProgressListener;
//    private int listenerWhat = 0;
//
//    public CircleTextProgressbar(Context context) {
//        this(context, null);
//    }
//
//    public CircleTextProgressbar(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//    }
//
//    public CircleTextProgressbar(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        initialize(context, attrs);
//    }
//
//    private void initialize(Context context, AttributeSet attributeSet) {
//        mPaint.setAntiAlias(true);
//        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.CircleTextProgressbar);
//        if (typedArray.hasValue(R.styleable.CircleTextProgressbar_in_circle_color))
//            inCircleColors = typedArray.getColorStateList(R.styleable.CircleTextProgressbar_in_circle_color);
//        else
//            inCircleColors = ColorStateList.valueOf(Color.TRANSPARENT);
//        circleColor = inCircleColors.getColorForState(getDrawableState(), Color.TRANSPARENT);
//        typedArray.recycle();
//    }
//
//    public void setOutLineColor(@ColorInt int outLineColor) {
//        this.outLineColor = outLineColor;
//        invalidate();
//    }
//
//    public void setOutLineWidth(@ColorInt int outLineWidth) {
//        this.outLineWidth = outLineWidth;
//        invalidate();
//    }
//
//    public void setInCircleColor(@ColorInt int inCircleColor) {
//        this.inCircleColors = ColorStateList.valueOf(inCircleColor);
//        invalidate();
//    }
//
//    private void validateCircleColor() {
//        int circleColorTemp = inCircleColors.getColorForState(getDrawableState(), Color.TRANSPARENT);
//        if (circleColor != circleColorTemp) {
//            circleColor = circleColorTemp;
//            invalidate();
//        }
//    }
//
//    public void setProgressColor(@ColorInt int progressLineColor) {
//        this.progressLineColor = progressLineColor;
//        invalidate();
//    }
//
//    public void setProgressLineWidth(int progressLineWidth) {
//        this.progressLineWidth = progressLineWidth;
//        invalidate();
//    }
//
//    public void setProgress(int progress) {
//        this.progress = validateProgress(progress);
//        invalidate();
//    }
//
//    private int validateProgress(int progress) {
//        if (progress > 100)
//            progress = 100;
//        else if (progress < 0)
//            progress = 0;
//        return progress;
//    }
//
//    public int getProgress() {
//        return progress;
//    }
//
//    public void setTimeMillis(long timeMillis) {
//        this.timeMillis = timeMillis;
//        invalidate();
//    }
//
//    public long getTimeMillis() {
//        return this.timeMillis;
//    }
//
//    public void setProgressType(ProgressType progressType) {
//        this.mProgressType = progressType;
//        resetProgress();
//        invalidate();
//    }
//
//    private void resetProgress() {
//        switch (mProgressType) {
//            case COUNT:
//                progress = 0;
//                break;
//            case COUNT_BACK:
//                progress = 100;
//                break;
//        }
//    }
//
//    public ProgressType getProgressType() {
//        return mProgressType;
//    }
//
//    public void setCountdownProgressListener(int what, OnCountdownProgressListener mCountdownProgressListener) {
//        this.listenerWhat = what;
//        this.mCountdownProgressListener = mCountdownProgressListener;
//    }
//
//    public void start() {
//        stop();
//        post(progressChangeTask);
//    }
//
//    public void reStart() {
//        resetProgress();
//        start();
//    }
//
//    public void stop() {
//        removeCallbacks(progressChangeTask);
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        getDrawingRect(bounds);
//
//        int size = bounds.height() > bounds.width() ? bounds.width() : bounds.height();
//        float outerRadius = size / 2;
//
//        int circleColor = inCircleColors.getColorForState(getDrawableState(), 0);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setColor(circleColor);
//        mPaint.setStrokeWidth(progressLineWidth);
//        int deleteW = progressLineWidth + outLineWidth;
//        mArcRect.set(bounds.left + deleteW / 2, bounds.top + deleteW / 2, bounds.right - deleteW / 2, bounds.bottom - deleteW / 2);
//        canvas.drawArc(mArcRect, 0, 360, false, mPaint);
//
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(outLineWidth);
//        mPaint.setColor(circleColor);
//        canvas.drawCircle(bounds.centerX(), bounds.centerY(), outerRadius - outLineWidth / 2, mPaint);
//
//        Paint paint = getPaint();
//        paint.setColor(getCurrentTextColor());
//        paint.setAntiAlias(true);
//        paint.setTextAlign(Paint.Align.CENTER);
//        float textY = bounds.centerY() - (paint.descent() + paint.ascent()) / 2;
//        canvas.drawText(getText().toString(), bounds.centerX(), textY, paint);
//
//        progressLineColor = Color.parseColor("#8CC7FB");
//        mPaint.setColor(progressLineColor);
//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(progressLineWidth);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);
//        int deleteWidth = progressLineWidth + outLineWidth;
//        mArcRect.set(bounds.left + deleteWidth / 2, bounds.top + deleteWidth / 2, bounds.right - deleteWidth / 2, bounds.bottom - deleteWidth / 2);
//
//        canvas.drawArc(mArcRect, 270, 360 * progress / 100, false, mPaint);
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int lineWidth = 4 * (outLineWidth + progressLineWidth);
//        int width = getMeasuredWidth();
//        int height = getMeasuredHeight();
//        int size = (width > height ? width : height) + lineWidth;
//        setMeasuredDimension(size, size);
//    }
//
//    @Override
//    protected void drawableStateChanged() {
//        super.drawableStateChanged();
//        validateCircleColor();
//    }
//
//    private final Runnable progressChangeTask = new Runnable() {
//        @Override
//        public void run() {
//            removeCallbacks(this);
//            switch (mProgressType) {
//                case COUNT:
//                    progress += 1;
//                    break;
//                case COUNT_BACK:
//                    progress -= 1;
//                    break;
//            }
//            if (progress >= 0 && progress <= 100) {
//                if (mCountdownProgressListener != null)
//                    mCountdownProgressListener.onProgress(listenerWhat, progress);
//                invalidate();
//                postDelayed(progressChangeTask, timeMillis / 100);
//            } else
//                progress = validateProgress(progress);
//        }
//    };
//
//    public enum ProgressType {
//        COUNT,
//        COUNT_BACK
//    }
//
//    public interface OnCountdownProgressListener {
//        void onProgress(int what, int progress);
//    }
//}