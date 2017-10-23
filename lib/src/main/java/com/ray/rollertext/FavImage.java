package com.ray.rollertext;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

/**
 * Created by zyl on 2017/10/19.
 */
public class FavImage extends ViewGroup {
    private int duration;

    private Animation selectInAnim;
    private Animation selectOutAnim;
    private Animation unSelectInAnim;
    private Animation unSelectOutAnim;
    private Animation shiningInAnim;
    private Animation shiningOutAnim;
    private Animation circleAnim;

    private Animation scalePressAnim;
    private Animation scaleReleaseAnim;

    private ImageView unSelectedImg;
    private ImageView selectedImg;
    private ImageView shiningImg;
    private ImageView circleImg;

    private Runnable showSelectAnimRunnable;
    private Runnable showSelectShiningAnimRunnable;
    private Runnable showUnSelectAnimRunnable;
    private boolean isSelectBlocking = false;

    public FavImage(Context context) {
        this(context, null);
    }

    public FavImage(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FavImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        duration = context.getResources().getInteger(R.integer.fav_anim_duration);

        selectInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_zoom_in);
        unSelectInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_zoom_in);
        shiningInAnim = AnimationUtils.loadAnimation(context, R.anim.shining_zoom_in);
        scaleReleaseAnim = AnimationUtils.loadAnimation(context, R.anim.press_zoom_in);
        circleAnim = AnimationUtils.loadAnimation(context, R.anim.circle_shining);

        selectOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_zoom_out);
        unSelectOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_zoom_out);
        shiningOutAnim = AnimationUtils.loadAnimation(context, R.anim.shining_zoom_out);
        scalePressAnim = AnimationUtils.loadAnimation(context, R.anim.press_zoom_out);

        showSelectAnimRunnable = new Runnable() {
            @Override
            public void run() {
                selectedImg.startAnimation(selectInAnim);
                circleImg.startAnimation(circleAnim);
            }
        };
        showSelectShiningAnimRunnable = new Runnable() {
            @Override
            public void run() {
                shiningImg.startAnimation(shiningInAnim);
            }
        };
        showUnSelectAnimRunnable = new Runnable() {
            @Override
            public void run() {
                unSelectedImg.startAnimation(unSelectInAnim);
            }
        };
        setClickable(true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Context context = getContext();
        unSelectedImg = new ImageView(context);
        selectedImg = new ImageView(context);
        shiningImg = new ImageView(context);
        circleImg = new ImageView(context);

        unSelectedImg.setImageResource(R.drawable.ic_messages_like_unselected);
        selectedImg.setImageResource(R.drawable.ic_messages_like_selected);
        shiningImg.setImageResource(R.drawable.ic_messages_like_selected_shining);
        circleImg.setImageResource(R.drawable.ic_messages_like_shining_circle);

        addView(unSelectedImg);
        addView(selectedImg);
        addView(shiningImg);
        addView(circleImg);

        unSelectedImg.setVisibility(VISIBLE);
        selectedImg.setVisibility(GONE);
        shiningImg.setVisibility(GONE);
        circleImg.setVisibility(GONE);

        selectInAnim.setAnimationListener(new InAnimationListener(selectedImg));
        unSelectInAnim.setAnimationListener(new InAnimationListener(unSelectedImg));
        shiningInAnim.setAnimationListener(new InAnimationListener(shiningImg));

        selectOutAnim.setAnimationListener(new OutAnimationListener(selectedImg));
        unSelectOutAnim.setAnimationListener(new OutAnimationListener(unSelectedImg));
        shiningOutAnim.setAnimationListener(new OutAnimationListener(shiningImg));
        circleAnim.setAnimationListener(new OutAnimationListener(circleImg));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = r - l;
        int height = b - t;

        int width50 = width / 2;
        int height50 = height / 2;

        int maxSize = Math.min(width, height);

        int selSize = (int) (maxSize * 0.6);
        int shiningSize = (int) (maxSize * 0.4);
        int circleSize = (int) (maxSize * 0.8);

        int selSize50 = selSize / 2;
        int shiningSize50 = shiningSize / 2;
        int circleSize50 = circleSize / 2;


        selectedImg.layout(width50 - selSize50, (int) (0.5 * height - selSize50), width50 + selSize50, (int) (0.5 * height + selSize50));
        unSelectedImg.layout(width50 - selSize50, (int) (0.5 * height - selSize50), width50 + selSize50, (int) (0.5 * height + selSize50));
        shiningImg.layout(width50 - shiningSize50, (int) (0.2 * height - shiningSize50), width50 + shiningSize50, (int) (0.2 * height + shiningSize50));
        circleImg.layout(width50 - circleSize50, (int) (0.5 * height - circleSize50), width50 + circleSize50, (int) (0.5 * height + circleSize50));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected != isSelected()) {
            if (!isSelectBlocking) {
                setSelected(selected, true);
            }
        }
        super.setSelected(selected);
    }

    public void setSelected(boolean selected, boolean anim) {
        isSelectBlocking = true;
        setSelected(selected);
        isSelectBlocking = false;

        if (anim) {
            showReleaseInnerAnim(selected);
        } else {
            if (selected) {
                selectedImg.setVisibility(VISIBLE);
                shiningImg.setVisibility(VISIBLE);
                unSelectedImg.setVisibility(GONE);
                circleImg.setVisibility(GONE);
            } else {
                selectedImg.setVisibility(GONE);
                shiningImg.setVisibility(GONE);
                unSelectedImg.setVisibility(VISIBLE);
                circleImg.setVisibility(GONE);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startAnimation(scalePressAnim);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (scalePressAnim.hasEnded()) {
                    startAnimation(scaleReleaseAnim);
                } else {
                    if (scalePressAnim.hasStarted()) {
                        scalePressAnim.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                startAnimation(scaleReleaseAnim);
                                scalePressAnim.setAnimationListener(null);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });
                    } else {
                        startAnimation(scaleReleaseAnim);
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void showReleaseInnerAnim(boolean fav) {
        resetAnim();
        if (fav) {
            unSelectedImg.startAnimation(unSelectOutAnim);
            postDelayed(showSelectAnimRunnable, unSelectOutAnim.getDuration());
            postDelayed(showSelectShiningAnimRunnable, selectInAnim.getDuration());
        } else {
            selectedImg.startAnimation(selectOutAnim);
            shiningImg.startAnimation(shiningOutAnim);
            postDelayed(showUnSelectAnimRunnable, selectOutAnim.getDuration());
        }
    }

    private void resetAnim() {
        selectedImg.clearAnimation();
        unSelectedImg.clearAnimation();
        shiningImg.clearAnimation();
        circleImg.clearAnimation();

        selectedImg.setVisibility(GONE);
        unSelectedImg.setVisibility(GONE);
        shiningImg.setVisibility(GONE);
        circleImg.setVisibility(GONE);

        removeCallbacks(showSelectAnimRunnable);
        removeCallbacks(showUnSelectAnimRunnable);
        removeCallbacks(showSelectShiningAnimRunnable);
    }

    private class InAnimationListener implements Animation.AnimationListener {
        View view;

        InAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class OutAnimationListener implements Animation.AnimationListener {
        View view;

        OutAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
            view.setVisibility(VISIBLE);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }
}
