package io.github.zkhan93.hisab.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by Zeeshan Khan on 7/31/2016.
 */
public class GroupItemDecorator extends RecyclerView.ItemDecoration {
    private Drawable drawable;
    public static String TAG = GroupItemDecorator.class.getSimpleName();

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);

        int left, right, top, bottom;
        left = 0;
        right = parent.getWidth();
        View child = parent.getChildAt(0);
        if (child != null) {
            left = child.getPaddingLeft();
            right -= child.getPaddingRight();
        }
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            top = child.getBottom() + params.bottomMargin;
            bottom = top + drawable.getIntrinsicHeight();
            Log.d(TAG, "group::"+left + " " + top + " " + right + " " + bottom);
            drawable.setBounds(left, top, right, bottom);
            drawable.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
            state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = drawable.getIntrinsicHeight();
    }

    public GroupItemDecorator(Drawable drawable) {
        super();
        this.drawable = drawable;
    }
}
