package io.github.zkhan93.hisab.ui;

import android.Manifest;
import android.app.DialogFragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Parcel;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Calendar;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Group;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.model.callback.ContextActionBarClbk;
import io.github.zkhan93.hisab.model.callback.GroupItemClickClbk;
import io.github.zkhan93.hisab.ui.adapter.GroupsAdapter;
import io.github.zkhan93.hisab.ui.dialog.CreateGroupDialog;
import io.github.zkhan93.hisab.util.Util;

/**
 * fragment to show all the groups of the user
 */
public class GroupsFragment extends Fragment implements
        PreferenceChangeListener, ContextActionBarClbk, View.OnClickListener, Toolbar
        .OnMenuItemClickListener {
    public static final String TAG = GroupsFragment.class.getSimpleName();
    public static final int GRP_FRAGMENT_PERMISSIONS_REQUEST_READ_CONTACTS = 23;
    //member views
    @BindView(R.id.groups)
    RecyclerView groupList;

    //other members
    private GroupsAdapter groupsAdapter;
    private User me;
    private ActionMode actionMode;
    private Toolbar toolbar;

    private int sortType = Group.SORT_TYPE.ALPHABETICAL;

    public GroupsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        me = Util.getUser(getContext());
        Parcel parcel = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_groups, container, false);
        ButterKnife.bind(this, rootView);

        groupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        groupsAdapter = new GroupsAdapter((GroupItemClickClbk) getActivity(), me, this);

        groupList.setAdapter(groupsAdapter);
        if (!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean
                ("isTwoPaneMode", false))
            setHasOptionsMenu(true);
        else {
            toolbar = ButterKnife.findById(getActivity(), R.id.toolbar);
            if (toolbar != null) {
                toolbar.setTitle(getString(R.string.title_activity_groups));
                toolbar.inflateMenu(R.menu.menu_groups_frag);
                toolbar.setOnMenuItemClickListener(this);
            }
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        groupsAdapter.registerChildEventListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        Util.getNotificationMapFromDisk(getActivity().getApplicationContext(), groupsAdapter);
    }

    @Override
    public void onPause() {
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putLong
                ("lastVisitOn", Calendar.getInstance().getTimeInMillis()).apply();
        super.onPause();
    }

    @Override
    public void onStop() {
        groupsAdapter.unregisterChildEventListener();
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_groups_frag, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_sort:
                toggleSortType();
                groupsAdapter.sort(sortType);
                Toast.makeText(getContext(), getCurrentSortTypeString(), Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_add_group:
                showCreateGroupDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleSortType() {
        sortType += 1;
        sortType %= 4;
    }

    private String getCurrentSortTypeString() {
        switch (sortType) {
            case Group.SORT_TYPE.ALPHABETICAL:
                return "Sorted alphabetically";
            case Group.SORT_TYPE.REVERSE_ALPHABETICAL:
                return "Sorted reversed alphabetically";
            case Group.SORT_TYPE.CHRONOLOGICAL:
                return "Sorted chronologically";
            case Group.SORT_TYPE.REVERSE_CHRONOLOGICAL:
                return "Sorted reversed chronologically";
        }
        return "";
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
        String keyChanged = preferenceChangeEvent.getKey();
        if (keyChanged.equals("name") || keyChanged.equals("email") || keyChanged.equals
                ("user_id")) {
            me = Util.getUser(getActivity());
        }
    }


    @Override
    public void showCAB() {
        actionMode = getActivity().startActionMode(groupsAdapter);
    }

    @Override
    public void setCount(int count) {
        actionMode.setTitle(getString(R.string.title_cab,count));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                Log.d(TAG, "click not implemented");
        }
    }

    public void showCreateGroupDialog() {
        DialogFragment dialog = new CreateGroupDialog();
        dialog.show(getActivity().getFragmentManager(), CreateGroupDialog.TAG);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

    /**
     * Check contact permission, result will be handles by host activity's onRequestPermissionsResult() method
     */
    private void checkContactPermission() {
        //check for permission in marshmellow and above
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_CONTACTS},
                        GRP_FRAGMENT_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            showCreateGroupDialog();
        }
    }

}
