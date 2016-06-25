package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = GroupsActivity.class.getSimpleName();

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                addGroup();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    public void addGroup() {
        //TODO: show dialog to create a new group
        Toast.makeText(this, "Adding group", Toast.LENGTH_SHORT).show();
    }
}
