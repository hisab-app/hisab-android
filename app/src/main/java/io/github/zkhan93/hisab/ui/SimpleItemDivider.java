package io.github.zkhan93.hisab.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 7/31/2016.
 */
public class SimpleItemDivider extends RecyclerView.ItemDecoration {
    private Drawable drawable;
    public static String TAG = SimpleItemDivider.class.getSimpleName();

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        View view = parent.getChildAt(1);
        int dividerLeft = 2 * parent.getResources().getDimensionPixelSize(R.dimen
                .horizontal_space_from_edge);
        int dividerRight = parent.getWidth();
        if (view != null) {
            View image = view.findViewById(R.id.image);
            if (image != null)
                dividerLeft += image.getWidth();
            dividerRight -= view.getPaddingRight();
        }

        int childCount = parent.getChildCount();
        for (int i = 1; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + drawable.getIntrinsicHeight();
            drawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            Log.d(TAG, dividerLeft + " " + dividerTop + " " + dividerRight + " " + dividerBottom);
            drawable.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State
            state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }
        outRect.bottom = drawable.getIntrinsicHeight();
    }

    public SimpleItemDivider(Drawable drawable) {
        super();
        this.drawable = drawable;
    }
}
