package io.github.zkhan93.hisab.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import io.github.zkhan93.hisab.model.ui.ExUser;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class User implements Parcelable {
    public static Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel parcel) {
            return new User(parcel);
        }

        @Override
        public User[] newArray(int i) {
            return new User[i];
        }
    };
    String name;
    String email;
    String id;
    Long lastVisitOn;

    public User() {
    }

    public User(String name, String email, String id) {
        this.name = name;
        this.email = email;
        this.id = id;
        lastVisitOn = Calendar.getInstance().getTimeInMillis();
    }

    public User(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        email = parcel.readString();
        lastVisitOn = parcel.readLong();
    }

    public User(@NonNull ExUser user) {
        this(user.getName(), user.getName(), user.getId());
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getLastVisitOn() {
        return lastVisitOn;
    }

    public void setLastVisitOn(Long lastVisitOn) {
        this.lastVisitOn = lastVisitOn;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(email);
        parcel.writeLong(lastVisitOn);
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", id='" + id + '\'' +
                ", lastVisitOn=" + lastVisitOn +
                '}';
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("email", email);
        map.put("id", id);
        map.put("lastVisitOn", lastVisitOn);
        return map;
    }

}
