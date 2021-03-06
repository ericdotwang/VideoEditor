package com.github.xch168.videoeditor.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.github.xch168.videoeditor.R;
import com.github.xch168.videoeditor.core.FrameExtractor;
import com.github.xch168.videoeditor.entity.VideoPartInfo;
import com.github.xch168.videoeditor.util.SizeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class EditorTrackView extends FrameLayout {
    private Context mContext;

    private Drawable mCursorDrawable;

    private ImageView mLeftThumb;
    private ImageView mRightThumb;
    private EditorMediaTrackView mMediaTrackView;

    private Paint mBorderPaint;
    private Paint mMaskPaint;

    private int mMinScale = 0;
    private int mMaxScale = 3000;

    private int mBorderHeight;
    private int mHalfThumbWidth;

    private HashMap<Integer, Bitmap> mThumbMap = new HashMap<>();

    private FrameExtractor mFrameExtractor;

    private Rect mBounds = new Rect();
    private Rect mLeftThumbBounds = new Rect();
    private Rect mRightThumbBounds = new Rect();

    private List<VideoPartInfo> mVideoPartInfoList = new ArrayList<>();

    public EditorTrackView(@NonNull Context context) {
        this(context, null);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditorTrackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mBorderHeight = SizeUtil.dp2px(mContext, 1);

        mFrameExtractor = new FrameExtractor();

        initView();
    }

    private void initView() {
        initPaint();
        initUIComponent();
        addUIComponent();

        setWillNotDraw(false);
    }

    private void initPaint() {
        mBorderPaint = new Paint();
        mBorderPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setColor(getResources().getColor(R.color.colorAccent));

        mMaskPaint = new Paint();
        mMaskPaint.setStyle(Paint.Style.FILL);
        mMaskPaint.setColor(getResources().getColor(R.color.colorMask));
    }

    private void initUIComponent() {
        mCursorDrawable = getResources().getDrawable(R.drawable.shape_cursor);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtil.dp2px(mContext, 40));
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mLeftThumb = new ImageView(mContext);
        mLeftThumb.setImageResource(R.drawable.ic_progress_left);
        mLeftThumb.setScaleType(ImageView.ScaleType.FIT_END);
        mLeftThumb.setLayoutParams(layoutParams);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, SizeUtil.dp2px(mContext, 40));
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mRightThumb = new ImageView(mContext);
        mRightThumb.setImageResource(R.drawable.ic_progress_right);
        mRightThumb.setScaleType(ImageView.ScaleType.FIT_START);
        mRightThumb.setLayoutParams(layoutParams);

        layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        mMediaTrackView = new EditorMediaTrackView(mContext, this);
        mMediaTrackView.setThumbMap(mThumbMap);
        mMediaTrackView.setLayoutParams(layoutParams);
        mMediaTrackView.setOnScrollChangeListener(new EditorMediaTrackView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(int scrollX) {
                updateThumbPosition(scrollX);
            }
        });
    }

    private void initCursor() {
        int cursorWidth = SizeUtil.dp2px(mContext, 2);
        mCursorDrawable.setBounds((getWidth() - cursorWidth) / 2, 0, (getWidth() + cursorWidth) / 2, getHeight());
    }

    private void addUIComponent() {
        removeAllViews();

        addView(mMediaTrackView);
        addView(mLeftThumb);
        addView(mRightThumb);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mode = MeasureSpec.getMode(heightMeasureSpec);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(SizeUtil.dp2px(mContext, 54), mode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mHalfThumbWidth = mLeftThumb.getMeasuredWidth() / 2;

        initCursor();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

        }
        mMediaTrackView.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {

        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBorder(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        drawMask(canvas);
        mCursorDrawable.draw(canvas);
    }

    private void drawBorder(Canvas canvas) {
        mLeftThumb.getHitRect(mLeftThumbBounds);
        mRightThumb.getHitRect(mRightThumbBounds);
        // top
        canvas.drawRect(mLeftThumbBounds.right, mLeftThumbBounds.top, mRightThumbBounds.left,  mRightThumbBounds.top + mBorderHeight, mBorderPaint);
        // bottom
        canvas.drawRect(mLeftThumbBounds.right, mLeftThumbBounds.bottom - mBorderHeight, mRightThumbBounds.left, mLeftThumbBounds.bottom, mBorderPaint);
    }

    private void drawMask(Canvas canvas) {
        // left
        mLeftThumb.getHitRect(mLeftThumbBounds);
        canvas.drawRect(-mMediaTrackView.getScrollX(), mLeftThumbBounds.top + mBorderHeight, mLeftThumbBounds.left + mHalfThumbWidth, mLeftThumbBounds.bottom - mBorderHeight, mMaskPaint);
        // right
        mRightThumb.getHitRect(mRightThumbBounds);
        canvas.drawRect(mRightThumbBounds.right - mHalfThumbWidth, mRightThumbBounds.top + mBorderHeight, mMediaTrackView.getMaxScale() - mMediaTrackView.getScrollX(), mRightThumbBounds.bottom - mBorderHeight, mMaskPaint);
    }

    private void setBounds(Rect bounds, int left, int top, int right, int bottom) {
        bounds.left = left;
        bounds.top = top;
        bounds.right = right;
        bounds.bottom = bottom;
    }

    private void updateThumbPosition(int scrollX) {
        VideoPartInfo currentPartInfo = getCurrentPartInfo();
        if (currentPartInfo != null) {
            moveLeftThumb(currentPartInfo.getStartScale() - scrollX);
            moveRightThumb(currentPartInfo.getEndScale() - scrollX);
        }
    }

    private VideoPartInfo getCurrentPartInfo() {
        for (int i = 0; i < mVideoPartInfoList.size(); i++) {
            VideoPartInfo videoPartInfo = mVideoPartInfoList.get(i);
            if (videoPartInfo.inScaleRange(mMediaTrackView.getCurrentScale())) {
                return videoPartInfo;
            }
        }
        return null;
    }

    private void moveLeftThumb(int x) {
        mLeftThumb.setTag(x);
        mLeftThumb.setX(x - mLeftThumb.getWidth());
    }

    private void moveRightThumb(int x) {
        mRightThumb.setTag(x);
        mRightThumb.setX(x);
    }

    private int getLeftThumbPosition() {
        Object tag = mLeftThumb.getTag();
        if (tag instanceof Integer) {
            return (Integer) tag;
        }
        return 0;
    }

    private int getRightThumbPosition() {
        Object tag = mRightThumb.getTag();
        if (tag instanceof Integer) {
            return (Integer) tag;
        }
        return 0;
    }

    public void update() {
        updateThumbPosition(mMediaTrackView.getScrollX());
        invalidate();
    }

    public void setVideoPath(String videoPath) {
        mFrameExtractor.setDataSource(videoPath);
        mFrameExtractor.setDstSize(mMediaTrackView.getItemSize(), mMediaTrackView.getItemSize());
        mFrameExtractor.getFrameByInterval(5000, new FrameExtractor.Callback() {
            @Override
            public void onFrameExtracted(Bitmap bitmap, long timestamp) {
                int index = (int) (timestamp / 5000);
                mThumbMap.put(index, bitmap);
            }
        });
        mMediaTrackView.setItemCount((int) (mFrameExtractor.getVideoDuration() / 5000));

        VideoPartInfo videoPartInfo = new VideoPartInfo();
        videoPartInfo.setStartTime(0);
        videoPartInfo.setEndTime((int) mFrameExtractor.getVideoDuration());
        videoPartInfo.setStartScale(0);
        videoPartInfo.setEndScale(mMediaTrackView.getMaxScale());
        mVideoPartInfoList.add(videoPartInfo);
    }

    public int getMinScale() {
        return mMinScale;
    }

    public void setMinScale(int minScale) {
        this.mMinScale = minScale;
    }

    public int getMaxScale() {
        return mMediaTrackView.getMaxScale();
    }

    public void setCurrentScale(float currentPos) {
        mMediaTrackView.setCurrentScale((int) currentPos);
    }

    public int getCurrentScale() {
        return mMediaTrackView.getCurrentScale();
    }

    public List<VideoPartInfo> getVideoPartInfoList() {
        return mVideoPartInfoList;
    }

    public VideoPartInfo getVideoPartInfo(int index) {
        if (index >= 0 && index < mVideoPartInfoList.size()) {
            return mVideoPartInfoList.get(index);
        }
        return null;
    }

    public HashMap<Integer, Bitmap> getThumbMap() {
        return mThumbMap;
    }

    public void setOnTrackViewChangeListener(EditorMediaTrackView.OnTrackViewChangeListener listener) {
        mMediaTrackView.setOnTrackViewChangeListener(listener);
    }
}
