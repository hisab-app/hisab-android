package io.github.zkhan93.hisab.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Zeeshan Khan on 11/4/2016.
 */

public class Contact implements Parcelable {

    String name;
    String email;
    Uri imageUri;
    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }

    public Contact(Long id, String name, String email, Uri imageUri) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.imageUri = imageUri;
    }

    public Contact(Parcel parcel) {
        id = parcel.readLong();
        name = parcel.readString();
        email = parcel.readString();
        imageUri = Uri.parse(parcel.readString());
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel parcel) {
            return new Contact(parcel);
        }

        @Override
        public Contact[] newArray(int i) {
            return new Contact[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(imageUri.toString());
    }

    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", imageUri=" + imageUri +
                ", id=" + id +
                '}';
    }
}
