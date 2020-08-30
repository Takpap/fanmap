package com.takpap.youngmap.Fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

//导航类
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviCommonParams;
import com.baidu.navisdk.adapter.BaiduNaviManagerFactory;
import com.baidu.navisdk.adapter.IBNRoutePlanManager;
import com.baidu.navisdk.adapter.IBaiduNaviManager;
import com.baidu.navisdk.adapter.struct.BNTTsInitConfig;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;

import com.baidu.navisdk.util.common.LogUtil;
import com.takpap.youngmap.Activity.GuideActivity;
import com.takpap.youngmap.MainActivity;
import com.takpap.youngmap.R;
import com.takpap.youngmap.utils.NormalUtils;
import com.takpap.youngmap.utils.UtilLog;
import com.takpap.youngmap.utils.httpConfig;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;


public class PassengerDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView tvName;
    private TextView tvCount;
    private TextView tvStart;
    private TextView tvDest;
    private Button btIgnore;
    private Button btAccept;
    private Button btCall;
    private DialogFragmentDataCallback dialogFragmentDataCallback;
    //导航相关
    private String mSDCardPath = null;
    private BNRoutePlanNode mStartNode = null;
    private BNRoutePlanNode mEndNode = null;
    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";
    private Handler handler;
    private static String resp;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.diaolog_fragment_passenger, null);
        tvName = view.findViewById(R.id.passenger_name);
        tvCount = view.findViewById(R.id.passenger_count);
        tvStart = view.findViewById(R.id.passenger_start);
        tvDest = view.findViewById(R.id.passenger_dest);
        btIgnore = view.findViewById(R.id.passenger_ignore);
        btAccept = view.findViewById(R.id.passenger_accept);
        btCall = view.findViewById(R.id.passenger_call);
        btAccept.setOnClickListener(this);
        btCall.setOnClickListener(this);
        btIgnore.setOnClickListener(this);
        fillText();
        builder.setView(view);
        //初始化导航相关
        if (initDirs()) {
            initNavi();
        }
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        Window window = dialog.getWindow();
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams attributes = window.getAttributes();
        //设置Dialog窗口的高度
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置Dialog窗口的宽度
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        //设置Dialog的居中方向
        attributes.gravity = Gravity.CENTER;
        //设置Dialog弹出时背景的透明度
        attributes.dimAmount = 0.6f;
