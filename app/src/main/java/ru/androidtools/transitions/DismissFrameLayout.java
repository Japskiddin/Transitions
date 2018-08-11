package ru.androidtools.transitions;

import android.annotation.TargetApi;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by huang on 2/15/17.
 */

public class DismissFrameLayout extends FrameLayout {
  private SwipeGestureDetector swipeGestureDetector;
  private OnDismissListener dismissListener;
  private int initHeight;
  private int initWidth;
  private int initLeft = 0;
  private int initTop = 0;

  public DismissFrameLayout(@NonNull Context context) {
    super(context);
    init();
  }

  public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @TargetApi(21) public DismissFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
    init();
  }

  private void init() {
    swipeGestureDetector =
        new SwipeGestureDetector(getContext(), new SwipeGestureDetector.OnSwipeGestureListener() {
          @Override public void onSwipeTopBottom(float deltaX, float deltaY) {
            dragChildView(deltaX, deltaY);
          }

          @Override public void onSwipeLeftRight(float deltaX, float deltaY) {
          }

          @Override public void onFinish(int direction, float distanceX, float distanceY) {
            if (dismissListener != null && direction == SwipeGestureDetector.DIRECTION_TOP_BOTTOM) {
              if (distanceY > initHeight / 10) {
                dismissListener.onDismiss();
              } else {
                dismissListener.onCancel();
                reset();
              }
            }
          }
        });
  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    return swipeGestureDetector.onInterceptTouchEvent(ev);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    return swipeGestureDetector.onTouchEvent(event);
  }

  private void dragChildView(float deltaX, float deltaY) {
    int count = getChildCount();
    if (count > 0) {
      View view = getChildAt(0);
      scaleAndMove(view, deltaX, deltaY);
    }
  }

  private void scaleAndMove(View view, float deltaX, float deltaY) {
    MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
    if (params == null) {
      params = new MarginLayoutParams(view.getWidth(), view.getHeight());
    }
    if (params.width <= 0 && params.height <= 0) {
      params.width = view.getWidth();
      params.height = view.getHeight();
    }
    if (initHeight <= 0) {
      initHeight = view.getHeight();
      initWidth = view.getWidth();
      initLeft = params.leftMargin;
      initTop = params.topMargin;
    }
    float percent = deltaY / getHeight();
    int scaleX = (int) (initWidth * percent);
    int scaleY = (int) (initHeight * percent);
    params.width = params.width - scaleX;
    params.height = params.height - scaleY;
    params.leftMargin += (calXOffset(deltaX) + scaleX / 2);
    params.topMargin += (calYOffset(deltaY) + scaleY / 2);
    view.setLayoutParams(params);
    if (dismissListener != null) {
      dismissListener.onScaleProgress(percent);
    }
  }

  private int calXOffset(float deltaX) {
    return (int) deltaX;
  }

  private int calYOffset(float deltaY) {
    return (int) deltaY;
  }

  private void reset() {
    int count = getChildCount();
    if (count > 0) {
      View view = getChildAt(0);
      MarginLayoutParams params = (MarginLayoutParams) view.getLayoutParams();
      params.width = initWidth;
      params.height = initHeight;
      params.leftMargin = initLeft;
      params.topMargin = initTop;
      view.setLayoutParams(params);
    }
  }

  public void setDismissListener(OnDismissListener dismissListener) {
    this.dismissListener = dismissListener;
  }

  public interface OnDismissListener {
    void onScaleProgress(float scale);

    void onDismiss();

    void onCancel();
  }
}
