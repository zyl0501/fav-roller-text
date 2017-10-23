package com.ray.rollertext;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by zyl on 2017/10/17.
 */
public class RollerText extends View {
    private static final int DEFAULT_ANIM_DURATION = 500;
    private static final int DEFAULT_TEXT_SIZE = 13;//SP
    private static final int DEFAULT_TEXT_COLOR = Color.BLACK;
    private static final float DEFAULT_ROLL_SPACE_RATIO = 1f;

    private CharSequence oldContent;
    private CharSequence content;
    private int textSize;
    private int textColor;
    private float rollerSpaceRatio;
    private float curRollOffset = 0f; //0~1

    private TextPaint paint;
    private Paint.FontMetrics fontMetrics;

    private int animDuration = DEFAULT_ANIM_DURATION;

    private Comparator<CharSequence> comparator;
    private boolean paintInvalid = false;

    public RollerText(Context context) {
        this(context, null);
    }

    public RollerText(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RollerText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray t = context.obtainStyledAttributes(attrs, R.styleable.RollerText);
        rollerSpaceRatio = t.getFloat(R.styleable.RollerText_rollerSpaceRatio, DEFAULT_ROLL_SPACE_RATIO);
        textSize = t.getDimensionPixelSize(R.styleable.RollerText_android_textSize, ViewUtils.sp2px(context, DEFAULT_TEXT_SIZE));
        textColor = t.getColor(R.styleable.RollerText_android_textColor, DEFAULT_TEXT_COLOR);
        CharSequence text = t.getText(R.styleable.RollerText_android_text);
        t.recycle();
        animDuration = context.getResources().getInteger(R.integer.fav_anim_duration) * 2;

        paint = new TextPaint();
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        if (!TextUtils.isEmpty(text)) {
            content = text;
        }
        fontMetrics = paint.getFontMetrics();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (paintInvalid) {
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            paintInvalid = true;
        }

        if (widthMode == MeasureSpec.AT_MOST) {
            int contentWidth = 0, oldContentWidth = 0;
            if (!TextUtils.isEmpty(content)) {
                contentWidth = (int) paint.measureText(content.toString()) + getPaddingLeft() + getPaddingRight();
            }
            if (!TextUtils.isEmpty(oldContent)) {
                oldContentWidth = (int) paint.measureText(oldContent.toString()) + getPaddingLeft() + getPaddingRight();
            }
            contentWidth = Math.max(oldContentWidth, contentWidth);
            if (contentWidth > 0) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY);
            }
        }
        if (heightMode == MeasureSpec.AT_MOST) {
            height = (int) (paint.getFontSpacing() * (1 + rollerSpaceRatio * 2));
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (content == null) return;
        if (paintInvalid) {
            paint.setTextSize(textSize);
            paint.setColor(textColor);
            paintInvalid = true;
        }

        paint.setTextSize(textSize);
        paint.setColor(textColor);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int left = getPaddingLeft();
        int right = width - getPaddingRight();
        paint.getFontMetrics(fontMetrics);
        float baseLine = centerY - fontMetrics.bottom / 2 - fontMetrics.top / 2;
        float textHeight = paint.getFontSpacing();
        //oldContent为null，第一次绘制
        canvas.save();
        canvas.clipRect(left, 0, right, height);
        if (oldContent == null) {
            canvas.drawText(content.toString(), left, baseLine, paint);
        } else {
            int oldLength = oldContent.length();
            int length = content.length();
            int compareResult = compare(oldContent, content);
            //位数没有变化
            if (oldLength == length) {
                List<Integer> diffIndex = diff(oldContent, content);
                if (diffIndex == null || diffIndex.size() <= 0) {
                    //完全一致，不需要拆分绘制
                    canvas.drawText(content.toString(), left, baseLine, paint);
                } else {
                    int textLeft = left;
                    boolean isUp = compareResult > 0;
                    for (int pre = 0, i = 0, size = diffIndex.size(); i < size; i += 2) {
                        int index = diffIndex.get(i);
                        int nextIndex = diffIndex.get(i + 1);
                        CharSequence subChar = oldContent.subSequence(pre, index);
                        CharSequence rollChar1 = oldContent.subSequence(index, nextIndex);
                        CharSequence rollChar2 = content.subSequence(index, nextIndex);

                        float textWidth = paint.measureText(subChar.toString());
                        paint.setAlpha(255);
                        canvas.drawText(subChar.toString(), textLeft, baseLine, paint);
                        textLeft += textWidth;

                        textWidth = paint.measureText(rollChar1.toString());
                        paint.setAlpha((int) (255 * (1 - curRollOffset)));
                        float ratio = isUp ? -curRollOffset : curRollOffset;
                        canvas.drawText(rollChar1.toString(), textLeft, baseLine + textHeight * rollerSpaceRatio * ratio, paint);

                        paint.setAlpha((int) (255 * curRollOffset));
                        ratio = isUp ? (1 - curRollOffset) : (curRollOffset - 1);
                        canvas.drawText(rollChar2.toString(), textLeft, baseLine + textHeight * rollerSpaceRatio * ratio, paint);
                        textLeft += textWidth;

                        pre = index;
                    }

                    int lastIndex = diffIndex.get(diffIndex.size() - 1);
                    int textSize = content.length();
                    if (lastIndex <= textSize - 1) {
                        CharSequence subChar = content.subSequence(lastIndex, textSize);
                        paint.setAlpha(255);
                        canvas.drawText(subChar.toString(), textLeft, baseLine, paint);
                    }
                }
            } else {
                //位数发生变化
                float x = left;
                boolean isUp = compareResult > 0;
                paint.setAlpha((int) (255 * (1 - curRollOffset)));
                float ratio = isUp ? -curRollOffset : curRollOffset;
                float y = baseLine + textHeight * rollerSpaceRatio * ratio;
                canvas.drawText(oldContent.toString(), x, y, paint);

                paint.setAlpha((int) (255 * curRollOffset));
                ratio = isUp ? (1 - curRollOffset) : (curRollOffset - 1);
                y = baseLine + textHeight * rollerSpaceRatio * ratio;
                canvas.drawText(content.toString(), x, y, paint);
            }
        }
        canvas.restore();
    }

    private int compare(CharSequence oldContent, CharSequence content) {
        if (comparator != null) return comparator.compare(oldContent, content);
        if (oldContent == null && content == null) return 0;
        if (oldContent == null) return 1;
        if (content == null) return -1;
        int length1 = content.length();
        int length2 = oldContent.length();
        if (length1 > length2) return 1;
        if (length1 < length2) return -1;
        return content.toString().compareTo(oldContent.toString());
    }

    /**
     * 获取两个text不同字符的index，两个一组[start,end)
     */
    private List<Integer> diff(CharSequence oldContent, CharSequence content) {
        if (oldContent == null || content == null || oldContent.length() != content.length())
            return null;
        if (TextUtils.equals(content, oldContent)) return null;
        int i = 0, length = oldContent.length();
        List<Integer> result = new ArrayList<>();
        boolean sameFlag = true;
        for (; i < length; i++) {
            char c1 = oldContent.charAt(i);
            char c2 = content.charAt(i);
            if (sameFlag) {
                if (c1 != c2) {
                    result.add(i);
                    sameFlag = false;
                }
            } else {
                if (c1 == c2) {
                    result.add(i);
                    sameFlag = true;
                }
            }
            if (!sameFlag && i == length - 1) {
                result.add(length);
            }
        }
        return result;
    }

    private void startAnim() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setInterpolator(new LinearInterpolator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                curRollOffset = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        anim.setDuration(animDuration);
        anim.start();
    }

    public void setText(CharSequence content) {
        setText(content, true);
    }

    public void setText(CharSequence content, boolean anim) {
        if (!TextUtils.equals(content, this.content)) {
            this.oldContent = this.content;
            this.content = content;
            if (anim) {
                requestLayout();
                startAnim();
            } else {
                curRollOffset = 1;
                requestLayout();
            }
        }
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        paintInvalid = true;
        requestLayout();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        paintInvalid = true;
        invalidate();
    }

    public void setAnimDuration(int animDuration) {
        this.animDuration = animDuration;
    }

    public void setComparator(Comparator<CharSequence> comparator) {
        this.comparator = comparator;
    }
}
