package com.takpap.youngmap.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.takpap.youngmap.ActivityLogin;
import com.takpap.youngmap.MainActivity;
import com.takpap.youngmap.MyApplication;
import com.takpap.youngmap.R;
import com.takpap.youngmap.utils.httpConfig;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName;
    private EditText etGender;
    private EditText etAge;
    private EditText etLpn;
    private EditText etTel;
    private EditText etPwd;
    private EditText etPwd1;
    private Button btRegister;
    private Handler handler;
    private String name;
    private String gender;
    private String age;
    private String lpn;
    private String tel;
    private String pwd;
    private String pwd1;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        myApplication = (MyApplication) getApplication();
        initView();
        btRegister.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View v) {
                if (checkMes()) {
                    startRegisterUser();
                    handler = new Handler() {
                        @Override
                        public void handleMessage(@NonNull Message msg) {
                            if (msg.what == 1 && msg.arg1==1) {
                                Toast.makeText(RegisterActivity.this, "注册成功请登录！", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(RegisterActivity.this, ActivityLogin.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "注册失败", Toast.LENGTH_LONG).show();
                            }
                        }
                    };
                }
            }
        });
    }

    private void startRegisterUser() {
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("gender", gender)
                .add("age", age)
                .add("lpn", lpn)
                .add("tel", tel)
                .add("brand", Build.BRAND)
                .add("model", Build.MODEL)
                .add("release", Build.VERSION.RELEASE)
                .add("pwd", pwd)
                .add("pushId", myApplication.getDeviceUUID())
                .build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(httpConfig.getServerUrl()+"register")
                        .post(formBody)
                        .build();
//                Response response = null;
//                try {
//                    response = okHttpClient.newCall(request).execute();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                assert response != null;
//                if (response.isSuccessful()) {
//                    try {
//                        String mesResponse = Objects.requireNonNull(response.body()).string();
//                        String status = new JSONObject(mesResponse).getString("status");
//                        boolean isSuc = Boolean.parseBoolean(status);
//                        Message message = new Message();
//                        if(isSuc){
//                            message.what = 1;
//                            message.arg1 = 1;
//                        }else {
//                            message.what = 1;
//                            message.arg1 = 2;
//                        }
//                        handler.sendMessage(message);
//                    } catch (IOException | JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        Log.d("receivelocationFailed", String.valueOf(e));
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        String mesResponse = Objects.requireNonNull(response.body()).string();
                        Log.d("receivelocationSuccess",mesResponse);
                    }
                });
            }
        }).start();
    }


    private boolean checkMes() {
        String telRegex = "^((13[0-9])|(14[5,7,9])|(15[^4])|(18[0-9])|(17[0,1,3,5,6,7,8]))\\d{8}$";
        String lpnRegex = "([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}(([0-9]{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))|([京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领A-Z]{1}[A-Z]{1}[A-HJ-NP-Z0-9]{4}[A-HJ-NP-Z0-9挂学警港澳]{1})";
        name = etName.getText().toString().trim();
        gender = etGender.getText().toString().trim();
        age = etAge.getText().toString().trim();
        lpn = etLpn.getText().toString().trim();
        tel = etTel.getText().toString().trim();
        pwd = etPwd.getText().toString().trim();
        pwd1 = etPwd1.getText().toString().trim();
        if (name.isEmpty() || gender.isEmpty() || age.isEmpty() || lpn.isEmpty() || pwd.isEmpty() || pwd1.isEmpty()) {
            Toast.makeText(this, "请完整填入信息", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!pwd.equals(pwd1)) {
            Toast.makeText(this, "两次填入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        } else if (!tel.matches(telRegex)) {
            Toast.makeText(this, "请确认手机号输入是否正确", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            return true;
        }
    }

    private void initView() {
        etName = findViewById(R.id.name);
        etGender = findViewById(R.id.gender);
        etAge = findViewById(R.id.age);
        etLpn = findViewById(R.id.lpn);
        etTel = findViewById(R.id.tel);
        etPwd = findViewById(R.id.pwd);
        etPwd1 = findViewById(R.id.pwd1);
        btRegister = findViewById(R.id.registerDriver);
    }

    public static String md5(String content) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }


}