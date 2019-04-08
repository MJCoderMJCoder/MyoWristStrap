package com.lzf.myowriststrap.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
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
            Manifest.permission.BLUETOOTH_PRIVILEGED,
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
    //蓝牙设备适配器
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //测试MYO腕带文本按钮
    private TextView sampleText;
    //MYO的核心
    private Hub hub;
    //线程控制
    private boolean threadControl = false;
    //数据 //FIFO队列
    private List<String> queue = Collections.synchronizedList(new LinkedList<String>());
    /**
     * 数据录取线程
     */
    private Thread thread = new Thread() {
        @Override
        public void run() {
            super.run();
            while (threadControl) {
                if (queue != null && queue.size() > 0) {
                    try {
                        String str[] = queue.remove(0).split(";");
                        SharedPreferencesUtil.put(MainActivity.this, str[0], str[1]);
                    } catch (Exception e) {
                        SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
    /**
     * 手机蓝牙广播接收器。
     */
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                    case BluetoothAdapter.STATE_ON:
                        //                                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                        //                                startActivity(intent);
                        // Use this instead to connect with a Myo that is very near (ie. almost touching) the device
                        // Finally, scan for Myo devices and connect to the first one found that is very near.
                        hub.attachToAdjacentMyo();
                        //                        hub.attachByMacAddress("EC:F2:AE:2D:F3:8D");
                        sampleText.setText("正在尝试连接...");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        sampleText.setTextColor(Color.GRAY);
                        sampleText.setText("正在打开蓝牙");
                        if (bluetoothAdapter != null) {
                            bluetoothAdapter.enable();
                        } else {
                            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            bluetoothAdapter.enable();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };
    /**
     * You need to register a DeviceListener with the Hub in order to receive Myo events.
     * If you don't want to implement the entire interface, you can extend AbstractDeviceListener and override only the methods you care about.
     */
    private DeviceListener deviceListener = new AbstractDeviceListener() {

        @Override
        public void onAttach(Myo myo, long timestamp) {
            try {
                String arm = "";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "左手臂上的";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "右手臂上的";
                }
                sampleText.setText("正在连接" + arm + "MYO腕带...");
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";连接MYO时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onConnect(Myo myo, long timestamp) {
            try {
                String arm = "手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "右手臂上";
                }
                myo.vibrate(Myo.VibrationType.LONG);
                //                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                //                startActivity(intent);
                sampleText.setText(arm + "的MYO腕带已连接");
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";MYO已连接");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            try {
                String armStr = "MYO腕带不在手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    armStr = "MYO腕带在左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    armStr = "MYO腕带在右手臂上";
                }
                if (xDirection == XDirection.TOWARD_ELBOW) {
                    sampleText.setText(armStr + "当前朝向胳膊肘部"); //USB端口朝向
                } else if (xDirection == XDirection.TOWARD_WRIST) {
                    sampleText.setText(armStr + "当前朝向手腕"); //USB端口朝向
                } else {
                    sampleText.setText(armStr);
                }
                queue.add(yMdHmsS.format(timestamp) + "-" + armStr + "-" + xDirection + ";当MYO识别出它在手臂上时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onUnlock(Myo myo, long timestamp) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";当同步的MYO解锁时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            try {
                String arm = "MYO腕带不在手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "MYO腕带在左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "MYO腕带在右手臂上";
                }
                // Handle the cases of the Pose enumeration, and change the text of the text view based on the pose we receive.
                switch (pose) {
                    case UNKNOWN:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //未知
                        break;
                    case REST:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //休息、轻松
                        break;
                    case DOUBLE_TAP:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //双发快射、双击。tap：轻敲；轻打；轻拍；轻击；轻叩
                        break;
                    case FIST:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //紧握；握成拳头
                        break;
                    case WAVE_IN:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //挥手、摆动、招手（向里摆动：左手是向右摆动、右手是向左摆动。）
                        break;
                    case WAVE_OUT:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //挥手、摆动、招手（向外摆动：左手是向左摆动、右手是向右摆动。）
                        break;
                    case FINGERS_SPREAD:
                        sampleText.setText(arm + " - 暂时无法识别；敬请期待。"); //手指伸展开（手掌展开）
                        break;
                }
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + "-" + pose + ";当MYO提供了新姿势时调用" + myo.getMacAddress() + "-" + myo.getName());
                //TODO: Do something awesome.
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + "-" + rotation + ";当MYO提供新的方向数据时调用");
                //TODO: Do something awesome.
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + "-" + accel + ";当MYO提供新的加速度计数据时调用");
                //TODO: Do something awesome.
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + "-" + gyro + ";当MYO提供新的陀螺仪数据时调用");
                //TODO: Do something awesome.
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        /**
         * @param myo
         * @param timestamp
         * @param rssi      由蓝牙硬件报告的远程设备的RSSI值。0如果没有RSSI值可用。(rssi)
         *                  rssi >= -50 时 相间距离<50cm； -50>rssi<=-70 时 相间距离<200cm；   -70>rssi<=-80 时 相间距离<400cm； -80>rssi<=-100 时 相间距离<900cm；
         */
        @Override
        public void onRssi(Myo myo, long timestamp, int rssi) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + "-" + rssi + ";当requestRssi()触发读取MYO的RSSI值时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onLock(Myo myo, long timestamp) {
            try {
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";当同步的Myo被锁定时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            try {
                String arm = "手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "右手臂上";
                }
                sampleText.setText("MYO腕带正从" + arm + "移开");
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";将Myo从手臂移开或移除时调用");
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            try {
                String arm = "手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "右手臂上";
                }
                sampleText.setText(arm + "的MYO腕带已断开链接");
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";连接的MYO断开连接时调用");
                sampleText.setTextColor(Color.BLACK);
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void onDetach(Myo myo, long timestamp) {
            try {
                String arm = "手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    arm = "手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    arm = "右手臂上";
                }
                sampleText.setText(arm + "的MYO腕带信号太弱");
                queue.add(myo + "-" + yMdHmsS.format(timestamp) + ";当MYO分离时调用");
                sampleText.setTextColor(Color.BLACK);
            } catch (Exception e) {
                SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                e.printStackTrace();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
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
                if (thread != null) {
                    threadControl = true;
                    thread.start();
                }
                registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                hub.addListener(deviceListener);
                // Set the maximum number of simultaneously attached Myos to 2.
                //            hub.setMyoAttachAllowance(2);
                // Example of a call to a native method
                sampleText = (TextView) findViewById(R.id.sample_text);
                sampleText.setText(stringFromJNI());
                sampleText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (sampleText.getCurrentTextColor() == Color.BLACK) {
                                if (bluetoothAdapter != null) {
                                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                }
                                sampleText.setTextColor(Color.GRAY);
                                if (bluetoothAdapter.isEnabled()) {
                                    //                                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                                    //                                startActivity(intent);
                                    // Use this instead to connect with a Myo that is very near (ie. almost touching) the device
                                    // Finally, scan for Myo devices and connect to the first one found that is very near.
                                    hub.attachToAdjacentMyo();
                                    //                                    hub.attachByMacAddress("EC:F2:AE:2D:F3:8D");
                                    sampleText.setText("正在尝试连接...");
                                } else {
                                    sampleText.setText("正在打开蓝牙");
                                    bluetoothAdapter.enable();
                                }
                            }
                        } catch (Exception e) {
                            SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sampleText.setTextColor(Color.BLACK);
    }

    /**
     * 判断哪些权限未授予以便在必要的时候重新申请
     * 判断存储未授予权限的集合permissionList是否为空：未授予的权限为空，表示都授予了
     */

    private void permissionIsGranted() {
        try {
            permissionList.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                for (String permission : PERMISSIONS) {
                    if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) { //该权限已经授予
                        //判断是否需要 向用户解释，为什么要申请该权限
                        ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        permissionList.add(permission);
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
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
        } catch (Exception e) {
            SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            threadControl = false;
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
            finish();
        } catch (Exception e) {
            SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
            e.printStackTrace();
        }
        //        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            threadControl = false;
            if (hub != null) {
                hub.removeListener(deviceListener);
                hub.shutdown();
            }
            if (bluetoothAdapter != null) {
                bluetoothAdapter.disable();
            }
            unregisterReceiver(broadcastReceiver);
            System.exit(0);
        } catch (Exception e) {
            SharedPreferencesUtil.put(MainActivity.this, e.getMessage(), e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
}
