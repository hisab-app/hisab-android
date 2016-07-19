package io.github.zkhan93.hisab.ui;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.ui.dialog.CreateGroupDialog;
import io.github.zkhan93.hisab.util.Util;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener,
        OnCompleteListener<Void> {
    public static final String TAG = GroupsActivity.class.getSimpleName();

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private DatabaseReference dbRef;


    private FirebaseUser firebaseUser;
    private FirebaseAuth firebaseAuth;
    private User me;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
        dbRef = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseAuth = FirebaseAuth.getInstance();
        me = Util.getUser(getApplicationContext());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                firebaseAuth.signOut();
                Util.clearPreferences(getApplicationContext());
                startActivity(new Intent(this, EntryActivity.class));
                finish();
                return true;
            case R.id.action_sort:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                showAddGroupDialog();
                break;
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    public void showAddGroupDialog() {
        DialogFragment dialog = new CreateGroupDialog();
        dialog.show(getFragmentManager(), "dialog");
    }

    public void createGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        group.setCreatedOn(java.util.Calendar.getInstance().getTimeInMillis());
        group.setModerator(me);
        dbRef.child("groups/" + me.getId()).push().setValue(group).addOnCompleteListener(this);
        Toast.makeText(this, "creating group " + groupName, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onComplete(@NonNull Task<Void> task) {
        if (task.isSuccessful()) {
            showSnackBar("Group created");
        } else {
            String error = "";
            if (task.getException() != null)
                error = task.getException().getLocalizedMessage();
            else
                error = "Unknown error";
            showSnackBar("Unable to create group " + error);
        }
    }

    private void showSnackBar(String msg) {
        Snackbar.make(toolbar, msg, Snackbar.LENGTH_SHORT).show();
    }
}
