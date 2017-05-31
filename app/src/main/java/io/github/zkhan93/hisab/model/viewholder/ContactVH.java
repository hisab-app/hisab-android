package io.github.zkhan93.hisab.model.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.github.zkhan93.hisab.R;
import io.github.zkhan93.hisab.model.Contact;
import io.github.zkhan93.hisab.model.callback.ContactItemClickClbk;

/**
 * Created by Zeeshan Khan on 11/4/2016.
 */

public class ContactVH extends RecyclerView.ViewHolder implements View.OnClickListener {

    public static final String TAG = ContactVH.class.getSimpleName();

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.image)
    CircleImageView image;

    private ContactItemClickClbk contactItemClickClbk;
    private Contact contact;
    private View itemView;
    private boolean selected;

    public ContactVH(View itemView, ContactItemClickClbk contactItemClickClbk) {
        super(itemView);
        this.contactItemClickClbk = contactItemClickClbk;
        ButterKnife.bind(this, itemView);
        itemView.setOnClickListener(this);
        this.itemView = itemView;
    }

    @Override
    public void onClick(View v) {
        contactItemClickClbk.onContactClicked(contact);
        selected = !selected;
        if (selected)
            Picasso.with(image.getContext()).load(R.drawable.round_done_button)
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
        else
            Picasso.with(image.getContext()).load(contact.getImageUri())
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
    }

    public void setContact(Contact contact, boolean selected) {
        this.selected = selected;
        this.contact = contact;
        name.setText(contact.getName());
        email.setText(contact.getEmail());
        if (selected)
            Picasso.with(image.getContext()).load(R.drawable.round_done_button)
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(image);
        else
            Picasso.with(image.getContext()).load(contact.getImageUri())
                    .placeholder(R.drawable.big_user).fit().centerCrop().into(image);

    }
}
