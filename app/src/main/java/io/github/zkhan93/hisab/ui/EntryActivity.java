package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.service.NotificationService;
import io.github.zkhan93.hisab.util.Util;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Util.isLoggedIn(getApplicationContext())) {
            showMainActivityAndQuit();
        }
        setContentView(R.layout.activity_entry);
        if (savedInstanceState == null) {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignInFragment.TAG);
            if (fragment == null)
                fragment = new SignInFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                    SignInFragment.TAG).commit();
        }
        //startService(new Intent(getApplicationContext(), NotificationService.class));
    }

    public void loadLoginFragment(View view) {
        loadLoginFragment();
    }

    public void loadSignUpFragment(View view) {
        loadSignUpFragment();
    }


    public void loadLoginFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignInFragment.TAG);
        if (fragment == null)
            fragment = new SignInFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                SignInFragment.TAG).commit();
    }

    private void loadSignUpFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignUpFragment.TAG);
        if (fragment == null)
            fragment = new SignUpFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                SignUpFragment.TAG).commit();
    }

    private void showMainActivityAndQuit() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
