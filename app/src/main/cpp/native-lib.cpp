#include <jni.h>
#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_lzf_myowriststrap_jni_MTCNN_stringFromJNI(JNIEnv *env,
                                                   jobject /* this */ instance) {
    std::string hello = "测试MYO腕带";
    return env->NewStringUTF(hello.c_str());
}