package io.github.zkhan93.hisab.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailGroupActivityFragment extends Fragment {
    public static final String TAG = DetailGroupActivityFragment.class.getSimpleName();
    //member views
    @BindView(R.id.expenses)
    RecyclerView expensesList;

    //other members
    ExpensesAdapter expensesAdapter;

    public DetailGroupActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_group, container, false);
        ButterKnife.bind(this, rootView);
        expensesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        expensesAdapter = new ExpensesAdapter(null);
        expensesList.setAdapter(expensesAdapter);
        return rootView;
    }
}
