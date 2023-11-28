package com.infomaniak.lib.pdfview.scroll;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.infomaniak.lib.pdfview.PDFView;
import com.infomaniak.lib.pdfview.R;
import com.infomaniak.lib.pdfview.util.Util;

public class DefaultScrollHandle extends RelativeLayout implements ScrollHandle {

    private final static int HANDLE_WIDTH = 65;
    private final static int HANDLE_HEIGHT = 40;
    private final static int DEFAULT_TEXT_SIZE = 12;

    private final Handler handler = new Handler();
    private final boolean inverted;
    private final Runnable hidePageScrollerRunnable = this::hide;
    protected TextView textView;
    protected Context context;
    private float relativeHandlerMiddle = 0f;
    private PDFView pdfView;
    private float currentPos;
    private Drawable handleBackgroundDrawable;
    private View handleView;
    private int handleAlign;
    private int handleWidth = HANDLE_WIDTH;
    private int handleHeight = HANDLE_HEIGHT;
    private int handlePaddingLeft = 0;
    private int handlePaddingTop = 0;
    private int handlePaddingRight = 0;
    private int handlePaddingBottom = 0;

    private int hideHandleDelayMillis = 1000;

    public DefaultScrollHandle(Context context) {
        this(context, false);
    }

    public DefaultScrollHandle(Context context, boolean inverted) {
        super(context);
        this.context = context;
        this.inverted = inverted;
        textView = new TextView(context);
        setVisibility(INVISIBLE);
        setTextColor(Color.BLACK);
        setTextSize(DEFAULT_TEXT_SIZE);
    }

    @Override
    public void setupLayout(PDFView pdfView) {
        // determine handler position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical()) {
            if (inverted) { // left
                handleAlign = ALIGN_PARENT_LEFT;
            } else { // right
                handleAlign = ALIGN_PARENT_RIGHT;
            }
        } else {
            int tempWidth = handleWidth;
            handleWidth = handleHeight;
            handleHeight = tempWidth;
            if (inverted) { // top
                handleAlign = ALIGN_PARENT_TOP;
            } else { // bottom
                handleAlign = ALIGN_PARENT_BOTTOM;
            }
        }

