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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzf.myowriststrap.LzfApplication;
import com.lzf.myowriststrap.R;
import com.lzf.myowriststrap.Util.CopyFileToSD;
import com.lzf.myowriststrap.bean.DataLog;
import com.lzf.myowriststrap.bean.ExceptionLog;
import com.lzf.myowriststrap.jni.MTCNN;
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

    /**
     * 声明一个集合，在后面的代码中用来存储用户拒绝授权的一系列权限
     */
    private List<String> permissionList = new ArrayList<String>();
    //蓝牙设备适配器
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    //测试MYO腕带文本按钮
    private TextView sampleText;
    //显示MYO腕带相应动作
    private ImageView imageView;
    //MYO的核心
    private Hub hub;
    //数据录入线程控制
    private boolean dataEntryThreadControl = false;
    //数据 //FIFO队列
    private List<DataLog> dataLogList = Collections.synchronizedList(new LinkedList<DataLog>());
    //上一个手势
    private String lastPose = "";
    //上一个手势的最新事件
    private long lastPoseTime = 0L;
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
     * 数据录入线程
     */
    private Thread dataEntryThread = new Thread() {
        @Override
        public void run() {
            super.run();
            while (dataEntryThreadControl) {
                try {
                    if (dataLogList != null && dataLogList.size() > 0) {
                        DataLog dataLog = dataLogList.remove(0);
                        LzfApplication.getDaoSession(MainActivity.this).getDataLogDao().insertWithoutSettingPk(dataLog);
                    }
                } catch (Exception e) {
                    exceptionLog("thread-run", e.getMessage());
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
            try {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)) {
                        case BluetoothAdapter.STATE_ON:
                            //                                Intent intent = new Intent(MainActivity.this, ScanActivity.class);
                            //                                startActivity(intent);
                            // Use this instead to connect with a Myo that is very near (ie. almost touching) the device
                            // Finally, scan for Myo devices and connect to the first one found that is very near.
                            //                        hub.attachToAdjacentMyo();
                            hub.attachByMacAddress("EC:F2:AE:2D:F3:8D");
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
            } catch (Exception e) {
                exceptionLog("broadcastReceiver-onReceive", e.getMessage());
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
                String armStr = "";
                if (myo.getArm() == Arm.LEFT) {
                    armStr = "左手臂上的";
                } else if (myo.getArm() == Arm.RIGHT) {
                    armStr = "右手臂上的";
                }
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), "正在连接" + armStr + "MYO腕带...", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText("正在连接" + armStr + "MYO腕带...");
            } catch (Exception e) {
                exceptionLog("deviceListener-onAttach", e.getMessage());
            }
        }

        @Override
        public void onConnect(Myo myo, long timestamp) {
            try {
                String armStr = "手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    armStr = "左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    armStr = "右手臂上";
                }
                myo.vibrate(Myo.VibrationType.LONG);
                //                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                //                startActivity(intent);
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), armStr + "的MYO腕带已连接", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText(armStr + "的MYO腕带已连接");
            } catch (Exception e) {
                exceptionLog("deviceListener-onConnect", e.getMessage());
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
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), armStr, arm + "", xDirection + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText(armStr);
            } catch (Exception e) {
                exceptionLog("deviceListener-onArmSync", e.getMessage());
            }
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            try {
                String armStr = "MYO腕带不在手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    armStr = "MYO腕带在左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    armStr = "MYO腕带在右手臂上";
                }
                // Handle the cases of the Pose enumeration, and change the text of the text view based on the pose we receive.
                switch (pose) {
                    case UNKNOWN:
                        armStr += " - 暂时无法识别；敬请期待。"; //未知
                        break;
                    case REST:
                        armStr += " - 呵护"; //休息、轻松（relax your armStr）
                        break;
                    case DOUBLE_TAP:
                        armStr += " - 暂时无法识别；敬请期待。"; //双发快射、双击。大拇指和中指相互连续碰两下。
                        break;
                    case FIST:
                        armStr += " - 握拳"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                        break;
                    case WAVE_IN:
                        armStr += " - 暂时无法识别；敬请期待。"; //挥手、摆动、招手（向里摆动：左手是向右摆动、右手是向左摆动。）
                        break;
                    case WAVE_OUT:
                        armStr += " - 暂时无法识别；敬请期待。"; //挥手、摆动、招手（向外摆动：左手是向左摆动、右手是向右摆动。）
                        break;
                    case FINGERS_SPREAD:
                        armStr += " - 伸展"; //（五个都）手指伸展开（手掌展开）
                        break;
                }
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), armStr, myo.getArm() + "", myo.getXDirection() + "", pose + "", "", "", ""));
                //TODO: Do something awesome.
            } catch (Exception e) {
                exceptionLog("deviceListener-onPose", e.getMessage());
            }
        }

        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            try {
                String armStr = "MYO腕带不在手臂上";
                if (myo.getArm() == Arm.LEFT) {
                    armStr = "MYO腕带在左手臂上";
                } else if (myo.getArm() == Arm.RIGHT) {
                    armStr = "MYO腕带在右手臂上";
                }
                if (myo.getPose() == Pose.UNKNOWN) {
                    imageView.setVisibility(View.INVISIBLE);
                } else {
                    imageView.setVisibility(View.VISIBLE);
                }
                switch (myo.getPose()) {
                    case UNKNOWN:
                        armStr += " - 暂时无法识别；敬请期待。"; //未知
                        sampleText.setText(armStr);
                        break;
                    case REST:
                        imageView.setImageResource(R.drawable.care_rest);
                        armStr += " - 呵护"; //休息、轻松（relax your armStr）
                        sampleText.setText(armStr);
                        break;
                    case DOUBLE_TAP:
                        armStr += " - 暂时无法识别；敬请期待。"; //双发快射、双击。大拇指和中指相互连续碰两下。
                        break;
                    case FIST:
                        if (Math.abs(rotation.x()) > 0.1 && Math.abs(rotation.x()) < 0.19) {
                            imageView.setImageResource(R.drawable.give_like);
                            armStr += " - 点赞"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                            sampleText.setText(armStr);
                        } else if (Math.abs(rotation.x()) < 0.1) {
                            imageView.setImageResource(R.drawable.make_fist);
                            armStr += " - 握拳"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                            sampleText.setText(armStr);
                        }
                        break;
                    case WAVE_IN:
                        break;
                    case WAVE_OUT:
                        if (Math.abs(rotation.x()) > 0.014 && Math.abs(rotation.x()) < 0.080) {
                            imageView.setImageResource(R.drawable.ic_ok);
                            armStr += " - OK"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                            sampleText.setText(armStr);
                        }
                        break;
                    case FINGERS_SPREAD:
                        if (Math.abs(rotation.x()) > 0.479 && Math.abs(rotation.x()) < 0.493) {
                            imageView.setImageResource(R.drawable.spread_fingers);
                            armStr += " - 伸展"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                            sampleText.setText(armStr);
                        } else if (Math.abs(rotation.x()) > 0.0142 && Math.abs(rotation.x()) < 0.2054) { //Math.abs(rotation.x()) > 0.108 && Math.abs(rotation.x()) < 0.2054
                            imageView.setImageResource(R.drawable.ic_ok);
                            armStr += " - OK"; //紧握；握成拳头；握拳；（把手指）捏成拳头
                            sampleText.setText(armStr);
                        }
                        break;
                }
                lastPose = myo.getPose() + "";
                lastPoseTime = System.currentTimeMillis();
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), "当MYO提供新的方向数据时调用", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", rotation + "", "", ""));
                //TODO: Do something awesome.
            } catch (Exception e) {
                exceptionLog("deviceListener-onOrientationData", e.getMessage());
            }
        }

        @Override
        public void onAccelerometerData(Myo myo, long timestamp, Vector3 accel) {
            try {
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), "当MYO提供新的加速度计数据时调用", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", accel + "", ""));
                //TODO: Do something awesome.
            } catch (Exception e) {
                exceptionLog("deviceListener-onAccelerometerData", e.getMessage());
            }
        }

        @Override
        public void onGyroscopeData(Myo myo, long timestamp, Vector3 gyro) {
            try {
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), "当MYO提供新的陀螺仪数据时调用", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", gyro + ""));
                //TODO: Do something awesome.
            } catch (Exception e) {
                exceptionLog("deviceListener-onGyroscopeData", e.getMessage());
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
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), "MYO腕带正从" + arm + "移开", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText("MYO腕带正从" + arm + "移开");
            } catch (Exception e) {
                exceptionLog("deviceListener-onArmUnsync", e.getMessage());
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
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), arm + "的MYO腕带已断开连接", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText(arm + "的MYO腕带已断开连接");
                sampleText.setTextColor(Color.BLACK);
            } catch (Exception e) {
                exceptionLog("deviceListener-onDisconnect", e.getMessage());
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
                dataLogList.add(new DataLog(yMdHmsS.format(System.currentTimeMillis()), arm + "的MYO腕带信号太弱", myo.getArm() + "", myo.getXDirection() + "", myo.getPose() + "", "", "", ""));
                imageView.setVisibility(View.INVISIBLE);
                sampleText.setText(arm + "的MYO腕带信号太弱");
                sampleText.setTextColor(Color.BLACK);
            } catch (Exception e) {
                exceptionLog("deviceListener-onDetach", e.getMessage());
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
                if (dataEntryThread != null) {
                    dataEntryThreadControl = true;
                    dataEntryThread.start();
                }
                registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
                hub.addListener(deviceListener);
                // Set the maximum number of simultaneously attached Myos to 2.
                //            hub.setMyoAttachAllowance(2);
                // Example of a call to a native method
                sampleText = findViewById(R.id.sample_text);
                imageView = findViewById(R.id.image_view);
                sampleText.setText(new MTCNN().stringFromJNI());
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
                                    //                                    hub.attachToAdjacentMyo();
                                    hub.attachByMacAddress("EC:F2:AE:2D:F3:8D");
                                    sampleText.setText("正在尝试连接...");
                                } else {
                                    sampleText.setText("正在打开蓝牙");
                                    bluetoothAdapter.enable();
                                }
                            }
                        } catch (Exception e) {
                            exceptionLog("onCreate-sampleText-onClick", e.getMessage());
                        }
                    }
                });
            }
        } catch (Exception e) {
            exceptionLog("onCreate", e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            sampleText.setTextColor(Color.BLACK);
        } catch (Exception e) {
            exceptionLog("onCreate", e.getMessage());
        }
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
            exceptionLog("permissionIsGranted", e.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        try {
            dataEntryThreadControl = false;
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
            //            CopyFileToSD.sharedPrefsFile(getPackageName(), SharedPreferencesUtil.FILE_NAME);
            CopyFileToSD.databaseFile(getPackageName(), "MYO_WRIST_STRAP");
            File shareDatabaseFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MYO_WRIST_STRAP.db");
            Intent intent = new Intent(Intent.ACTION_SEND);
            //                        intent.setType("text/plain"); //分享文字
            //            intent.setType("image/*");
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(shareDatabaseFile)); //分享单张图片
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
            exceptionLog("onBackPressed", e.getMessage());
        }
        //        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            dataEntryThreadControl = false;
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
            exceptionLog("onDestroy", e.getMessage());
        }
    }

    /**
     * 将异常打印到数据库中
     *
     * @param methodName
     * @param message
     */
    private void exceptionLog(String methodName, String message) {
        try {
            LzfApplication.getDaoSession(this).getExceptionLogDao().insertWithoutSettingPk(new ExceptionLog("com.lzf.myowriststrap.activity.MainActivity", methodName, message, yMdHmsS.format(System.currentTimeMillis())));
        } catch (Exception e) {
            LzfApplication.getDaoSession(this).getExceptionLogDao().insertWithoutSettingPk(new ExceptionLog("com.lzf.myowriststrap.activity.MainActivity", "exceptionLog", e.getMessage(), yMdHmsS.format(System.currentTimeMillis())));
        }
    }
}
