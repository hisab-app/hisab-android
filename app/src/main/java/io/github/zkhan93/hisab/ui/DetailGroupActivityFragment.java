package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.User;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;
import io.github.zkhan93.hisab.util.Util;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailGroupActivityFragment extends Fragment {
    public static final String TAG = DetailGroupActivityFragment.class.getSimpleName();
    //member views
    @BindView(R.id.expenses)
    RecyclerView expensesList;
    String groupId;
    //other members
    ExpensesAdapter expensesAdapter;
    private User me;

    public DetailGroupActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle bundle = getArguments();
            if (bundle != null) {
                groupId = bundle.getString("groupId");
            }
        } else {
            groupId = savedInstanceState.getString("groupId");
        }
        me = Util.getUser(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_group, container, false);
        ButterKnife.bind(this, rootView);
        expensesList.setLayoutManager(new LinearLayoutManager(getActivity()));

        expensesAdapter = new ExpensesAdapter(me, groupId);
        expensesList.setAdapter(expensesAdapter);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("groupId", groupId);
    }

    @Override
    public void onStart() {
        super.onStart();
        expensesAdapter.registerChildEventListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        expensesAdapter.unregisterChildEventListener();
        expensesAdapter.clear();
    }
}
