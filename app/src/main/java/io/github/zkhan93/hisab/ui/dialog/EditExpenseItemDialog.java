package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;
import io.github.zkhan93.hisab.ui.ExpensesFragment;
import io.github.zkhan93.hisab.ui.MainActivity;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class EditExpenseItemDialog extends DialogFragment implements DialogInterface
        .OnClickListener, TextWatcher, View.OnFocusChangeListener {

    public static final String TAG = EditExpenseItemDialog.class.getSimpleName();

    @BindView(R.id.description)
    TextInputEditText description;
    @BindView(R.id.amount)
    TextInputEditText amount;

    private ExpenseItemClbk expenseItemUpdateClbk;
    private ExpenseItem expense;
    private int currentEditTextId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_edit_expense);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_expense,
                null);
        ButterKnife.bind(this, view);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            expense = bundle.getParcelable("expense");
        } else {
            expense = savedInstanceState.getParcelable("expense");
        }
        expenseItemUpdateClbk = (ExpenseItemClbk) (((MainActivity) getActivity())
                .getSupportFragmentManager().findFragmentByTag(ExpensesFragment.TAG));
        description.setText(expense.getDescription());
        amount.setText(String.valueOf(expense.getAmount()));
        builder.setView(view);
        builder.setPositiveButton(R.string.label_done, this).setNegativeButton(R.string
                .label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (expenseItemUpdateClbk != null) {
            if (validateValues(description.getText().toString(), amount.getText().toString())) {
                expense.setDescription(description.getText().toString());
                expense.setAmount(Float.parseFloat(amount.getText().toString()));
                expense.setCreatedOn(Calendar.getInstance().getTimeInMillis());
                expenseItemUpdateClbk.update(expense);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), getString(R.string.msg_cannot_update),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "ExpenseItemUpdateClbk not present, you have to implement ExpenseItemClbk " +
                    "in the" +
                    " fragment with tag used here");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("expense", expense);
    }

    public boolean validateValues(String desc, String amt) {
        boolean result = true;
        try {
            if (desc == null || desc.isEmpty()) {
                description.setError(getString(R.string.err_empty_desc));
                description.requestFocus();
                result = false;
            }
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
    public void onResume() {
        super.onResume();
        amount.addTextChangedListener(this);
        description.addTextChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        amount.removeTextChangedListener(this);
        description.removeTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {
        CharSequence charSequence = editable.toString();
        int len = editable.toString().length();
        switch (currentEditTextId) {
            case R.id.description:
                if (charSequence.toString().isEmpty())
                    description.setError(getString(R.string.err_required, "Description"));
                else if (charSequence.toString().length() > 100) {
                    editable.delete(100, len);
                    description.setError("Keep the description short and crisp.");
                }
                description.setError(null);
                break;
            case R.id.amount:
                if (charSequence.toString().isEmpty()) {
                    amount.setError(getString(R.string.err_required, "Amount"));
                    return;
                }
                try {
                    float amt = Float.parseFloat(charSequence.toString());
                    if (amt == 0) {
                        amount.setError(getString(R.string.err_zero_amount));
                        return;
                    }
                    amount.setError(null);
//                    enableIfValidInput();
                } catch (NumberFormatException ex) {
                    amount.setError(getString(R.string.err_invalid_amount));
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b)
            currentEditTextId = view.getId();
    }
}
