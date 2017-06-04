package io.github.zkhan93.hisab.ui;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;

/**
 * Created by zeeshan on 31/5/17.
 */

public class ExpenseItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable dividerDrawable;
    private int leftOffset;
    private int rightOffset;
    public static final String TAG = ExpenseItemDecoration.class.getSimpleName();

    @SuppressWarnings("deprecation")
    public ExpenseItemDecoration(Resources resources, int dividerId, Resources.Theme theme) {
        if (Build.VERSION.SDK_INT < 21) {
            dividerDrawable = resources.getDrawable(dividerId);
        } else {
            dividerDrawable = resources.getDrawable(dividerId, theme);
        }

        DisplayMetrics metrics = resources.getDisplayMetrics();
        leftOffset = (int) (metrics.density * 16); // leave icon and padding
        rightOffset = (int) (metrics.density * 16); //leave the item padding from right
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount() - 1;

        for (int i = 1; i < childCount; i++) {

            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int ty = (int) (child.getTranslationY() + 0.5f);

            int top = child.getBottom() + params.bottomMargin + ty;
            int bottom = top + dividerDrawable.getIntrinsicHeight();

            dividerDrawable.setBounds(left + leftOffset, top, right - rightOffset, bottom);
            dividerDrawable.draw(canvas);

        }
    }
}
