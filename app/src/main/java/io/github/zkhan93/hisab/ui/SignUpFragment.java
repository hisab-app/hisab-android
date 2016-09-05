package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.math.BigInteger;
import java.security.SecureRandom;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignUpFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SignUpFragment.class.getSimpleName();

    @BindView(R.id.name)
    AppCompatEditText edtTxtName;
    @BindView(R.id.email)
    AppCompatEditText edtTxtEmail;
    @BindView(R.id.btn_sign_up)
    Button btnRegister;
    @BindView(R.id.form)
    View formView;
    @BindView(R.id.progress_layout)
    View progressView;
    @BindView(R.id.error)
    TextView error;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;

    public SignUpFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
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

    public void registerClick() {
        String pswd;

        if (!checkForValidValues())
            return;
        final String email = edtTxtEmail.getText().toString();
        final String name = edtTxtName.getText().toString();
        pswd = new BigInteger(40, new SecureRandom()).toString(32);
        showProgress();
        firebaseAuth.createUserWithEmailAndPassword(email, pswd).addOnCompleteListener
                (getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            setError(task.getException().getLocalizedMessage());
                            Log.d(TAG, "error: " + task.getException().getLocalizedMessage());
                            hideProgress();
                        } else {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String email = firebaseUser.getEmail();
                            String userId = firebaseUser.getUid();//Util.encodedEmail(email);
                            User user = new User(name, email, userId);
                            firebaseDatabase.getReference("users/" + userId).setValue(user);
                            firebaseAuth.signOut();
                            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener
                                    (getActivity(), new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "sign up successful");
                                            hideProgress();
                                            final Snackbar snackbar = Snackbar.make(getView(),
                                                    getString(R.string
                                                            .msg_signup_done_email_check), Snackbar
                                                            .LENGTH_INDEFINITE);
                                            snackbar.setAction(getString(R.string.ok), new View
                                                    .OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    snackbar.dismiss();
                                                }
                                            });
                                            snackbar.show();
                                            ((EntryActivity) getActivity()).loadLoginFragment();
                                        }
                                    });
                        }
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
        AppCompatEditText[] edTxts = new AppCompatEditText[]{edtTxtName, edtTxtEmail};
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
                }
                et.setError(msg);
                et.requestFocus();
                result = false;
            }
        }
        //TODO: implement other required validations
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

    private void setError(String msg) {
        if (msg == null || msg.trim().length() == 0) {
            clearError();
            return;
        }
        error.setText(msg);
        error.setVisibility(View.VISIBLE);
    }

    private void clearError() {
        error.setVisibility(View.GONE);
        error.setText("");
    }
}
