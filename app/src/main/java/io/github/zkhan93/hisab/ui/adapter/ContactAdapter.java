package io.github.zkhan93.hisab.ui.adapter;

import android.content.ContentUris;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Contact;
import io.github.zkhan93.hisab.model.callback.ContactItemClickClbk;
import io.github.zkhan93.hisab.model.viewholder.ContactVH;

/**
 * Created by Zeeshan Khan on 11/4/2016.
 */

public class ContactAdapter extends RecyclerView.Adapter<ContactVH> implements ContactItemClickClbk {

    private Cursor cursor;
    private final int nameColIndex, emailColindex, idColIndex;
    private ContactItemClickClbk contactItemClickClbk;
    List<Long> selectedContacts;

    {
        selectedContacts = new ArrayList<>();
    }

    public ContactAdapter(Cursor cursor, ContactItemClickClbk contactItemClickClbk) {
        this.contactItemClickClbk = contactItemClickClbk;
        this.cursor = cursor;
        nameColIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        emailColindex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
        idColIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
    }

    @Override
    public ContactVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false), this);
    }

    @Override
    public void onBindViewHolder(ContactVH holder, int position) {
        cursor.moveToPosition(position);
        Contact contact = new Contact(cursor.getLong
                (idColIndex), cursor.getString(nameColIndex), cursor.getString(emailColindex),
                ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, cursor.getLong(idColIndex)));
        holder.setContact(contact, selectedContacts.contains(contact.getId()));
    }

    public void setContacts(Cursor cursor) {
        if (this.cursor != null)
            this.cursor.close();
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (cursor == null || cursor.isClosed())
            return 0;
        return cursor.getCount();
    }

    @Override
    public void onContactClicked(Contact contact) {
        if (selectedContacts.contains(contact.getId()))
            selectedContacts.remove(contact.getId());
        else
            selectedContacts.add(contact.getId());
        contactItemClickClbk.onContactClicked(contact);
    }
}
