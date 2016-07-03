package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.callback.GroupRenameClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class RenameGroupDialog extends DialogFragment {

    public static final String TAG = RenameGroupDialog.class.getSimpleName();

    @BindView(R.id.group_name)
    TextInputEditText groupName;
    GroupRenameClbk groupRenameClbk;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_rename_grp);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_rename_group,
                null);
        ButterKnife.bind(this, view);
        groupRenameClbk = (GroupRenameClbk) getActivity();
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                String name = bundle.getString("name");
                if (name != null)
                    groupName.setText(name);
            }
        } else {
            groupName.setText(savedInstanceState.getString("name"));
        }
        builder.setView(view);
        builder.setPositiveButton(R.string.label_done, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (groupRenameClbk != null)
                    groupRenameClbk.renameTo(groupName.getText().toString());
                else
                    Log.e(TAG, "no GroupRenameCallback added to RenameGroupDialog, you need to " +
                            "implement GroupRenameClbk in host Activity");

            }
        }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
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
        outState.putString("name", groupName.getText().toString());
    }
}
