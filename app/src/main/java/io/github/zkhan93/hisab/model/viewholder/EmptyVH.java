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
    }

    public void setType(int type) {
        int drawables, msgId, suggestionId;
        switch (type) {
            case TYPE.EXPENSE:
                drawables = R.drawable.big_item;
                suggestionId = R.string.msg_no_expenses_suggestion;
                msgId = R.string.msg_no_expenses;
                break;
            case TYPE.GROUP:
                drawables = R.drawable.big_groups;
                suggestionId = R.string.msg_no_groups_suggestion;
                msgId = R.string.msg_no_groups;
                break;
            case TYPE.USERS:
                drawables = R.drawable.big_user;
                suggestionId = R.string.msg_no_users_suggestion;
                msgId = R.string.msg_no_users;
                break;
            case TYPE.GENERAL:
            default:
                drawables = R.drawable.empty_glass;
                suggestionId = R.string.empty;
                msgId = R.string.msg_no_expenses;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            image.setImageDrawable(context.getResources().getDrawable(drawables));
        else
            image.setImageDrawable(context.getDrawable(drawables));
        msg.setText(context.getString(msgId));
        suggestion.setText(context.getString(suggestionId));
    }

    public interface TYPE {
        int GENERAL = 0;
        int GROUP = 1;
        int EXPENSE = 2;
        int USERS = 3;
    }
}
