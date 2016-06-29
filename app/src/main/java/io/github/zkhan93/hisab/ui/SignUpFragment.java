package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SignUpFragment.class.getSimpleName();

    @BindView(R.id.name)
    AppCompatEditText edtTxtName;
    @BindView(R.id.email)
    AppCompatEditText edtTxtEmail;
    @BindView(R.id.password)
    AppCompatEditText edtTxtPswd;
    @BindView(R.id.confirm_password)
    AppCompatEditText edtTxtConfirmPswd;
    @BindView(R.id.btn_sign_up)
    Button btnRegister;
    @BindView(R.id.form)
    View formView;
    @BindView(R.id.progress_layout)
    View progressView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    public SignUpFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Log.d(TAG, "user signed_in" + firebaseUser.getUid());
                    startActivity(new Intent(getActivity(), GroupsActivity.class));
                } else {
                    Log.d(TAG, "user signed_out");
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_up, container, false);
        ButterKnife.bind(this, rootView);

        if (btnRegister != null)
            btnRegister.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign_up:
                registerClick();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (firebaseAuth != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void loginClick() {
        startActivity(new Intent(getActivity(), GroupsActivity.class));
    }

    public void registerClick() {
        String name, email, pswd, cnfPswd;
        if (!checkForValidValues())
            return;
//        Toast.makeText(getActivity(), "Register", Toast.LENGTH_SHORT).show();
        email = edtTxtEmail.getText().toString();
        pswd = edtTxtPswd.getText().toString();
        showProgress();
        firebaseAuth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener
                (getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "failed to register you", Toast
                                    .LENGTH_SHORT).show();
                            Log.d(TAG, "error: " + task.getException().getLocalizedMessage());
                        } else {
                            Log.d(TAG, "signup successful" + task.getResult().getUser().getUid());
                        }
                        hideProgress();
                    }
                });
    }

    /**
     * currently this function only validates that all the EditTexts contain some values
     *
     * @return
     */
    public boolean checkForValidValues() {
        boolean result = true;
        //checking for empty values
        AppCompatEditText[] edTxts = new AppCompatEditText[]{edtTxtName, edtTxtEmail, edtTxtPswd, edtTxtConfirmPswd};
        for (EditText et : edTxts) {
            if (et.getText().toString().isEmpty()) {
                String msg = "";
                switch (et.getId()) {
                    case R.id.name:
                        msg = String.format(getString(R.string.err_required), "Name");
                        break;
                    case R.id.email:
                        msg = String.format(getString(R.string.err_required), "Email");
                        break;
                    case R.id.password:
                        msg = String.format(getString(R.string.err_required), "Password");
                        break;
                    case R.id.confirm_password:
                        msg = "You need to confirm your password";
                }
                et.setError(msg);
                et.requestFocus();
                result = false;
            }
        }
        //check for both password match
        if (!edtTxtConfirmPswd.getText().toString().equals(edtTxtPswd.getText().toString())) {
            edtTxtConfirmPswd.setError("Password dows not match");
            edtTxtConfirmPswd.requestFocus();
            result = false;
        }
        //TODO: implement other validations is required
        return result;
    }

    private void showProgress() {
        progressView.setVisibility(View.VISIBLE);
        formView.setVisibility(View.GONE);
    }

    private void hideProgress() {
        progressView.setVisibility(View.GONE);
        formView.setVisibility(View.VISIBLE);
    }
}
