package io.github.zkhan93.hisab;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class HisabApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
