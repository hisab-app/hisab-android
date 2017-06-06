package io.github.zkhan93.hisab.ui.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.model.callback.ExpenseItemClbk;
import io.github.zkhan93.hisab.ui.ExpensesFragment;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.util.ImagePicker;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class EditExpenseItemDialog extends DialogFragment implements DialogInterface
        .OnClickListener, TextWatcher, View.OnFocusChangeListener, View.OnClickListener {

    public static final String TAG = EditExpenseItemDialog.class.getSimpleName();

    @BindView(R.id.description)
    TextInputEditText description;

    @BindView(R.id.amount)
    TextInputEditText amount;

    @BindView(R.id.image)
    ImageView image;

    private ExpenseItemClbk expenseItemUpdateClbk;
    private ExpenseItem expense;
    private boolean imageAdded;
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
        description.setOnFocusChangeListener(this);
        amount.setOnFocusChangeListener(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_done, this).setNegativeButton(R.string
                .label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        image.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (expenseItemUpdateClbk != null) {
            expense.setDescription(description.getText().toString());
            expense.setAmount(Float.parseFloat(amount.getText().toString()));
            expense.setCreatedOn(Calendar.getInstance().getTimeInMillis());
            expenseItemUpdateClbk.update(expense);

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }


    @Override
    public void onResume() {
        super.onResume();
        amount.addTextChangedListener(this);
        description.addTextChangedListener(this);
        enableIfValidInput();
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

                if (charSequence.toString().isEmpty()) {
                    description.setError(getString(R.string.err_required, "Description"));
                } else if (charSequence.toString().length() > 100) {
                    editable.delete(100, len);
                    description.setError("Keep the description short and crisp.");
                } else
                    description.setError(null);

                break;
            case R.id.amount:
                if (charSequence.toString().isEmpty()) {
                    amount.setError(getString(R.string.err_required, "Amount"));
                }
                try {
                    float amt = Float.parseFloat(charSequence.toString());
                    if (amt <= 0) {
                        amount.setError(getString(R.string.err_zero_amount));
                    } else
                        amount.setError(null);
                } catch (NumberFormatException ex) {
                    amount.setError(getString(R.string.err_invalid_amount));
                }
                break;
        }
        enableIfValidInput();
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        if (b)
            currentEditTextId = view.getId();
    }

    private void showSelectImage() {
        startActivityForResult(ImagePicker.getPickImageIntent(getActivity(), imageAdded), 0);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image:
                // Show only images, no videos or anything else
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                        .CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    showSelectImage();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        //TODO: explain  why you need permission
                        Toast.makeText(getActivity(), "allow camera permission", Toast
                                .LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest
                                .permission.CAMERA}, 0);
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSelectImage();
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Enable created button without setting error messages
     */
    private void enableIfValidInput() {
        if (description.getText().toString().trim().isEmpty() || amount.getText().toString().trim
                ().isEmpty())
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode != RESULT_OK) return;
            if (data.getBooleanExtra("remove_image", false)) {
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                image.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable
                        .ic_add_a_photo_grey_500_24dp));
                imageAdded = false;
                return;
            }
            Bitmap bitmap = ImagePicker.getImageFromResult(getActivity(), resultCode, data);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setImageBitmap(bitmap);
            imageAdded = true;
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
