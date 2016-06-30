package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.github.zkhan93.hisab.R;

public class EntryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignUpFragment.TAG);
        if (fragment == null)
            fragment = new SignUpFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                SignUpFragment.TAG).commit();
    }

    public void loadLoginFragment(View view) {
        loadLoginFragment();
    }

    public void loadLoginFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SignInFragment.TAG);
        if (fragment == null)
            fragment = new SignInFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                SignInFragment.TAG).commit();
    }
}