//        //设置Dialog水平方向的间距
//        attributes.horizontalMargin = 0f;
//        //设置Dialog垂直方向的间距
//        attributes.verticalMargin = 0f;
//        //设置Dialog显示时X轴的坐标,具体屏幕X轴的偏移量
//        attributes.x = 0;
//        //设置Dialog显示时Y轴的坐标,距离屏幕Y轴的偏移量
//        attributes.y = 0;
//        //设置Dialog的透明度
//        attributes.alpha = 0f;
//        //设置Dialog显示和消失时的动画
//        attributes.windowAnimations = 0;
        window.setAttributes(attributes);
    }

    public static class MyHandler extends Handler {
        private WeakReference<Activity> reference;
        public MyHandler(Activity activity) {
            reference = new WeakReference<Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            if (reference.get() != null) {
                if (msg.what == 1) {
                    resp = msg.obj.toString();
                }

            }
        }
    }


    public void fillText() {
        dialogFragmentDataCallback = (DialogFragmentDataCallback) getActivity();
        tvName.setText(dialogFragmentDataCallback.getPassengerName());
        tvCount.setText(dialogFragmentDataCallback.getPassengerCount());
        tvStart.setText(dialogFragmentDataCallback.getPassengerStart());
        tvDest.setText(dialogFragmentDataCallback.getPassengerDest());
        initRoutePlanNode();
    }

    @Override
    public void onClick(View v) {
        Log.d("logmain", "dialogFragmentDataCallback.getPassengerTel()");
        switch (v.getId()) {
            case R.id.passenger_ignore:
                Toast.makeText(getActivity(), "您拒绝了" + dialogFragmentDataCallback.getPassengerName() + "的打车请求", Toast.LENGTH_LONG).show();
                dismiss();
                break;

            case R.id.passenger_call:
                Log.d("logmain", dialogFragmentDataCallback.getPassengerTel());
                //获取输入的电话号码
                String phone = dialogFragmentDataCallback.getPassengerTel();
                //创建打电话的意图
                Intent intent = new Intent();
                //设置拨打电话的动作
                intent.setAction(Intent.ACTION_CALL);
                //设置拨打电话的号码
                intent.setData(Uri.parse("tel:" + phone));
                //开启打电话的意图
                startActivity(intent);
                dismiss();
                break;

            case R.id.passenger_accept:
                routePlanToNavi(mStartNode,mEndNode);
                sendToPassenger(dialogFragmentDataCallback.getOrderId());
                //dismiss();
                break;

        }

    }

    /**
     * 导航相关的各种方法
     */
    private void initRoutePlanNode() {
        mStartNode = new BNRoutePlanNode.Builder()
                .latitude(dialogFragmentDataCallback.getDriverLat())
                .longitude(dialogFragmentDataCallback.getDriverLon())
                .name("司机起点")
                .description("司机起点")
                .coordinateType(CoordinateType.BD09LL)
                .build();
        mEndNode = new BNRoutePlanNode.Builder()
                .latitude(dialogFragmentDataCallback.getPassengerStartLat())
                .longitude(dialogFragmentDataCallback.getPassengerStartLon())
                .name(dialogFragmentDataCallback.getPassengerStart())
                .description("乘客起点")
                .coordinateType(CoordinateType.GCJ02)
                .build();
    }

    private void routePlanToNavi(BNRoutePlanNode sNode, BNRoutePlanNode eNode) {
        List<BNRoutePlanNode> list = new ArrayList<>();
        list.add(sNode);
        list.add(eNode);
        if (getActivity() == null) {
            UtilLog.d("getActivity 获得一个空的上下文对象!", getActivity().toString());
        }
        UtilLog.d("getActivity ===", getActivity().toString());

        BaiduNaviManagerFactory.getRoutePlanManager().routeplanToNavi(list, IBNRoutePlanManager.RoutePlanPreference.ROUTE_PLAN_PREFERENCE_DEFAULT, null, new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_START:
                        Toast.makeText((MainActivity) getActivity(), "算路开始", Toast.LENGTH_SHORT).show();
                        break;
                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_SUCCESS:
                        Toast.makeText((MainActivity) getActivity(),
                                "算路成功", Toast.LENGTH_SHORT).show();
                        // 躲避限行消息
                        Bundle infoBundle = (Bundle) msg.obj;
                        if (infoBundle != null) {
                            String info = infoBundle.getString(BNaviCommonParams.BNRouteInfoKey.TRAFFIC_LIMIT_INFO);
                            Log.d("OnSdkDemo", "info = " + info);
                        }
                        break;
                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_FAILED:
                        Toast.makeText((MainActivity) getActivity(),
                                "算路失败", Toast.LENGTH_SHORT).show();
                        break;
                    case IBNRoutePlanManager.MSG_NAVI_ROUTE_PLAN_TO_NAVI:
                        Toast.makeText((MainActivity) getActivity(),
                                "算路成功准备进入导航", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PassengerDialogFragment.this.getContext(), GuideActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        // nothing
                        break;
                }
            }
        });
    }

    private void initNavi() {
        // 申请权限
//        if (android.os.Build.VERSION.SDK_INT >= 23) {
//            if (!hasBasePhoneAuth()) {
//                this.requestPermissions(authBaseArr, authBaseRequestCode);
//                return;
//            }
//        }

        if (BaiduNaviManagerFactory.getBaiduNaviManager().isInited()) {
            return;
        }

        BaiduNaviManagerFactory.getBaiduNaviManager().init(PassengerDialogFragment.this.getContext(), mSDCardPath, APP_FOLDER_NAME, new IBaiduNaviManager.INaviInitListener() {

            @Override
            public void onAuthResult(int status, String msg) {
                String result;
                if (0 == status) {
                    result = "key校验成功!";
                } else {
                    result = "key校验失败, " + msg;
                }
                Toast.makeText(PassengerDialogFragment.this.getContext(), result, Toast.LENGTH_LONG).show();
            }

            @Override
            public void initStart() {
                Toast.makeText(PassengerDialogFragment.this.getContext(),
                        "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void initSuccess() {
                Toast.makeText(PassengerDialogFragment.this.getContext(),
                        "百度导航引擎初始化成功", Toast.LENGTH_SHORT).show();
                // 初始化tts
                initTTS();
            }

            @Override
            public void initFailed(int errCode) {
                Toast.makeText(PassengerDialogFragment.this.getContext(),
                        "百度导航引擎初始化失败 " + errCode, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initTTS() {
        // 使用内置TTS
        BNTTsInitConfig config = new BNTTsInitConfig.Builder()
                .context(getContext())
                .sdcardRootPath(getSdcardDir())
                .appFolderName(APP_FOLDER_NAME)
                .appId(NormalUtils.getTTSAppID())
                .appKey(NormalUtils.getTTSAppKey())
                .secretKey(NormalUtils.getTTSsecretKey())
                .build();
        BaiduNaviManagerFactory.getTTSManager().initTTS(config);
    }

    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void sendToPassenger(String orderId) {
        UtilLog.d("开始发送请求" + orderId);
        SharedPreferences sp = getContext().getSharedPreferences("userData", getContext().MODE_PRIVATE);
        String name = sp.getString("name","无");
        String lpn = sp.getString("lpn","无");
        String tel = sp.getString("tel","无");
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(httpConfig.getServerUrl() + "ResponseToPassenger?orderId=" + orderId+"&name="+name+"&lpn="+lpn+"&tel="+tel)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Toast.makeText(getContext(),response.body().string(),Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
    }
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.diaolog_fragment_passenger, container, false);
//    }
}
