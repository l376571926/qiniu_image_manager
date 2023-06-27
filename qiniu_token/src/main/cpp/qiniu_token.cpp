#include <jni.h>
#include <string>
#include "android/log.h"
#include "base64.h"

#ifdef __cplusplus
extern "C" {
#endif
#include <hmac/hmac.h>
#ifdef __cplusplus
}
#endif

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"JNI-LOG",__VA_ARGS__);

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_huawei_qiniu_1token_QiniuToken_hmac_1sha1(JNIEnv *env, jclass clz, jstring secret_key,
                                                   jstring data) {
    const char *sk = env->GetStringUTFChars(secret_key, nullptr);
    const char *data_ch = env->GetStringUTFChars(data, nullptr);

    char out[256] = {0};
    size_t len = sizeof(out);

    //https://github.com/Akagi201/hmac-sha1
    hmac_sha1(sk, strlen(sk), data_ch, strlen(data_ch), out, &len);

    jbyteArray result = env->NewByteArray(len);
    unsigned char dddd[len];
    for (int i = 0; i < len; ++i) {
        dddd[i] = out[i];
    }
    env->SetByteArrayRegion(result, 0, len, reinterpret_cast<const jbyte *>(dddd));

    env->ReleaseStringUTFChars(secret_key, sk);
    env->ReleaseStringUTFChars(data, data_ch);
    return result;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_huawei_qiniu_1token_QiniuToken_base64(JNIEnv *env, jclass clazz, jbyteArray data,
                                               jboolean url) {

    jbyte *jb = env->GetByteArrayElements(data, nullptr);
    jsize len = env->GetArrayLength(data);

    unsigned char aaa[120];
    for (int i = 0; i < len; i++) {
        aaa[i] = jb[i];
    }

    std::string encode_data = base64_encode(aaa, len, url);

    return env->NewStringUTF(encode_data.c_str());
}