        setHandleView();
        pdfView.addView(this);
        this.pdfView = pdfView;
    }

    private void setHandleView() {
        if (handleBackgroundDrawable != null) {
            setBackground(handleBackgroundDrawable);
        } else if (handleView != null) {
            initWithCustomView();
        } else {
            initDefaultView();
        }
    }

    private ViewParent getViewToDisableTouch(View startingView) {
        ViewParent parentView = startingView.getParent();
        while (parentView != null && !(parentView instanceof RecyclerView)) {
            parentView = parentView.getParent();
        }
        return parentView;
    }

    private void handleTouchPriority(MotionEvent event, View view) {
        ViewParent viewToDisableTouch = getViewToDisableTouch(view);
        boolean canScrollHorizontally = view.canScrollHorizontally(1) && view.canScrollHorizontally(-1);
        boolean canScrollVertically = view.canScrollVertically(1) && view.canScrollVertically(-1);
        if (viewToDisableTouch != null && (event.getPointerCount() >= 1 || canScrollHorizontally || canScrollVertically)) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_MOVE) {
                viewToDisableTouch.requestDisallowInterceptTouchEvent(true);
            } else if (action == MotionEvent.ACTION_UP) {
                viewToDisableTouch.requestDisallowInterceptTouchEvent(false);
            }
        }
    }

    private void initDefaultView() {
        int handleBackground;
        handleBackground = switch (handleAlign) {
            case ALIGN_PARENT_LEFT -> R.drawable.default_scroll_handle_left;
            case ALIGN_PARENT_RIGHT -> R.drawable.default_scroll_handle_right;
            case ALIGN_PARENT_TOP -> R.drawable.default_scroll_handle_top;
            default -> R.drawable.default_scroll_handle_bottom;
        };

        setBackground(getDrawable(handleBackground));

        LayoutParams rootLayoutParams = new LayoutParams(Util.getDP(context, handleWidth), Util.getDP(context,
                handleHeight));
        rootLayoutParams.addRule(handleAlign);
        setLayoutParams(rootLayoutParams);

        LayoutParams textviewLayoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        textviewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        addView(textView, textviewLayoutParams);
    }

    private void initWithCustomView() {
        textView = handleView.findViewWithTag("pageIndicator");

        LayoutParams lp = new LayoutParams(Util.getDP(context, handleWidth), Util.getDP(context, handleHeight));
        lp.setMargins(
                Util.getDP(context, handlePaddingLeft),
                Util.getDP(context, handlePaddingTop),
                Util.getDP(context, handlePaddingRight),
                Util.getDP(context, handlePaddingBottom)
        );
        addView(handleView, lp);

        LayoutParams rootLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        rootLayoutParams.addRule(handleAlign);
        setLayoutParams(rootLayoutParams);
    }

    private Drawable getDrawable(int resDrawable) {
        return ContextCompat.getDrawable(context, resDrawable);
    }

    @Override
    public void destroyLayout() {
        pdfView.removeView(this);
    }

    @Override
    public void setScroll(float position) {
        if (!shown()) {
            show();
        } else {
            handler.removeCallbacks(hidePageScrollerRunnable);
        }
        if (pdfView != null) {
            setPosition((pdfView.isSwipeVertical() ? pdfView.getHeight() : pdfView.getWidth()) * position);
        }
    }

    private void setPosition(float pos) {
        if (Float.isInfinite(pos) || Float.isNaN(pos)) {
            return;
        }
        float pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pdfViewSize = pdfView.getHeight();
        } else {
            pdfViewSize = pdfView.getWidth();
        }
        pos -= relativeHandlerMiddle;

        if (pos < 0) {
            pos = 0;
        } else if (pos > pdfViewSize - Util.getDP(context, handleHeight) - Util.getDP(context, getPaddings())) {
            pos = pdfViewSize - Util.getDP(context, handleHeight) - Util.getDP(context, getPaddings());
        }

        if (pdfView.isSwipeVertical()) {
            setY(pos);
        } else {
            setX(pos);
        }

        calculateMiddle();
        invalidate();
    }

    private int getPaddings() {
        return switch (handleAlign) {
            case ALIGN_PARENT_LEFT, ALIGN_PARENT_RIGHT -> handlePaddingTop + handlePaddingBottom;
            case ALIGN_PARENT_TOP, ALIGN_PARENT_BOTTOM -> handlePaddingLeft + handlePaddingRight;
            default -> 0;
        };
    }

    private void calculateMiddle() {
        float pos, viewSize, pdfViewSize;
        if (pdfView.isSwipeVertical()) {
            pos = getY();
            viewSize = getHeight();
            pdfViewSize = pdfView.getHeight();
        } else {
            pos = getX();
            viewSize = getWidth();
            pdfViewSize = pdfView.getWidth();
        }
        relativeHandlerMiddle = ((pos + relativeHandlerMiddle) / pdfViewSize) * viewSize;
    }

    @Override
    public void hideDelayed() {
        handler.postDelayed(hidePageScrollerRunnable, hideHandleDelayMillis);
    }

    @Override
    public void setPageNum(int pageNum) {
        String text = String.valueOf(pageNum);
        if (!textView.getText().equals(text)) {
            textView.setText(text);
        }
    }

    @Override
    public boolean shown() {
        return getVisibility() == VISIBLE;
    }

    @Override
    public void show() {
        setVisibility(VISIBLE);
    }

    @Override
    public void hide() {
        setVisibility(INVISIBLE);
    }

    /**
     * @param color color of the text handle
     */
    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    /**
     * @param size text size in dp
     */
    public void setTextSize(int size) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
    }

    /**
     * @param handleBackgroundDrawable drawable to set as a background
     */
    public void setPageHandleBackground(Drawable handleBackgroundDrawable) {
        this.handleBackgroundDrawable = handleBackgroundDrawable;
    }

    /**
     * Use a custom view as the handle. Note that this view should have a TextView with a specific tag,
     * "pageIndicator", in order to display the current page
     * @param handleView view to set as the handle
     */
    public void setPageHandleView(View handleView) {
        this.handleView = handleView;
    }

    /**
     * @param handleWidth width of the handle
     * @param handleHeight width of the handle
     */
    public void setHandleSize(int handleWidth, int handleHeight) {
        this.handleWidth = handleWidth;
        this.handleHeight = handleHeight;
    }

    /**
     * @param paddingLeft left padding of the handle
     * @param paddingTop top padding of the handle
     * @param paddingRight right padding of the handle
     * @param paddingBottom bottom padding of the handle
     */
    public void setHandlePaddings(int paddingLeft, int paddingTop,  int paddingRight, int paddingBottom) {
        handlePaddingLeft = paddingLeft;
        handlePaddingTop = paddingTop;
        handlePaddingRight = paddingRight;
        handlePaddingBottom = paddingBottom;
    }

    /**
     * @param hideHandleDelayMillis delay in milliseconds to hide the handle after scrolling the PDF
     */
    public void setHideHandleDelay(int hideHandleDelayMillis) {
        this.hideHandleDelayMillis = hideHandleDelayMillis;
    }

    private boolean isPDFViewReady() {
        return pdfView != null && pdfView.getPageCount() > 0 && !pdfView.documentFitsView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isPDFViewReady()) {
            return super.onTouchEvent(event);
        }

        handleTouchPriority(event, this);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                pdfView.stopFling();
                handler.removeCallbacks(hidePageScrollerRunnable);
                if (pdfView.isSwipeVertical()) {
                    currentPos = event.getRawY() - getY();
                } else {
                    currentPos = event.getRawX() - getX();
                }
            case MotionEvent.ACTION_MOVE:
                if (pdfView.isSwipeVertical()) {
                    setPosition(event.getRawY() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getHeight(), false);
                } else {
                    setPosition(event.getRawX() - currentPos + relativeHandlerMiddle);
                    pdfView.setPositionOffset(relativeHandlerMiddle / (float) getWidth(), false);
                }
                return true;
            case MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP:
                hideDelayed();
                pdfView.performPageSnap();
                return true;
        }

        return super.onTouchEvent(event);
    }
}
