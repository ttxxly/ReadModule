package com.ttxxly.demo.readmodule;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

/**
 * description:
 *
 * @author Administrator
 * @date 2018-04-22
 * mail ttxxly@gmail.com
 */
public class DrawTextView extends View {

    protected int mScreenWidth;
    protected int mScreenHeight;

    protected PageFactory pagefactory = null;


    public DrawTextView(Context context, int mScreenWidth, int mScreenHeight) {
        super(context);
        this.mScreenWidth = mScreenWidth;
        this.mScreenHeight = mScreenHeight;
        init();
    }

    public synchronized void init() {

        pagefactory = new PageFactory(getContext(), mScreenWidth, mScreenHeight, 20);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        pagefactory.onDraw(canvas);
    }
}
