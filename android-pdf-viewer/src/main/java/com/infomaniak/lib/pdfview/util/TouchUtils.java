package com.infomaniak.lib.pdfview.util;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.recyclerview.widget.RecyclerView;

public class TouchUtils {

    private TouchUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void handleTouchPriority(MotionEvent event, View view, int pointerCount, boolean shouldOverrideTouchPriority) {
        ViewParent viewToDisableTouch = getViewToDisableTouch(view);
        boolean canScrollHorizontally = view.canScrollHorizontally(1) && view.canScrollHorizontally(-1);
        boolean canScrollVertically = view.canScrollVertically(1) && view.canScrollVertically(-1);
        if (shouldOverrideTouchPriority) {
            viewToDisableTouch.requestDisallowInterceptTouchEvent(false);
        } else if (viewToDisableTouch != null && (event.getPointerCount() >= pointerCount || canScrollHorizontally || canScrollVertically)) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                viewToDisableTouch.requestDisallowInterceptTouchEvent(false);
            } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
                viewToDisableTouch.requestDisallowInterceptTouchEvent(true);
            }
        }
    }

    private static ViewParent getViewToDisableTouch(View startingView) {
        ViewParent parentView = startingView.getParent();
        while (parentView != null && !(parentView instanceof RecyclerView)) {
            parentView = parentView.getParent();
        }
        return parentView;
    }
}
