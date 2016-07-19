package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class EmptyVH extends RecyclerView.ViewHolder {
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.msg)
    TextView msg;
    @BindView(R.id.suggestion)
    TextView suggestion;

    Context context;

    public EmptyVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        context = itemView.getContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.big_groups));
        else
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.big_groups,
                    context.getTheme()));
        msg.setText(context.getString(R.string.msg_no_groups));
        suggestion.setText(context.getString(R.string.msg_no_groups_suggestion));
    }
}
