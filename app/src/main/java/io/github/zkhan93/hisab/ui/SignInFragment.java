package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class SignInFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = SignInFragment.class.getSimpleName();


    @BindView(R.id.email)
    EditText edtTxtEmail;
    @BindView(R.id.password)
    EditText edtTxtPswd;
    @BindView(R.id.btn_sign_in)
    Button btnLogin;
    @BindView(R.id.form)
    View formView;
    @BindView(R.id.progress_layout)
    View progressView;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    public SignInFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        ButterKnife.bind(this, rootView);

        if (btnLogin != null)
            btnLogin.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign_up:
                loginBtnAction();
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

    public void loginBtnAction() {
        String email, pswd;
        if (!checkForValidValues())
            return;
//        Toast.makeText(getActivity(), "Register", Toast.LENGTH_SHORT).show();
        email = edtTxtEmail.getText().toString();
        pswd = edtTxtPswd.getText().toString();
        showProgress();
        firebaseAuth.signInWithEmailAndPassword(email, pswd).addOnCompleteListener
                (getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(getActivity(), "failed to register you", Toast
                                    .LENGTH_SHORT).show();
                            Log.d(TAG, "error: " + task.getException().getLocalizedMessage());
                        } else {
                            Log.d(TAG, "login successful" + task.getResult().getUser().getUid());
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
        EditText[] edTxts = new EditText[]{edtTxtEmail, edtTxtPswd};
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
