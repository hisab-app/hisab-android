package io.github.zkhan93.hisab;

import android.app.Application;
import android.os.Build;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

import org.greenrobot.greendao.database.Database;

import io.github.zkhan93.hisab.model.notification.DaoMaster;
import io.github.zkhan93.hisab.model.notification.DaoSession;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class HisabApplication extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            FirebaseDatabase.getInstance().setLogLevel(Logger.Level.INFO);
        }
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);


        //green dao
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "hisab-db");
        Database db = helper.getWritableDb();
        if (BuildConfig.DEBUG) {
//            DaoMaster.dropAllTables(db,true);
//            DaoMaster.createAllTables(db,false);
        }
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
