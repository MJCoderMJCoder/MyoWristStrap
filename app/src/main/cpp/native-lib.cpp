#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lzf_myowriststrap_activity_MainActivity_stringFromJNI(JNIEnv *env,
                                                               jobject /* this */ instance) {
    std::string hello = "测试MYO腕带";
    return env->NewStringUTF(hello.c_str());
}