package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * Created by Zeeshan Khan on 8/9/2016.
 */
public class ProgressVH extends RecyclerView.ViewHolder {
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.msg)
    TextView message;

    public ProgressVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        message.setVisibility(View.GONE);
    }

    public void setMessage(String message) {
        if (message == null)
            return;
        this.message.setText(message);
        this.message.setVisibility(View.VISIBLE);
    }
}
