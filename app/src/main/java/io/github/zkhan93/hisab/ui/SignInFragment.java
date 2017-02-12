package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class SignInFragment extends Fragment implements View.OnClickListener, GoogleApiClient
        .OnConnectionFailedListener, OnCompleteListener<AuthResult> {

    public static final String TAG = SignInFragment.class.getSimpleName();
    private static final int RC_SIGN_IN = 1;

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
    @BindView(R.id.error)
    TextView error;
    @BindView(R.id.reset_password)
    TextView resetPassword;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth.AuthStateListener authStateListener;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    private ValueEventListener nameValueListener;

    {
        nameValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                Log.d(TAG, String.format("%s;", name));
                saveUserToPreference(name, null, null);
                showGroupActivityAndQuit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "error occurred while reading name from firebase: " + databaseError
                        .getMessage());
            }
        };
    }

    public SignInFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    Log.d(TAG, "user signed_in with " + firebaseAuth.getCurrentUser()
                            .getProviderId());
                    String userId, email, name;
                    userId = firebaseUser.getUid();//Util.encodedEmail(firebaseUser
                    email = firebaseUser.getEmail();// .getEmail());
                    name = firebaseUser.getDisplayName();
                    saveUserToPreference(name, email, userId);
                    if (firebaseUser.getDisplayName() != null && !firebaseUser.getDisplayName()
                            .isEmpty()) {
                        //google sign in
                        User user = new User
                                (firebaseUser.getDisplayName(), firebaseUser.getEmail(),
                                        userId, FirebaseInstanceId.getInstance().getToken());
                        firebaseDatabase.getReference("users").child(userId).setValue(user)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            showGroupActivityAndQuit();
                                        } else {
                                            Log.d(TAG, "error creating user: " + task
                                                    .getException()
                                                    .getLocalizedMessage());
                                            showError(task.getException().getLocalizedMessage
                                                    ());
                                            hideProgress();
                                        }
                                    }
                                });
                    } else {
                        //sign in with password

                        //try to get name from firebase

                        firebaseDatabase.getReference("users").child(firebaseUser.getUid()).child
                                ("name")
                                .addListenerForSingleValueEvent(nameValueListener);
                    }

                } else {
                    Log.d(TAG, "user signed_out");
                    Util.clearPreferences(getActivity());
                    hideProgress();
                }
            }
        };
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions
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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sign_in, container, false);
        ButterKnife.bind(this, rootView);

        if (btnLogin != null)
            btnLogin.setOnClickListener(this);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());
        signInButton.setOnClickListener(this);
        resetPassword.setOnClickListener(this);
        showProgress();
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_sign_in:
                loginBtnAction();
                break;
            case R.id.btn_google_sign_in:
                googleSignInAction();
                break;
            case R.id.reset_password:
                resetPassword();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null) {
            googleApiClient.stopAutoManage(getActivity());
            googleApiClient.disconnect();
        }
    }

    public void loginBtnAction() {
        clearError();
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
        AppCompatEditText[] edTxts = new AppCompatEditText[]{edtTxtEmail, edtTxtPswd};
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
        clearError();
        showProgress();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "google sign in result" + result.isSuccess());
        if (result.isSuccess()) {
            final GoogleSignInAccount account = result.getSignInAccount();
            if (account != null) {
                FirebaseAuthWithGoogle(account);
            } else {
                Log.d(TAG, "account is null");
            }
        } else {
            String error = result.getStatus().getStatusMessage();
            if (error == null)
                error = "Google signin failed! Try again";
            showError(error);
            hideProgress();
            Log.d(TAG, "google sign in result failed");
        }
    }

    private void FirebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + account.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this);
    }

    private void showError(String msg) {
        if (error != null) {
            if (msg == null || msg.trim().length() == 0) {
                clearError();
                return;
            }
            error.setText(msg);
            error.setVisibility(View.VISIBLE);
        }
    }

    private void clearError() {
        if (error != null) {
            error.setVisibility(View.GONE);
            error.setText("");
        }
        edtTxtEmail.setError(null);
        edtTxtPswd.setError(null);
    }

    private void resetPassword() {
        clearError();
        if (edtTxtEmail == null) {
            Log.d(TAG, "no email field");
            return;
        }
        final String email = edtTxtEmail.getText().toString().trim();
        if (email.isEmpty()) {
            edtTxtEmail.setError("Enter you email address to reset your password");
            edtTxtEmail.requestFocus();
            return;
        }

        showProgress();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(getActivity(), new
                OnCompleteListener<Void>() {

                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = null;
                        hideProgress();
                        if (task.isSuccessful()) {
                            msg = getString(R.string
                                    .msg_reset_email_check,email);
                        } else {
                            msg = task.getException().getLocalizedMessage();
                            if (msg == null) {
                                msg = getString(R.string.msg_gen_error);
                            }
                        }
                        final Snackbar snackbar = Snackbar.make(formView, msg, Snackbar
                                .LENGTH_INDEFINITE);
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }
                });
    }

    /**
     * TaskComplete listener for normal and google sign in calls
     *
     * @param task
     */
    @Override
    public void onComplete(@NonNull Task<AuthResult> task) {
        if (!task.isSuccessful()) {
            showError(task.getException().getLocalizedMessage());
            Log.d(TAG, "error: " + task.getException().getLocalizedMessage());
            hideProgress();
        }
    }

    private void saveUserToPreference(final String name, final String email, final String userId) {
        AsyncTask<Void, Void, Void> prefUpdater = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getActivity
                        ());
                Log.d(TAG, String.format("%s;%s;%s", name, email, userId));
                SharedPreferences.Editor editor = spf.edit();
                editor.putBoolean("logged_in", true);
                if (userId != null && !userId.isEmpty())
                    editor.putString("user_id", userId);
                if (name != null && !name.isEmpty())
                    editor.putString("name", name);
                if (email != null && !email.isEmpty())
                    editor.putString("email", email);
                editor.apply();
                return null;
            }
        };
        prefUpdater.execute();
    }

    private void showGroupActivityAndQuit() {
        if (isVisible()) {
            startActivity(new Intent(getActivity(),
                    MainActivity
                            .class));
            getActivity().finish();
        }
    }
}
