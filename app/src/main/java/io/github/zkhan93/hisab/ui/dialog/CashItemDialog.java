package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RadioGroup;
import android.widget.TextView;

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
public class CashItemDialog extends DialogFragment implements UserItemActionClickClbk,
        TextWatcher, RadioGroup.OnCheckedChangeListener {
    public static final String TAG = CashItemDialog.class.getSimpleName();


    @BindView(R.id.amount)
    TextInputEditText amount;

    @BindView(R.id.optionGiveTake)
    RadioGroup optionGiveTake;

    @BindView(R.id.members)
    RecyclerView members;

    @BindView(R.id.description)
    TextInputEditText description;

    @BindView(R.id.usersHeader)
    TextView usersHeader;

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
        optionGiveTake.setOnCheckedChangeListener(this);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            groupId = bundle.getString("groupId");
            me = bundle.getParcelable("me");
            checkedUser = bundle.getParcelable("checkedUser");

        } else {
            groupId = savedInstanceState.getString("groupId");
            me = savedInstanceState.getParcelable("me");
            checkedUser = savedInstanceState.getParcelable("checkedUser");
        }
        Log.d(TAG, "groupId=" + groupId + " me:" + me);
        members.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        membersAdapter = new MembersAdapter(this, me, groupId, true);
        members.setAdapter(membersAdapter);
        if (savedInstanceState != null && checkedUser != null)
            membersAdapter.setCheckedUser(new ExUser(checkedUser));
        builder.setView(view);
        builder.setPositiveButton(R.string.label_create, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (validateValues(amount.getText().toString()) && optionGiveTake
                        .getCheckedRadioButtonId() != -1) {
                    int shareType = optionGiveTake.getCheckedRadioButtonId() == R.id.paid ?
                            ExpenseItem.SHARE_TYPE.PAID : ExpenseItem.SHARE_TYPE.RECEIVED;
//                    String desc = shareType == ExpenseItem.SHARE_TYPE.PAID ? getString(R.string
//                            .paid) : getString(R.string.received);
                    ((MainActivity) getActivity()).createExpense(description.getText().toString(), Float.parseFloat(amount
                                    .getText()
                                    .toString()),false,
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onResume() {
        membersAdapter.registerEventListener();
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
                amount.setError(getString(R.string.err_amount_non_zero_positive));
                amount.requestFocus();
                result = false;
            }
        } catch (NumberFormatException ex) {
            amount.setError(getString(R.string.err_invalid_amount));
            amount.requestFocus();
            result = false;
        }
        return result;
    }

    @Override
    public void userClicked(ExUser user) {
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
                amount.setError(getString(R.string.err_required, "Amount"));
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

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.paid:
                usersHeader.setText(getString(R.string.to));
                break;
            case R.id.received:
                usersHeader.setText(getString(R.string.from));
                break;

        }
    }
}
