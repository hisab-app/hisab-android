package io.github.zkhan93.hisab.model.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 10/15/2016.
 */

public class HeaderVH extends RecyclerView.ViewHolder {
    public static final String TAG = HeaderVH.class.getSimpleName();
    private Context context;
    @BindView(R.id.category_name)
    TextView title;

    public HeaderVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.context = itemView.getContext();
    }

    public void setType(int type) {
        switch (type) {
            case TYPE.FAVORITE:
                title.setText(context.getString(R.string.title_favorites));
                break;
            case TYPE.OTHER:
                title.setText(context.getString(R.string.title_others));
                break;
            default:
                Log.d(TAG, "unknown group header type encounteere");
        }
    }

    public interface TYPE {
        int FAVORITE = 0;
        int OTHER = 1;
    }
}
