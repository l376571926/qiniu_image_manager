package com.huawei.qiniu_token;

import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * {"scope":"esp32-cam-image","deadline":1686211814} --> eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2MjExODE0fQ==
 * <p>
 * Json.encode(x): {"scope":"esp32-cam-image","deadline":1686211814}
 * UrlSafeBase64.encodeToString(data): eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2MjExODE0fQ==
 * token: oE6xigv1Yh9ioaeiEicw_WTFX3Dg4DldmIGvN--c:WrRdtcFwJr3oFOEC22D9RDWiRFY=:eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2MjExODE0fQ==
 */
public class QiniuToken extends BaseToken {

    private static final String TAG = QiniuToken.class.getSimpleName();

    // Used to load the 'qiniu_token' library on application startup.
    static {
        System.loadLibrary("qiniu_token");
    }

    public static void testHmacSha1() {

//        privateDownloadUrl(accessKey, secretKey, "http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png");

//        privateDownloadUrl(accessKey, secretKey, "http://rvruzr2c3.hn-bkt.clouddn.com/bilibili_2_1_custom.png");

//        String sign = sign(accessKey, secretKey, "eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2Mjg0OTE3fQ==");
//        Log.e(TAG, "sign result token: " + sign);
    }

    public void testBase64() {
        Map<String, Object> map = new HashMap<>();
        map.put("scope", "esp32-cam-image");
        map.put("deadline", 1686214309);
        String src1 = new JSONObject(map).toString();

        Log.e(TAG, "java src.length(): " + src1.length());
        String encode = base64(src1.getBytes(), false);
        Log.e(TAG, "base64 encode: " + src1 + " --> " + encode);

        byte[] bytes = src1.getBytes();
        int length = bytes.length;
        String encode2 = base64(bytes, false);
        Log.e(TAG, "base64 encode2: " + src1 + " " + encode2);
    }

    /**
     * @param bucket
     * @param key
     * @param deadline
     * @param policyJson
     * @param strict
     * @return
     */
    public static String uploadTokenWithDeadline(String bucket, String key, long deadline, String policyJson, boolean strict) {
        Map<String, Object> map = new HashMap<>();
        map.put("scope", bucket);
        map.put("deadline", deadline);
        String json = new JSONObject(map).toString();
        String encode2 = base64(json.getBytes(), true);
        String sign = sign(encode2);
        return sign + ":" + encode2;
    }

    private static String sign(String base64) {
        byte[] bytes = hmac_sha1(secretKey, base64);
        String encode = base64(bytes, true);
        return accessKey + ":" + encode;
    }

    public static String sign1(String data){
        byte[] bytes = hmac_sha1(secretKey, data);
        String encode = base64(bytes, true);
        return accessKey + ":" + encode;
    }

    /**
     * //[11, -102, 50, 21, -42, -114, 72, 22, -125, 106, -49, -79, -95, -38, -57, -63, -11, -39, 20, -120]
     * //[11, -102, 50, 21, -42, -114, 72, 22, -125, 106, -49, -79, -95, -38, -57, -63, -11, -39, 20, -120]
     *
     * @param baseUrl   http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png
     * @return http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png?e=1686283873&token=oE6xigv1Yh9ioaeiEicw_WTFX3Dg4DldmIGvN--c:ZYjUh/NIZTBpbmcZUG/FMDFFWAM=
     */
    public static String privateDownloadUrlWithDeadline(String baseUrl, long deadline) {
        String url = baseUrl + "?e=" + deadline;
        byte[] bytes = hmac_sha1(secretKey, url);
        String encode = base64(bytes, true);
        String downloadUrl = url + "&token=" + (accessKey + ":" + encode);
        Log.e(TAG, "download url = " + downloadUrl);
        return downloadUrl;
    }

    /**
     * https://github.com/Akagi201/hmac-sha1
     *
     * @param secretKey
     * @param data
     * @return
     */
    public static native byte[] hmac_sha1(String secretKey, String data);

    /**
     * https://github.com/ReneNyffenegger/cpp-base64
     *
     * @param data data {"scope":"esp32-cam-image","deadline":1686214309}
     * @param url
     * @return eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2MjE0MzA5fQ==
     */
    public static native String base64(byte[] data, boolean url);
}