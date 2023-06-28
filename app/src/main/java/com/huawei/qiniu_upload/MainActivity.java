package com.huawei.qiniu_upload;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.qiniu_token.BaseToken;
import com.huawei.qiniu_token.HexStringUtil;
import com.huawei.qiniu_token.QiniuToken;
import com.huawei.qiniu_token.QiniuTokenJava;
import com.huawei.qiniu_upload.databinding.ActivityMainBinding;
import com.huawei.qiniu_upload.databinding.LayoutListImageItemBinding;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;
import com.qiniu.util.UrlSafeBase64;
import com.socks.library.KLog;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final OkHttpClient client = new OkHttpClient();

    private ActivityMainBinding binding;
    private ItemAdapter mItemAdapter;
    private MqttAndroidClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.setListener(this);

        RecyclerView mRecyclerView = binding.recyclerView;
        mItemAdapter = new ItemAdapter();
        mRecyclerView.setAdapter(mItemAdapter);

        String serverURI = "tcp://" + MqttUserInfo.HOST_IP + ":" + MqttUserInfo.PORT;
        String clientId = MqttUserInfo.CLIENT_ID;//设备id
        mClient = new MqttAndroidClient(this, serverURI, clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(MqttUserInfo.USERNAME);//产品id
        options.setPassword(MqttUserInfo.PASSWORD.toCharArray());//设备id鉴权信息
        try {
            mClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    String[] topics = asyncActionToken.getTopics();
                    KLog.e(Arrays.toString(topics));
                    try {
                        if (mClient != null) {
                            IMqttToken subscribe = mClient.subscribe(TOPIC_WILL, TOPIC_WILL_QOS);
                            KLog.e(subscribe.getMessageId());
                        }
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    KLog.e(exception.toString());
                }
            });
            mClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    KLog.e();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String json = new String(message.getPayload());
                    KLog.e("topic = [" + topic + "],message = [" + json + "]");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, topic + ":" + json, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    try {
                        KLog.e(token.getMessage());
                    } catch (MqttException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        listImage();
    }

    String[] TOPIC_WILL = new String[]{
            "/topic/command/takePhoto/response"
    };
    int[] TOPIC_WILL_QOS = new int[]{
            0
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            IMqttToken disconnect = mClient.disconnect();
            KLog.e(disconnect.getMessageId());
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    void test2() {
//        long deadline = 1687169022L;
//        long deadline = 1687190027L;
//        long deadline = 1687203904L;
        Auth auth = Auth.create(BaseToken.accessKey, BaseToken.secretKey);
        //{"map":{"scope":"esp32-cam-image","deadline":1687190027}}
        //param_base64_result-->
        //eyJtYXAiOnsic2NvcGUiOiJlc3AzMi1jYW0taW1hZ2UiLCJkZWFkbGluZSI6MTY4NzE5MDAyN319
        //to hex string-->
        //65 79 4A 74 59 58 41 69 4F 6E 73 69 63 32 4E 76 63 47 55 69 4F 69 4A 6C 63 33 41 7A 4D 69 31 6A 59 57 30 74 61 57 31 68 5A 32 55 69 4C 43 4A 6B 5A 57 46 6B 62 47 6C 75 5A 53 49 36 4D 54 59 34 4E 7A 45 35 4D 44 41 79 4E 33 31 39
        //hmac-sha1-->
        //22 74 5A B0 05 98 60 DE 0D C3 C8 FE 69 7D BD AA B1 3E D4 88
        //sign_result_base64_result-->
        //InRasAWYYN4Nw8j-aX29qrE-1Ig=
        //-->
        //oE6xigv1Yh9ioaeiEicw_WTFX3Dg4DldmIGvN--c:InRasAWYYN4Nw8j-aX29qrE-1Ig=:eyJtYXAiOnsic2NvcGUiOiJlc3AzMi1jYW0taW1hZ2UiLCJkZWFkbGluZSI6MTY4NzE5MDAyN319
        String result = auth.uploadToken("esp32-cam-image");
//        String result = auth.uploadTokenWithDeadline("esp32-cam-image", null, deadline, null, true);
        KLog.e(TAG, "liyiwei: " + result);
    }

    void test1() {
        long deadline = System.currentTimeMillis() / 1000 + 3600;

        String token = QiniuTokenJava.uploadTokenWithDeadline("esp32-cam-image", null, deadline, null, true);
        KLog.e("upload token(java): " + token);

        String token1 = QiniuToken.uploadTokenWithDeadline("esp32-cam-image", null, deadline, null, true);
        KLog.e("upload token(jni): " + token1);
    }

    public void listImage() {
        String host = "rsf.qbox.me";
//        String path = "/list?bucket=esp32-cam-image&limit=10";
        String path = "/list?bucket=esp32-cam-image";
        String x_qiniu_date = xQiniuDate();
        KLog.e("X-Qiniu-Date = " + x_qiniu_date);
        String authorization = "Qiniu " + generateListFileToken(host, path, x_qiniu_date);
        KLog.e("Authorization = " + authorization);

        Request request = new Request.Builder()
                .url("https://" + host + path)
                .get()
                .header("Authorization", authorization)
                .header("X-Qiniu-Date", x_qiniu_date)
                .header("User-Agent", "QiniuJava/7.13.1 (Windows 10 amd64 10.0) Java/17.0.6")
                .build();
        //main
        KLog.e(Thread.currentThread().getName());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                KLog.e(Thread.currentThread().getName());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                //OkHttp https://rsf.qbox.me/...
                KLog.e(Thread.currentThread().getName());

                String json = response.body().string();
                KLog.e(json);
                try {
                    JSONObject object = new JSONObject(json);
                    if (object.has("error")) {
                        KLog.e(json);
                        return;
                    }
                    JSONArray items = object.getJSONArray("items");
                    Type type = new TypeToken<List<FileItemInfo>>() {
                    }.getType();
                    List<FileItemInfo> itemInfoList = new Gson().fromJson(items.toString(), type);
                    itemInfoList.sort(new Comparator<FileItemInfo>() {
                        @Override
                        public int compare(FileItemInfo o1, FileItemInfo o2) {
                            long putTime = o1.getPutTime();
                            long putTime1 = o2.getPutTime();
                            if (putTime > putTime1) {
                                return -1;
                            }
                            if (putTime < putTime1) {
                                return 1;
                            }
                            return 0;
                        }
                    });
                    mItemAdapter.replaceData(itemInfoList);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void delete(String key) {
        KLog.e("delete key: " + key);
        String host = "rs.qbox.me";
        String x_qiniu_date = xQiniuDate();
        KLog.e("x_qiniu_date = " + x_qiniu_date);
        String encodedEntry = UrlSafeBase64.encodeToString("esp32-cam-image" + ":" + key);
        KLog.e("encodedEntry = " + encodedEntry);
        String authorization = "Qiniu " + generateDeleteToken(host, encodedEntry, x_qiniu_date);
        KLog.e("authorization = " + authorization);
        String url = "https://" + host + "/delete/" + encodedEntry;
        KLog.e("url = " + url);
        MediaType t = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody requestBody = RequestBody.create(new byte[0], t);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", authorization)
                .header("X-Qiniu-Date", x_qiniu_date)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "QiniuJava/7.13.1 (Windows 10 amd64 10.0) Java/17.0.6")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                KLog.e(Thread.currentThread().getName());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                KLog.e(Thread.currentThread().getName());
                int code = response.code();//200
                String json = response.body().string();
                KLog.e(json);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (code == 200) {
                            Toast.makeText(MainActivity.this, key + " 删除成功", Toast.LENGTH_SHORT).show();
                            listImage();
                        } else {
                            Toast.makeText(MainActivity.this, json, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * @param host         rs.qbox.me
     * @param encodedEntry
     * @param x_qiniu_date 20230621T024626Z
     * @return
     */
    private static String generateDeleteToken(String host, String encodedEntry, String x_qiniu_date) {
        String params = "POST /delete/" + encodedEntry + "\n" +
                "Host: " + host + "\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "X-Qiniu-Date: " + x_qiniu_date + "\n" +
                "\n";
//        String params = "POST /delete/ZXNwMzItY2FtLWltYWdlOmJpbGliaWxpXzJfMS5wbmc=\n" +
//                "Host: rs.qbox.me\n" +
//                "Content-Type: application/x-www-form-urlencoded\n" +
//                "X-Qiniu-Date: 20230621T024626Z\n" +
//                "\n";
        return BaseToken.accessKey + ":" + encodeWithHmacSHA1(params);
    }

    /**
     * @param host         rsf.qbox.me
     * @param path         /list?bucket=esp32-cam-image&limit=10
     * @param x_qiniu_date 20230621T012540Z
     * @return
     */
    private static String generateListFileToken(String host, String path, String x_qiniu_date) {
        String params = "GET " + path + "\n" +
                "Host: " + host + "\n" +
                "X-Qiniu-Date: " + x_qiniu_date + "\n" +
                "\n";
//        String params = "GET /list?bucket=esp32-cam-image&limit=10\n" +
//                "Host: rsf.qbox.me\n" +
//                "X-Qiniu-Date: 20230621T012540Z\n" +
//                "\n";
        return BaseToken.accessKey + ":" + encodeWithHmacSHA1(params);
    }

    private static String encodeWithHmacSHA1(String data) {
        Mac mac;
        try {
            mac = javax.crypto.Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(BaseToken.secretKey.getBytes(), "HmacSHA1"));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        }
        mac.update(data.getBytes());

        return UrlSafeBase64.encodeToString(mac.doFinal());
    }

    private static String xQiniuDate() {
        DateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date(System.currentTimeMillis());
        return format.format(date);
    }

    /**
     * @param key aabbcc.webp
     * @return
     */
    private String getDownloadUrl(String key) {
        String url = "http://rvruzr2c3.hn-bkt.clouddn.com/" + key;
        long deadline = System.currentTimeMillis() / 1000 + 3600;

        return QiniuTokenJava.privateDownloadUrlWithDeadline(url, deadline);
    }

    /**
     * 拍照
     */
    public void takePhoto() {
        MqttMessage message = new MqttMessage();
        String msg = "takePhoto";
        message.setPayload(msg.getBytes());
        try {
            mClient.publish("/topic/command/takePhoto/request", message);
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.upload_toke_btn) {
//        byte[] bytes = QiniuToken.hmac_sha1(QiniuToken.secretKey, "http://rsf.qiniuapi.com/list?bucket=esp32-cam-image");
//        byte[] bytes = QiniuToken.hmac_sha1(QiniuToken.secretKey, "POST /move/bmV3ZG9jczpmaW5kX21hbi50eHQ=/bmV3ZG9jczpmaW5kLm1hbi50eHQ=\nHost: rs.qiniu.com\n\n");
//        String s = QiniuToken.base64(bytes, true);
            new Thread(new Runnable() {
                @Override
                public void run() {
//                test3();
                    test2();
//                test1();
                }
            }).start();

        } else if (viewId == R.id.download_token_btn) {
            String url = "http://rvruzr2c3.hn-bkt.clouddn.com/aabbcc.webp";
            long deadline = System.currentTimeMillis() / 1000 + 3600;

            String downloadUrl = QiniuTokenJava.privateDownloadUrlWithDeadline(url, deadline);
            KLog.e("downloadImage(Java): " + downloadUrl);

            String downloadUrl1 = QiniuToken.privateDownloadUrlWithDeadline(url, deadline);
            KLog.e("downloadImage(Jni): " + downloadUrl1);

        } else if (viewId == R.id.list_token_btn) {
            String host = "rsf.qbox.me";
//        String path = "/list?bucket=esp32-cam-image&limit=10";
            String path = "/list?bucket=esp32-cam-image";
            String x_qiniu_date = xQiniuDate();
            KLog.e("X-Qiniu-Date = " + x_qiniu_date);
            String authorization = "Qiniu " + generateListFileToken(host, path, x_qiniu_date);
            KLog.e("Authorization = " + authorization);

        } else if (viewId == R.id.take_photo_btn) {
            takePhoto();
        } else if (viewId == R.id.list_btn) {
            listImage();
        }
    }

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        LayoutListImageItemBinding itemBinding;

        public ItemViewHolder(LayoutListImageItemBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        List<FileItemInfo> dataList;

        public ItemAdapter() {
            this.dataList = new ArrayList<>();
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutListImageItemBinding itemBinding = LayoutListImageItemBinding.inflate(getLayoutInflater(), parent, false);
            return new ItemViewHolder(itemBinding);
        }

        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
            FileItemInfo fileItemInfo = dataList.get(position);
            String key = fileItemInfo.getKey();
            int fsize = fileItemInfo.getFsize();
            long putTime = fileItemInfo.getPutTime();

            holder.itemBinding.key.setText(key);

            String downloadUrl = getDownloadUrl(key);
            Glide.with(holder.itemBinding.image)
                    .load(downloadUrl)
                    .into(holder.itemBinding.image);
            holder.itemBinding.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), ImagePreviewActivity.class);
                    intent.putExtra("url", downloadUrl);
                    startActivity(intent);
                }
            });

            String fileSize = FileSizeFormat.getFileSize(fsize);
            holder.itemBinding.size.setText(fileSize);

            String time = dateFormat.format(new Date(putTime / 10000));
            holder.itemBinding.time.setText(time);

            holder.itemBinding.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle("提醒")
                            .setMessage("是否确认删除？")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    delete(key);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dataList.size();
        }

        public void replaceData(List<FileItemInfo> list) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataList.clear();
                    dataList.addAll(list);
                    notifyDataSetChanged();
                }
            });
        }
    }
}