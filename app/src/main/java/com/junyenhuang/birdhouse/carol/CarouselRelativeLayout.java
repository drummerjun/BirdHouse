package com.junyenhuang.birdhouse.carol;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CarouselRelativeLayout extends RelativeLayout {
    private float scale = CarolOverviewAdapter.BIG_SCALE;

    public CarouselRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CarouselRelativeLayout(Context context) {
        super(context);
    }

    public void setScaleBoth(float scale) {
        this.scale = scale;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // The main mechanism to display scale animation, you can customize it as your needs
        int w = this.getWidth();
        int h = this.getHeight();
        canvas.scale(scale, scale, w/2, h/2);
    }
}