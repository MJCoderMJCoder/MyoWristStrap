package com.lzf.myowriststrap.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lzf.myowriststrap.R;
import com.lzf.myowriststrap.Util.CopyFileToSD;
import com.lzf.myowriststrap.Util.SharedPreferencesUtil;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.Vector3;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.lzf.myowriststrap.LzfApplication.REQUEST_PERMISSION_CODE;
import static com.lzf.myowriststrap.LzfApplication.yMdHmsS;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * 所请求的一系列权限
     */
    private final String[] PERMISSIONS = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    /**
     * 声明一个集合，在后面的代码中用来存储用户拒绝授权的一系列权限
     */
    private List<String> permissionList = new ArrayList<String>();
    //测试MYO腕带文本按钮
    private TextView sampleText;
    //MYO的核心
    private Hub hub;
    /**
     * You need to register a DeviceListener with the Hub in order to receive Myo events.
     * If you don't want to implement the entire interface, you can extend AbstractDeviceListener and override only the methods you care about.
     */
    private DeviceListener deviceListener = new AbstractDeviceListener() {

        @Override
        public void onAttach(Myo myo, long timestamp) {
            super.onAttach(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "连接MYO时调用");
        }

        @Override
        public void onConnect(Myo myo, long timestamp) {
            super.onConnect(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "连接附加的MYO时调用");
        }

        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            super.onArmSync(myo, timestamp, arm, xDirection);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + arm + "-" + xDirection, "当附加的MYO识别出它在手臂上时调用");
            if (myo.getArm() == Arm.LEFT) {
                SharedPreferencesUtil.put(MainActivity.this, "左手-" + yMdHmsS.format(timestamp) + "-" + arm + "-" + xDirection, "当附加的MYO识别出它在手臂上时调用");
            } else if (myo.getArm() == Arm.RIGHT) {
                SharedPreferencesUtil.put(MainActivity.this, "右手-" + yMdHmsS.format(timestamp) + "-" + arm + "-" + xDirection, "当附加的MYO识别出它在手臂上时调用");
            } else {
                SharedPreferencesUtil.put(MainActivity.this, "未知-" + yMdHmsS.format(timestamp) + "-" + arm + "-" + xDirection, "当附加的MYO识别出它在手臂上时调用");
            }
        }

        @Override
        public void onUnlock(Myo myo, long timestamp) {
            super.onUnlock(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "当同步的MYO解锁时调用");
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            super.onPose(myo, timestamp, pose);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + pose, "当附加的MYO提供了新姿势时调用");
            //TODO: Do something awesome.
        }

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            super.onOrientationData(myo, timestamp, rotation);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + rotation, "当附加的MYO提供新的方向数据时调用");
            //TODO: Do something awesome.
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            super.onAccelerometerData(myo, timestamp, accel);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + accel, "当附加的MYO提供新的加速度计数据时调用");
            //TODO: Do something awesome.
        }

        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            super.onGyroscopeData(myo, timestamp, gyro);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + gyro, "当附加的MYO提供新的陀螺仪数据时调用");
            //TODO: Do something awesome.
        }

        /**
         *
         * @param myo
         * @param timestamp
         * @param rssi 由蓝牙硬件报告的远程设备的RSSI值。0如果没有RSSI值可用。(rssi)
         *             rssi >= -50 时 相间距离<50cm； -50>rssi<=-70 时 相间距离<200cm；   -70>rssi<=-80 时 相间距离<400cm； -80>rssi<=-100 时 相间距离<900cm；
         *
         */
        @Override
        public void onRssi(Myo myo, long timestamp, int rssi) {
            super.onRssi(myo, timestamp, rssi);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp) + "-" + rssi, "当requestRssi()触发读取附加的MYO的RSSI值时调用。");
        }

        @Override
        public void onLock(Myo myo, long timestamp) {
            super.onLock(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "当同步的Myo被锁定时调用");
        }

        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            super.onArmUnsync(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "将附加的Myo从手臂移开或移除时调用");
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            super.onDisconnect(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "连接的MYO断开连接时调用");
        }

        @Override
        public void onDetach(Myo myo, long timestamp) {
            super.onDetach(myo, timestamp);
            SharedPreferencesUtil.put(MainActivity.this, myo + "-" + yMdHmsS.format(timestamp), "当MYO分离时调用");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permissionIsGranted();
        hub = Hub.getInstance();
        if (!hub.init(this)) {
            Toast.makeText(this, "抱歉，您的设备暂时无法初始化MYO腕带插件", Toast.LENGTH_LONG);
            finish();
            return;
        } else {
            //LockingPolicy.STANDARD Using this policy means Myo will be locked until the user performs the unlock pose. This is the default policy.
            //LockingPolicy.NONE Using this policy means you will always receive pose events, regardless of Myo's unlock state.
            hub.setLockingPolicy(Hub.LockingPolicy.NONE);
            hub.addListener(deviceListener);
            // Example of a call to a native method
            sampleText = (TextView) findViewById(R.id.sample_text);
            sampleText.setText(stringFromJNI());
            sampleText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sampleText.setEnabled(false);
                    Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                    startActivity(intent);
                    // Use this instead to connect with a Myo that is very near (ie. almost touching) the device
                    //                hub.attachToAdjacentMyo();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sampleText.setEnabled(true);
    }

    /**
     * 判断哪些权限未授予以便在必要的时候重新申请
     * 判断存储未授予权限的集合permissionList是否为空：未授予的权限为空，表示都授予了
     */
    private void permissionIsGranted() {
        permissionList.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) { //该权限已经授予
                    //判断是否需要 向用户解释，为什么要申请该权限
                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                    permissionList.add(permission);
                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            permission)) {
                        Toast.makeText(this, "MyoWristStrap：获取权限失败，请在“设置”-“应用权限”-打开所需权限", Toast.LENGTH_LONG).show();
                    }
                }
            }
            if (!permissionList.isEmpty()) {
                String[] permissions = new String[permissionList.size()];
                //请求权限
                ActivityCompat.requestPermissions(this, permissionList.toArray(permissions), REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // 置入一个不设防的VmPolicy：Android 7.0 FileUriExposedException
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        //        int h = 0;
        //        for (int i = 0; i < scrollView.getChildCount(); i++) {
        //            h += scrollView.getChildAt(i).getHeight();
        //            scrollView.getChildAt(i).setBackgroundColor(Color.parseColor("#313744"));
        //        }
        //        Log.v("h", h + "");
        //        Bitmap bitmap = Bitmap.createBitmap(scrollView.getWidth(), h, Bitmap.Config.RGB_565);
        //        final Canvas canvasScroll = new Canvas(bitmap);
        //        scrollView.draw(canvasScroll);
        //                View decorView = getWindow().getDecorView(); // 获取屏幕
        //                decorView.setDrawingCacheEnabled(true);
        //                decorView.buildDrawingCache();
        //                Bitmap bitmap = decorView.getDrawingCache();
        //        if (bitmap != null) {
                   /*Vivo v3有问题
                   Bitmap.Config config = bitmap.getConfig();
                    int sourceBitmapHeight = bitmap.getHeight();
                    int sourceBitmapWidth = bitmap.getWidth();
                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE); // 画笔颜色
                    TextPaint textpaint = new TextPaint(paint);
                    textpaint.setTextSize(70); // 文字大小
                    textpaint.setAntiAlias(true); // 抗锯齿
                    StaticLayout title_layout = new StaticLayout("分享自Fitmind\n", textpaint, sourceBitmapWidth, Layout.Alignment.ALIGN_CENTER, 0f, 0f, true);
                    Bitmap share_bitmap = Bitmap.createBitmap(sourceBitmapWidth, sourceBitmapHeight + title_layout.getHeight(), config);// 创建一个新的位图bitmap
                    Canvas canvas = new Canvas(share_bitmap); //将share_bitmap图片作为画布
                    canvas.drawColor(Color.parseColor("#313744"));
                    canvas.drawBitmap(bitmap, 0, title_layout.getHeight(), paint); // 绘制图片
                    canvas.translate(0, 0);
                    title_layout.draw(canvas);*/
        try {
            CopyFileToSD.sharedPrefsFile(getPackageName(), SharedPreferencesUtil.FILE_NAME);
            File sharedPrefsFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + SharedPreferencesUtil.FILE_NAME + ".xml");
            Intent intent = new Intent(Intent.ACTION_SEND);
            //                        intent.setType("text/plain"); //分享文字
            //            intent.setType("image/*");
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(sharedPrefsFile)); //分享单张图片
            //                ArrayList<Uri> uriList = new ArrayList<>();
            //                String path = Environment.getExternalStorageDirectory() + File.separator;
            //                uriList.add(Uri.fromFile(new File(path + "australia_1.jpg")));
            //                uriList.add(Uri.fromFile(new File(path + "australia_2.jpg")));
            //                uriList.add(Uri.fromFile(new File(path + "australia_3.jpg")));
            //                        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            //                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList); //分享多张图片
            intent.putExtra(Intent.EXTRA_SUBJECT, "发送给纸纷飞（598157378）");
            intent.putExtra(Intent.EXTRA_TEXT, "发送给纸纷飞（598157378）");//设置分享的文字内容
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //每次都显示分享列表
            startActivity(Intent.createChooser(intent, "发送给纸纷飞（598157378）")); //getTitle()设置分享列表的标题
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hub.removeListener(deviceListener);
    }
}
