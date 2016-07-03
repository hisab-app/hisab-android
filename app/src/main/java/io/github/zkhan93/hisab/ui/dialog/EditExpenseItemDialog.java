package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.callback.ExpenseItemActionClbk;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class EditExpenseItemDialog extends DialogFragment implements DialogInterface
        .OnClickListener {

    public static final String TAG = EditExpenseItemDialog.class.getSimpleName();

    @BindView(R.id.description)
    TextInputEditText description;
    @BindView(R.id.amount)
    TextInputEditText amount;

    private ExpenseItemActionClbk actionClbk;
    private ExpenseItem expense;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_edit_expense);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_expense,
                null);
        ButterKnife.bind(this, view);
        if (savedInstanceState != null) {
            expense = savedInstanceState.getParcelable("expense");
        }
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

    public void setExpense(ExpenseItem expense) {
        this.expense = expense;
    }

    public void setActionClbk(ExpenseItemActionClbk actionClbk) {
        this.actionClbk = actionClbk;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (actionClbk != null) {
            String desc, amt;
            desc = description.getText().toString();
            amt = amount.getText().toString();
            if (!desc.isEmpty() && !amt.isEmpty()) {
                expense.setDescription(description.getText().toString());
                expense.setAmount(Float.parseFloat(amt));
                expense.setCreatedOn(Calendar.getInstance().getTimeInMillis());
                actionClbk.update(expense);
            } else {
                Toast.makeText(getActivity().getApplicationContext(), "cannot update Expense",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("expense", expense);
    }
}
