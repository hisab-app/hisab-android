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
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.ui.adapter.MembersAdapter;

/**
 * Created by Zeeshan Khan on 8/9/2016.
 */
public class PaidReceivedItemDialog extends DialogFragment implements UserItemActionClickClbk,
        TextWatcher {
    public static final String TAG = PaidReceivedItemDialog.class.getSimpleName();


    @BindView(R.id.amount)
    TextInputEditText amount;
    @BindView(R.id.optionGiveTake)
    RadioGroup optionGiveTake;
    @BindView(R.id.members)
    RecyclerView members;

    private User me;
    private String groupId;
    private MembersAdapter membersAdapter;
    private User checkedUser;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_create_expense);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout
                        .dialog_create_paid_received_item,
                null);
        ButterKnife.bind(this, view);

        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            groupId = bundle.getString("groupId");
            me = bundle.getParcelable("me");
            checkedUser=bundle.getParcelable("checkedUser");

        } else {
            groupId = savedInstanceState.getString("groupId");
            me = savedInstanceState.getParcelable("me");
        }
        Log.d(TAG, "groupId=" + groupId + " me:" + me);
        members.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        membersAdapter = new MembersAdapter(this, me, groupId);
        members.setAdapter(membersAdapter);
        if(savedInstanceState!=null)
            membersAdapter.UserClicked(new ExUser(checkedUser));
        builder.setView(view);
        builder.setPositiveButton(R.string.label_create, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (validateValues(amount.getText().toString()) && optionGiveTake
                        .getCheckedRadioButtonId() != -1) {
                    int shareType = optionGiveTake.getCheckedRadioButtonId() == R.id.paid ?
                            ExpenseItem.SHARE_TYPE.PAID : ExpenseItem.SHARE_TYPE.RECEIVED;
                    String desc = shareType == ExpenseItem.SHARE_TYPE.PAID ? getString(R.string
                            .paid) : getString(R.string.received);
                    ((MainActivity) getActivity()).createExpense(desc
                                    .toString(), Float.parseFloat(amount.getText().toString()),
                            ExpenseItem.ITEM_TYPE.PAID_RECEIVED, checkedUser, shareType);
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
        outState.putParcelable("checkedUser", checkedUser);
    }

    @Override
    public void onStart() {
        membersAdapter.registerEventListener();
        super.onStart();
    }

    @Override
    public void onResume() {
        amount.addTextChangedListener(this);
        super.onResume();
        enableIfValidInput();
    }

    @Override
    public void onPause() {
        membersAdapter.unregisterEventListener();
        amount.removeTextChangedListener(this);
        super.onPause();
    }

    public boolean validateValues(String amt) {
        boolean result = true;
        try {
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
    public void UserClicked(ExUser user) {
        this.checkedUser = new User(user);
        enableIfValidInput();
    }

    private void enableIfValidInput() {
        if (checkedUser == null || amount.getText().toString().isEmpty())
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        try {
            String str = amount.getText().toString();
            if (str.isEmpty()) {
                amount.setError(getString(R.string.err_required,"Amount"));
                return;
            }
            float amt = Float.parseFloat(str);
            if (amt == 0) {
                amount.setError(getString(R.string.err_zero_amount));
                return;
            }
            amount.setError(null);
            enableIfValidInput();
        } catch (NumberFormatException ex) {
            amount.setError(getString(R.string.err_invalid_amount));
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}
