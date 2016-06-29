package io.github.zkhan93.hisab.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.ui.dialog.CreateGroupDialog;

public class GroupsActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = GroupsActivity.class.getSimpleName();

    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private DatabaseReference dbRef;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        fab.setOnClickListener(this);
        dbRef = FirebaseDatabase.getInstance().getReference();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
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
        //TODO: create group using firebase reference
        Group group = new Group();
        group.setName(groupName);
        group.setCreatedOn(java.util.Calendar.getInstance().getTimeInMillis());
        ArrayList<String> memberIds = new ArrayList<>();
        memberIds.add(firebaseUser.getUid());
        group.setModeratorId(memberIds.get(0));
        group.setMembersIds(memberIds);
        dbRef.child("groups").push().setValue(group);
        Toast.makeText(this, "creating group " + groupName, Toast.LENGTH_SHORT).show();
    }
}
