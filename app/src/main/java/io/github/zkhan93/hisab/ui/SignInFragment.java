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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignInFragment extends Fragment implements View.OnClickListener, GoogleApiClient
        .OnConnectionFailedListener, OnCompleteListener<AuthResult> {

    public static final String TAG = SignInFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 12;

    @BindView(R.id.email)
    AppCompatEditText edtTxtEmail;
    @BindView(R.id.password)
    AppCompatEditText edtTxtPswd;
    @BindView(R.id.btn_sign_in)
    Button btnLogin;
    @BindView(R.id.form)
    View formView;
    @BindView(R.id.progress_layout)
    View progressView;
    @BindView(R.id.btn_google_sign_in)
    SignInButton signInButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;


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
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
                .DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .enableAutoManage(getActivity() /* FragmentActivity */, this /*
                OnConnectionFailedListener
                 */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);
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
            case R.id.btn_google_sign_in:
                googleSignInAction();
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
                (getActivity(), this);
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed android");
    }

    private void googleSignInAction() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "google sign in result" + result.isSuccess());
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            FirebaseAuthWithGoogle(account);
        } else {

        }
    }

    private void FirebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this);
    }

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
}
