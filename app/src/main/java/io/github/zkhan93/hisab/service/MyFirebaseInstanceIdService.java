package io.github.zkhan93.hisab.service;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;

import io.github.zkhan93.hisab.util.Util;

/**
 * Created by zeeshan on 2/12/2017.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService implements PreferenceChangeListener {
    public static final String TAG = MyFirebaseInstanceIdService.class.getSimpleName();

    private DatabaseReference usersRef;
    private String refreshedToken;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        usersRef = firebaseDatabase.getReference().child("users");
    }

    @Override
    public void onTokenRefresh() {
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        updateTokenOnServer();
    }

    private void updateTokenOnServer() {
        String userId;
        if (Util.isLoggedIn(getApplicationContext())) {
            userId = Util.getUserId(getApplicationContext());
            if (userId == null) {
                Log.d(TAG, "saved userId is null");
                return;
            }
            usersRef.child(userId).child("token").setValue(refreshedToken);
        }
    }

    @Override
    public void preferenceChange(PreferenceChangeEvent preferenceChangeEvent) {
        if (preferenceChangeEvent.getKey().equals("user_id")) {
            if (preferenceChangeEvent.getNewValue() == null) {
                //userId removed
                return;
            }
            //userId updated
            updateTokenOnServer();
        }
    }
}
