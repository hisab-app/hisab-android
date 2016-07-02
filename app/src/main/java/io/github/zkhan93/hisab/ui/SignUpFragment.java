package io.github.zkhan93.hisab.ui;

import android.content.Intent;
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
import android.widget.Toast;

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
import io.github.zkhan93.hisab.util.Util;

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

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth.AuthStateListener authStateListener;

    public SignUpFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Log.d(TAG, "user signed_in" + firebaseUser.getUid());
                    startActivity(new Intent(getActivity(), GroupsActivity.class));
                } else {
                    Log.d(TAG, "user signed_out");
                    Util.clearPreferences(getActivity());
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
                            Toast.makeText(getActivity(), "failed to register you", Toast
                                    .LENGTH_SHORT).show();
                            Log.d(TAG, "error: " + task.getException().getLocalizedMessage());
                        } else {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            String email = firebaseUser.getEmail();
                            String userId = Util.encodedEmail(email);
                            User user = new User(name, email, userId);
                            firebaseDatabase.getReference("users/" + userId).setValue(user);
                            firebaseAuth.signOut();
                            firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener
                                    (getActivity(), new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d(TAG, "sign up successful");
                                            hideProgress();
                                            Snackbar.make(getView(), "Sign up successful, check " +
                                                    "your " +
                                                    "email for password", Snackbar.LENGTH_LONG)
                                                    .show();
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
}
