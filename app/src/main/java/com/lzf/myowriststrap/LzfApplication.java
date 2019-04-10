package com.lzf.myowriststrap;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lzf.myowriststrap.bean.DaoMaster;
import com.lzf.myowriststrap.bean.DaoSession;

import org.greenrobot.greendao.database.Database;

import java.text.SimpleDateFormat;

/**
 * Created by MJCoder on 2019-04-01.
 */

public class LzfApplication extends Application {
    public static final SimpleDateFormat yMdHmsS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    public static final int REQUEST_PERMISSION_CODE = 6003;
    private volatile static DaoSession daoSession;

    /**
     * 获取数据库会话对象
     * 双检锁/双重校验锁（DCL，即 double-checked locking）
     * 是否 Lazy 初始化：是
     * 是否多线程安全：是
     * 描述：这种方式采用双锁机制，安全且在多线程情况下能保持高性能。
     *
     * @param context
     * @return
     */
    public static DaoSession getDaoSession(Context context) {
        try {
            if (daoSession == null) {
                synchronized (DaoSession.class) {
                    if (daoSession == null) {
                        // do this once, for example in your Application class
                        //// note: DevOpenHelper is for dev only, use a OpenHelper subclass instead
                        DaoMaster.OpenHelper helper = new DaoMaster.OpenHelper(context, "MYO_WRIST_STRAP.db", null) {
                            @Override
                            public void onCreate(Database db) {
                                //                                Log.v("Database", "Creating tables for schema version " + DaoMaster.SCHEMA_VERSION);
                                super.onCreate(db);
                            }

                            @Override
                            public void onCreate(SQLiteDatabase db) {
                                //                                Log.v("SQLiteDatabase", "Creating tables for schema version " + DaoMaster.SCHEMA_VERSION);
                                super.onCreate(db);
                            }

                            @Override
                            public void onUpgrade(Database db, int oldVersion, int newVersion) {
                                //                                Log.v("Database", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
                                super.onUpgrade(db, oldVersion, newVersion);
                            }

                            @Override
                            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                                //                                Log.v("SQLiteDatabase", "Upgrading schema from version " + oldVersion + " to " + newVersion + " by dropping all tables");
                                super.onUpgrade(db, oldVersion, newVersion);
                            }
                        };
                        daoSession = new DaoMaster(helper.getWritableDatabase()).newSession();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return daoSession;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getDaoSession(this);
    }
}
