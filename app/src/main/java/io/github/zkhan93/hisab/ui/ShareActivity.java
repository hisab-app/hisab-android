package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import butterknife.BindView;
import io.github.zkhan93.hisab.R;

public class ShareActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = ShareActivity.class.getSimpleName();

    @BindView(R.id.fab)
    private FloatingActionButton fab;
    @BindView(R.id.toolbar)
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ShareActivityFragment
                .TAG);
        if (fragment == null)
            fragment = new ShareActivityFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment,
                ShareActivityFragment.TAG).commit();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab:
                addShareUser();
                break;
            default:
                Log.d(TAG, "click not inplement");
        }
    }

    private void addShareUser() {
        Snackbar.make(fab, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }
}
