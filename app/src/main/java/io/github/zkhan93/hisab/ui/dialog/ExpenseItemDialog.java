package io.github.zkhan93.hisab.ui.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.ui.MainActivity;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class ExpenseItemDialog extends DialogFragment implements TextWatcher, View.OnClickListener {

    public static final String TAG = ExpenseItemDialog.class.getSimpleName();

    @BindView(R.id.description)
    TextInputEditText description;

    @BindView(R.id.amount)
    TextInputEditText amount;

    @BindView(R.id.addImage)
    ImageButton btnAddImage;

    @BindView(R.id.clickImage)
    ImageButton btnClickImage;

    @BindView(R.id.image)
    ImageView image;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_create_expense);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_expense,
                null);
        ButterKnife.bind(this, view);
        description.addTextChangedListener(this);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_create, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (validateValues()) {
                    ((MainActivity) getActivity()).createExpense(description.getText()
                                    .toString(), Float.parseFloat(amount.getText().toString()),
                            ExpenseItem.ITEM_TYPE.SHARED, null, 0);
                } else {

                }
            }
        }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        btnAddImage.setOnClickListener(this);
        btnClickImage.setOnClickListener(this);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        enableIfValidInput();
    }

    private void enableIfValidInput() {
        if (description.getText().toString().trim().isEmpty() || amount.getText().toString().trim
                ().isEmpty())
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        else
            ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
    }

    public boolean validateValues() {
        boolean result = true;
        try {
            if (description.getText().toString().trim().isEmpty()) {
                description.setError(getString(R.string.err_empty_desc));
                description.requestFocus();
                result = false;
            }
            Float famt = Float.parseFloat(amount.getText().toString().trim());
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
            description.setError(getString(R.string.err_long_desc));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.addImage:
                // Show only images, no videos or anything else
                intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 0);
                break;
            case R.id.clickImage:
                // Show only images, no videos or anything else
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission
                        .CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Always show the chooser (if there are multiple options available)
                    startActivityForResult(intent, 1);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                            Manifest.permission.CAMERA)) {
                        Toast.makeText(getActivity(), "allow camera permission", Toast
                                .LENGTH_LONG).show();
                    } else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest
                                .permission.CAMERA}, 0);
                    }
                }
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode != RESULT_OK) return;
            Log.d(TAG, "data: " + data.getDataString());
            Picasso.with(getActivity().getApplicationContext()).load(data.getDataString())
                    .into(image);
            image.setVisibility(View.VISIBLE);
            return;
        } else if (requestCode == 1) {
            if (resultCode != RESULT_OK) return;
            Bitmap bitmapImage = (Bitmap) data.getExtras().get("data");
            image.setImageBitmap(bitmapImage);
            image.setVisibility(View.VISIBLE);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(intent, 1);
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
