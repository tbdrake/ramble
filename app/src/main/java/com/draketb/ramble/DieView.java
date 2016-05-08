package com.draketb.ramble;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by draketb on 4/28/16.
 */
public class DieView extends TextView {
    public DieView(Context context) {
        super(context);
    }

    public DieView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DieView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Force view to be square
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
