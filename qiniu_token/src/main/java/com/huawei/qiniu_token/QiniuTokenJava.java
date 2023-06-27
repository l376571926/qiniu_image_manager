package com.huawei.qiniu_token;

import com.qiniu.util.Auth;

public class QiniuTokenJava extends BaseToken {
    private static final Auth auth = Auth.create(accessKey, secretKey);

    /**
     * 获取上传token
     *
     * @param bucket esp32-cam-image
     * @param key null
     * @param deadline 1686295372
     * @param policyJson null
     * @param strict true
     * @return oE6xigv1Yh9ioaeiEicw_WTFX3Dg4DldmIGvN--c:Xp4ZKBk9EaoQ_i6PwgiHQpgA3YI=:eyJzY29wZSI6ImVzcDMyLWNhbS1pbWFnZSIsImRlYWRsaW5lIjoxNjg2Mjk1MzcyfQ==
     */
    public static String uploadTokenWithDeadline(String bucket, String key, long deadline, String policyJson, boolean strict) {
        return auth.uploadTokenWithDeadline(bucket, key, deadline, null, true);
    }

    /**
     * 获取私有资源下载链接
     *
     * @param baseUrl  http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png
     * @param deadline 1686040163
     * @return http://rvruzr2c3.hn-bkt.clouddn.com/my-java.png?e=1686040163&token=oE6xigv1Yh9ioaeiEicw_WTFX3Dg4DldmIGvN--c:nn1iobjC2fYeBDpjAJoh8Tbhhsk=
     */
    public static String privateDownloadUrlWithDeadline(String baseUrl, long deadline) {
        return auth.privateDownloadUrlWithDeadline(baseUrl, deadline);
    }
}
