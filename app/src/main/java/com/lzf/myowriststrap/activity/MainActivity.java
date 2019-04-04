package com.lzf.myowriststrap.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lzf.myowriststrap.R;
import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.scanner.ScanActivity;

import java.util.ArrayList;
import java.util.List;

import static com.lzf.myowriststrap.LzfApplication.REQUEST_PERMISSION_CODE;

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
    private final String[] PERMISSIONS = new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.INTERNET};
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
        public void onConnect(Myo myo, long timestamp) {
            Toast.makeText(MainActivity.this, "Myo Connected!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            Toast.makeText(MainActivity.this, "Myo Disconnected!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            Toast.makeText(MainActivity.this, "Pose: " + pose, Toast.LENGTH_SHORT).show();
            //TODO: Do something awesome.
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
    protected void onDestroy() {
        super.onDestroy();
        hub.removeListener(deviceListener);
    }
}
