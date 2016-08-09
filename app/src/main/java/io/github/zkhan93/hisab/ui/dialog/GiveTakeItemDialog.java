package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.ui.DetailGroupActivity;
import io.github.zkhan93.hisab.ui.adapter.MembersAdapter;

/**
 * Created by Zeeshan Khan on 8/9/2016.
 */
public class GiveTakeItemDialog extends DialogFragment implements TextWatcher,
        UserItemActionClickClbk {
    public static final String TAG = GiveTakeItemDialog.class.getSimpleName();

    @BindView(R.id.description)
    TextInputEditText description;
    @BindView(R.id.amount)
    TextInputEditText amount;
    @BindView(R.id.optionGiveTake)
    RadioGroup optionGiveTake;
    @BindView(R.id.members)
    RecyclerView members;

    private User me;
    private String groupId;
    private MembersAdapter membersAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_create_expense);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout
                        .dialog_create_give_take_item,
                null);
        ButterKnife.bind(this, view);

        description.addTextChangedListener(this);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            groupId = bundle.getString("groupId");
            me = bundle.getParcelable("me");
        } else {
            groupId = savedInstanceState.getString("groupId");
            me = savedInstanceState.getParcelable("me");
        }
        Log.d(TAG, "groupId=" + groupId + " me:" + me);
        members.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        membersAdapter = new MembersAdapter(this, me, groupId);
        members.setAdapter(membersAdapter);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_create, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (validateValues(description.getText()
                        .toString(), amount.getText().toString()) && optionGiveTake
                        .getCheckedRadioButtonId() != -1) {

                    ((DetailGroupActivity) getActivity()).createExpense(description.getText()
                            .toString(), Float.parseFloat(amount.getText().toString()));
                } else {
                    Log.d(TAG, "validation failed");
                }
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
        outState.putParcelable("me", me);
        outState.putString("groupId", groupId);
    }

    @Override
    public void onStart() {
        membersAdapter.registerEventListener();
        super.onStart();
    }

    @Override
    public void onPause() {
        membersAdapter.unregisterEventListener();
        super.onPause();
    }

    public boolean validateValues(String desc, String amt) {
        boolean result = true;
        try {
            if (desc == null || desc.isEmpty()) {
                description.setError("Description cannot be empty");
                description.requestFocus();
                result = false;
            }
            Float famt = Float.parseFloat(amt);
            if (famt <= 0) {
                amount.setError("Amount must be a non zero positive value");
                amount.requestFocus();
                result = false;
            }
        } catch (NumberFormatException ex) {
            amount.setError("Invalid amount value");
            amount.requestFocus();
            result = false;
        }
        return result;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        int len = editable.toString().length();
        if (len > 100) {
            editable.delete(100, len);
            description.setError("Keep the description short and crisp.");
        }
    }

    @Override
    public void UserClicked(ExUser user) {

    }
}
