package com.lzf.myowriststrap;

import android.app.Application;

import java.text.SimpleDateFormat;

/**
 * Created by MJCoder on 2019-04-01.
 */

public class LzfApplication extends Application {
    public static final SimpleDateFormat yMdHmsS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
    public static final int REQUEST_PERMISSION_CODE = 6003;
}
