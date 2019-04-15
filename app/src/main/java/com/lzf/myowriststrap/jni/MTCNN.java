package com.lzf.myowriststrap.jni;

/**
 * Created by MJCoder on 2019-04-12.
 */

public class MTCNN {
    //人脸检测模型导入
    public native boolean FaceDetectionModelInit(String faceDetectionModelPath);

    //人脸检测
    public native int[] FaceDetect(byte[] imageDate, int imageWidth, int imageHeight, int imageChannel);

    public native int[] MaxFaceDetect(byte[] imageDate, int imageWidth, int imageHeight, int imageChannel);

    //人脸检测模型反初始化
    public native boolean FaceDetectionModelUnInit();

    //检测的最小人脸设置
    public native boolean SetMinFaceSize(int minSize);

    //线程设置
    public native boolean SetThreadsNumber(int threadsNumber);

    //循环测试次数
    public native boolean SetTimeCount(int timeCount);

    static {
        System.loadLibrary("mtcnn");
    }


    // Used to load the 'native-lib' library on application startup.
    //    static {
    //        System.loadLibrary("native-lib");
    //    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
