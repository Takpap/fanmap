package com.takpap.youngmap.Fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.fragment.app.DialogFragment;

import com.takpap.youngmap.R;


public class PassengerDialogFragment extends DialogFragment implements View.OnClickListener {

    private TextView tvName;
    private TextView tvCount;
    private TextView tvStart;
    private TextView tvDest;
    private Button btIgnore;
    private Button btAccept;
    private Button btCall;
    private DialogFragmentDataCallback dialogFragmentDataCallback;

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
//                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // FIRE ZE MISSILES!
//                    }
//                })
//                .setNegativeButton("no", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // User cancelled the dialog
//                    }
//                });
        // Create the AlertDialog object and return it
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

    public void fillText(){
        dialogFragmentDataCallback = (DialogFragmentDataCallback) getActivity();
        tvName.setText(dialogFragmentDataCallback.getPassengerName());
        tvCount.setText(dialogFragmentDataCallback.getPassengerCount());
        tvStart.setText(dialogFragmentDataCallback.getPassengerStart());
        tvDest.setText(dialogFragmentDataCallback.getPassengerDest());
    }
    @Override
    public void onClick(View v) {
        Log.d("logmain","dialogFragmentDataCallback.getPassengerTel()");

        switch (v.getId()) {

            case R.id.passenger_ignore:
                Toast.makeText(getActivity(),"您拒绝了"+dialogFragmentDataCallback.getPassengerName()+"的打车请求",Toast.LENGTH_LONG).show();
                dismiss();
                break;

            case R.id.passenger_call:
                Log.d("logmain",dialogFragmentDataCallback.getPassengerTel());
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
                break;

            case R.id.passenger_accept:
                break;

        }

    }


//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.diaolog_fragment_passenger, container, false);
//    }
}
