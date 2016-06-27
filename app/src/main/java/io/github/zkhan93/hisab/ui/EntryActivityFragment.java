package io.github.zkhan93.hisab.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class EntryActivityFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = EntryActivityFragment.class.getSimpleName();

    @BindView(R.id.btn_login)
    Button btnLogin;
    @BindView(R.id.btn_register)
    Button btnRegister;

    public EntryActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entry, container, false);
        ButterKnife.bind(this, rootView);

        if (btnLogin != null)
            btnLogin.setOnClickListener(this);
        if (btnRegister != null)
            btnRegister.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                loginClick();
                break;
            case R.id.btn_register:
                registerClick();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }

    }

    public void loginClick() {
        startActivity(new Intent(getActivity(), GroupsActivity.class));
    }

    public void registerClick() {
        Toast.makeText(getActivity(), "Register", Toast.LENGTH_SHORT).show();
    }

    public void confusionClick() {
        Toast.makeText(getActivity(), "Confuse", Toast.LENGTH_SHORT).show();
    }
}
