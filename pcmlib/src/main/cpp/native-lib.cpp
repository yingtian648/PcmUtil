#include <jni.h>
#include <string>
#include "LameEncoder.h"

// 全局LameEncoder
LameEncoder *encoder = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_exa_pcmutil_util_PcmToMp3Util_init(JNIEnv *env, jobject thiz) {
    encoder = new LameEncoder();
    encoder->init();
}

/**
 * GetMethodID 获取java类方法
 * CallVoidMethod 调用返回值为void的java类方法
 * java方法返回值如下
 * Ljava/lang/String  java的String类型
 * V ->void
 * Z -> boolean
 * B -> byte
 * C -> char
 * S -> short
 * I -> int
 * J -> long
 * F -> float
 * D -> double
 */
extern "C"
JNIEXPORT void JNICALL
Java_com_exa_pcmutil_util_PcmToMp3Util_encode(JNIEnv *env, jobject thiz, jstring pcm_path,
                                              jstring mp3_path, jint audio_channels, jint bit_rate,
                                              jint sample_rate) {
    jclass jcl = env->GetObjectClass(thiz);
    jmethodID methodOnStart = env->GetMethodID(jcl, "onStart", "()V");
    jmethodID methodEncodeComplete = env->GetMethodID(
            jcl, "onEncodeComplete", "(Ljava/lang/String;Ljava/lang/String;)V");
    jmethodID methodEncodeException = env->GetMethodID(
            jcl, "onEncodeException", "(Ljava/lang/String;)V");
    const char *pcmPath = env->GetStringUTFChars(pcm_path, NULL);
    const char *mp3Path = env->GetStringUTFChars(mp3_path, NULL);
    //调用java类方法
    env->CallVoidMethod(thiz, methodOnStart);
    //转MP3
    if (encoder != nullptr) {
        encoder->encode(thiz, pcmPath, mp3Path, audio_channels,
                        bit_rate, sample_rate);
        env->CallVoidMethod(thiz, methodEncodeComplete, pcm_path, mp3_path);
    } else {
        const auto msg = reinterpret_cast<jstring const>('endcode should use after init');
        env->CallVoidMethod(thiz, methodEncodeException,msg);
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_exa_pcmutil_util_PcmToMp3Util_destroy(JNIEnv *env, jobject thiz) {
    if (encoder != nullptr) {
        encoder->destroy();
    }
}