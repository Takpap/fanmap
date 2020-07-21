package com.takpap.youngmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.takpap.youngmap.utils.httpConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ActivityLogin extends AppCompatActivity {

    private TextView username;
    private TextView password;
    private ProgressBar progress_circular;
    private Boolean User = false;
    private String TAG = "logActivityLogin";
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        progress_circular = findViewById(R.id.progress_circular);
        Button loginButton = findViewById(R.id.login);
        if(isFirstLogin()){
            startActivity(new Intent(ActivityLogin.this, MainActivity.class));
            finish();
            return ;
        }
        loginButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(View v) {
                if (password.getText().toString().trim().isEmpty()) {
                    Toast.makeText(ActivityLogin.this, "请先填入信息！", Toast.LENGTH_LONG).show();
                    return;
                }
                isUser();
                progress_circular.setVisibility(View.VISIBLE);
                handler = new Handler() {
                    @Override
                    public void handleMessage(@NonNull Message msg) {
                        Log.d(TAG, String.valueOf(msg.what));
                        if (msg.what == 1) {
                            progress_circular.setVisibility(View.INVISIBLE);
                            SharedPreferences.Editor editor = getSharedPreferences("userData",
                                    MODE_PRIVATE).edit();
                            editor.putString("userId", md5(password.getText().toString().trim()));
                            editor.apply();
                            startActivity(new Intent(ActivityLogin.this, MainActivity.class));
                            finish();
                        } else {
                            progress_circular.setVisibility(View.INVISIBLE);
                            Toast.makeText(ActivityLogin.this, "用户不存在或密码错误！", Toast.LENGTH_LONG).show();

                        }
                    }
                };
            }
        });
    }

    private Boolean isFirstLogin() {
        SharedPreferences sharedPreferences = getSharedPreferences("userData", MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId","");
        if(userId.isEmpty()){
            Toast.makeText(ActivityLogin.this,"第一次使用请先登录",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void isUser() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                String userId = md5(password.getText().toString().trim());
                Request request = new Request.Builder()
                        .url(httpConfig.getServerUrl() + "login?userId=" + userId)
                        .build();
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert response != null;
                if (response.isSuccessful()) {
                    try {
                        String mesResponse = Objects.requireNonNull(response.body()).string();
                        String status = new JSONObject(mesResponse).getString("status");
                        User = Boolean.parseBoolean(status);
                        Message message = new Message();
                        if (User) {
                            message.what = 1;
                            message.obj = true;
                        } else {
                            message.what = 2;
                            message.obj = false;
                        }
                        handler.sendMessage(message);
                        Log.d(TAG, String.valueOf(User));
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
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
