package io.github.zkhan93.hisab.ui.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Contact;
import io.github.zkhan93.hisab.model.callback.ContactItemClickClbk;
import io.github.zkhan93.hisab.ui.MainActivity;
import io.github.zkhan93.hisab.ui.adapter.ContactAdapter;

/**
 * Created by Zeeshan Khan on 6/26/2016.
 */
public class CreateGroupDialog extends DialogFragment implements TextWatcher, LoaderManager.LoaderCallbacks<Cursor>,
        ContactItemClickClbk {

    public static final String TAG = CreateGroupDialog.class.getSimpleName();

    @BindView(R.id.group_name)
    TextInputEditText groupName;
    @BindView(R.id.contacts)
    RecyclerView contacts;
    @BindView(R.id.search)
    EditText search;
    @BindView(R.id.count)
    TextView count;

    private Map<Long, Contact> membersMap;
    private String filter;
    private static final String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.CommonDataKinds.Email.DATA,
            ContactsContract.CommonDataKinds.Photo.PHOTO_ID,
            ContactsContract.Contacts.DISPLAY_NAME,
    };

    {
        membersMap = new HashMap<>();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_create_grp);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_create_group,
                null);
        ButterKnife.bind(this, view);
        builder.setView(view);
        builder.setPositiveButton(R.string.label_create, new DialogInterface
                .OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((MainActivity) getActivity()).createGroup(groupName.getText().toString());
            }
        }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        search.addTextChangedListener(this);
        contacts.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d!=null){
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public void onContactClicked(Contact contact) {
        if (membersMap.containsKey(contact.getId()))
            membersMap.remove(contact.getId());
        else
            membersMap.put(contact.getId(), contact);
        int size = membersMap.size();
        count.setText(size == 0 ? " Just you" : " " + (size + 1) + " Members");
        Log.d(TAG, "selected" + membersMap.toString());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri contentUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        // Starts the query
        String searchStr = search.getText().toString();
        filter = new StringBuilder().append(ContactsContract.CommonDataKinds
                .Email.DATA).append(" like '%" + searchStr + "%' or ")
                .append(ContactsContract.Contacts.DISPLAY_NAME).append(" like '%" + searchStr + "%'").toString();


        return new CursorLoader(
                getActivity().getApplicationContext(),
                contentUri,
                PROJECTION, filter,
                null,
                ContactsContract.Contacts.DISPLAY_NAME);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        RecyclerView.Adapter adapter = contacts.getAdapter();
        if (adapter != null)
            ((ContactAdapter) adapter).setContacts(data);
        else
            contacts.setAdapter(new ContactAdapter(data, this));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        getLoaderManager().restartLoader(0, null, this);
    }

}
