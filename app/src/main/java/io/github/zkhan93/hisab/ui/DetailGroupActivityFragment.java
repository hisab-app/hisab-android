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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.ExpenseItem;
import io.github.zkhan93.hisab.ui.adapter.ExpensesAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailGroupActivityFragment extends Fragment implements ChildEventListener {
    public static final String TAG = DetailGroupActivityFragment.class.getSimpleName();
    //member views
    @BindView(R.id.expenses)
    RecyclerView expensesList;
    String groupId;
    //other members
    ExpensesAdapter expensesAdapter;
    ArrayList<ExpenseItem> expenses;
    DatabaseReference dbRef;

    public DetailGroupActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        groupId = bundle.getString("groupId");
        dbRef = FirebaseDatabase.getInstance().getReference("expenses/" + groupId);
        dbRef.addChildEventListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_group, container, false);
        ButterKnife.bind(this, rootView);
        expensesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState == null) {
            expenses = new ArrayList<>();
        } else {
            expenses = savedInstanceState.getParcelableArrayList("expense");
        }
        expensesAdapter = new ExpensesAdapter(null);
        expensesList.setAdapter(expensesAdapter);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("expenses", expenses);
    }


    @Override
    public void onStop() {
        super.onStop();
        dbRef.removeEventListener(this);
    }

    //firebase listners
    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        expensesAdapter.addExpense(expense);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        expensesAdapter.modifyExpense(expense);
    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {
        ExpenseItem expense = dataSnapshot.getValue(ExpenseItem.class);
        expense.setId(dataSnapshot.getKey());
        expensesAdapter.removeExpense(expense);
    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        Log.d(TAG, "onChildMoved");
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Log.d(TAG, "onCancelled");
    }
}
