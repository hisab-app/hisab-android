package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.ui.adapter.MembersAdapter;

/**
 * Created by Zeeshan Khan on 9/29/2016.
 */

public class GroupDetailDialog extends DialogFragment {

    public static final String TAG = GroupDetailDialog.class.getSimpleName();

    @BindView(R.id.members)
    RecyclerView members;
    @BindView(R.id.time)
    TextView time;
    @BindView(R.id.moderator)
    TextView moderator;

    private Group group;
    private MembersAdapter membersAdapter;
    private User me;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            group = bundle.getParcelable("group");
            me = bundle.getParcelable("me");
        } else {
            group = savedInstanceState.getParcelable("group");
            me = savedInstanceState.getParcelable("me");
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(group.getName());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_group_detail,
                null);
        ButterKnife.bind(this, view);
        members.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        membersAdapter = new MembersAdapter(null, me, group.getId(), false);
        members.setAdapter(membersAdapter);
        time.setText(DateUtils.getRelativeTimeSpanString(getActivity().getApplication().getApplicationContext(), group
                .getCreatedOn()));
        moderator.setText(group.getModerator().getName());
        builder.setView(view);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("group", group);
        outState.putParcelable("me", me);
    }

    @Override
    public void onPause() {
        super.onPause();
        membersAdapter.unregisterEventListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        membersAdapter.registerEventListener();
    }
}
