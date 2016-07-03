package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.UserItemActionClickClbk;
import io.github.zkhan93.hisab.model.ui.ExUser;
import io.github.zkhan93.hisab.ui.adapter.UsersAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ShareActivityFragment extends Fragment implements UserItemActionClickClbk {
    public static final String TAG = ShareActivityFragment.class.getSimpleName();

    @BindView(R.id.users)
    RecyclerView usersListView;

    private UsersAdapter usersAdapter;
    private String groupId;
    private DatabaseReference shareDbRef;
    private User me;

    public ShareActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                groupId = bundle.getString("groupId");
                me = bundle.getParcelable("me");
            }
        } else {
            groupId = savedInstanceState.getString("groupId");
            me = savedInstanceState.getParcelable("me");
        }
        shareDbRef = FirebaseDatabase.getInstance().getReference("shareWith/" + groupId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_share, container, false);
        ButterKnife.bind(this, rootView);
        usersListView.setLayoutManager(new LinearLayoutManager(getContext()));
        usersAdapter = new UsersAdapter(this);
        usersListView.setAdapter(usersAdapter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        usersAdapter.registerChildListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        usersAdapter.unregisterChildListener();
        usersAdapter.clear();
    }

    @Override
    public void UserClicked(final ExUser user) {
        //TODO: add this user to me's fried list
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        if (user.isChecked()) {
            Log.d(TAG, "adding " + user.getName() + " to share list ");

            dbRef.child("groups").child(me.getId()).child(groupId).addListenerForSingleValueEvent
                    (new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, dataSnapshot.toString() + "");
                            FirebaseDatabase.getInstance().getReference("groups/" + user.getId()
                                    + "/" + dataSnapshot.getKey()).setValue(dataSnapshot.getValue
                                    (Group.class));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.d(TAG, "onCancelled");
                        }
                    });
            dbRef.child("shareWith").child(groupId).child(user.getId()).setValue(new User(user
                    .getName(), user.getEmail(), user.getId()));
        } else {
            Log.d(TAG, "removing " + user.getName() + " from sharing list");
            shareDbRef.child(user.getId()).removeValue();
            dbRef.child("groups").child(user.getId()).child(groupId).removeValue();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("groupId", groupId);
        outState.putParcelable("me", me);
    }
}
