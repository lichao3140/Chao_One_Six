package com.runvision.g69a_sn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.runvision.core.Const;
import com.runvision.utils.CameraHelp;
import com.runvision.utils.LogToFile;
import com.runvision.utils.SPUtil;
import java.lang.reflect.Field;

public class MySettingActivity extends FragmentActivity implements View.OnClickListener {

    private String TAG = this.getClass().getSimpleName();
    private TextView threshold_1,threshold_n,wait_for_time,open_time,device_ip,vms_ip,vms_port,vms_uername,vms_password,version,device_pass;
    private CheckBox cb_choice,cb_1_1open,cb_1_Nopen;
    private Spinner Preservation_time;
    private Button btn_Sure,btn_Refresh, btn_Back;
    private int preservation_day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_my_setting);
        // 全屏代码
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideBottomUIMenu();
        initView();
        initData();
    }

    private void initView() {
        device_pass= findViewById(R.id.device_pass);
        version= findViewById(R.id.version);
        threshold_1= findViewById(R.id.threshold_1);
        threshold_n= findViewById(R.id.threshold_n);
        wait_for_time= findViewById(R.id.wait_for_time);
        open_time= findViewById(R.id.open_time);
        device_ip= findViewById(R.id.device_ip);
        vms_ip= findViewById(R.id.vms_ip);
        vms_port= findViewById(R.id.vms_port);
        vms_uername= findViewById(R.id.vms_uername);
        vms_password= findViewById(R.id.vms_password);

        cb_choice= findViewById(R.id.cb_choice);
        cb_1_1open= findViewById(R.id.cb_1_1open);
        cb_1_Nopen= findViewById(R.id.cb_1_Nopen);

        btn_Sure= findViewById(R.id.btn_Sure);
        btn_Sure.setOnClickListener(this);

        btn_Refresh= findViewById(R.id.btn_Refresh);
        btn_Refresh.setOnClickListener(this);

        btn_Back = findViewById(R.id.btn_Back);
        btn_Back.setOnClickListener(this);

        Preservation_time = findViewById(R.id.Preservation_time);
        Preservation_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sexNumber = MySettingActivity.this.getResources().getStringArray(R.array.user_time)[i];
                System.out.println(sexNumber);
                if(sexNumber.equals("120天")) {
                    preservation_day=120;
                } else if(sexNumber.equals("90天")) {
                    preservation_day=90;
                } else if(sexNumber.equals("60天")) {
                    preservation_day=60;
                } else if(sexNumber.equals("30天")) {
                    preservation_day=30;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void initData() {
        version.setText("基本信息配置"+"   (V"+ LogToFile.getAppVersionName(MySettingActivity.this)+")");
        threshold_1.setText(String.valueOf(SPUtil.getFloat(Const.KEY_CARDSCORE, Const.ONEVSONE_SCORE)));
        threshold_n.setText(String.valueOf(SPUtil.getFloat(Const.KEY_ONEVSMORESCORE, Const.ONEVSMORE_SCORE)));
        wait_for_time.setText(String.valueOf(SPUtil.getInt(Const.KEY_BACKHOME, Const.CLOSE_HOME_TIMEOUT)));
        open_time.setText(String.valueOf(SPUtil.getInt(Const.KEY_OPENDOOR, Const.CLOSE_DOOR_TIME)));
        device_ip.setText(CameraHelp.getIpAddress());


        device_pass.setText(SPUtil.getString(Const.KEY_DEVICE_PASS, Const.DEVICE_PASS));

        //Preservation_time.set(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90));
        if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==120) {
            Preservation_time.setSelection(0);
        }
        else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==90) {
            Preservation_time.setSelection(1);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==60) {
            Preservation_time.setSelection(2);
        } else if(SPUtil.getInt(Const.KEY_PRESERVATION_DAY,90)==30) {
            Preservation_time.setSelection(2);
        }
        //活体
        if(SPUtil.getBoolean(Const.KEY_ISOPENLIVE, Const.OPEN_LIVE)==true) {
            cb_choice.setChecked(true);
        } else {
            cb_choice.setChecked(false);
        }

        //1:1
        if(SPUtil.getBoolean(Const.KEY_ISOPEN1_1, Const.OPEN_1_1)==true) {
            cb_1_1open.setChecked(true);
        } else {
            cb_1_1open.setChecked(false);
        }

        //1:n
        if(SPUtil.getBoolean(Const.KEY_ISOPEN1_N, Const.OPEN_1_N)==true) {
            cb_1_Nopen.setChecked(true);
        } else {
            cb_1_Nopen.setChecked(false);
        }

        vms_ip.setText(SPUtil.getString(Const.KEY_VMSIP, ""));
        vms_port.setText(Integer.toString(SPUtil.getInt(Const.KEY_VMSPROT,0)));
        vms_uername.setText(SPUtil.getString(Const.KEY_VMSUSERNAME, ""));
        vms_password.setText(SPUtil.getString(Const.KEY_VMSPASSWORD, ""));
    }

    private void setData() {
        SPUtil.addFloat(Const.KEY_CARDSCORE, Float.parseFloat(threshold_1.getText().toString().trim()));
        SPUtil.addFloat(Const.KEY_ONEVSMORESCORE, Float.parseFloat(threshold_n.getText().toString().trim()));
        SPUtil.putInt(Const.KEY_BACKHOME, Integer.parseInt(wait_for_time.getText().toString().trim()));
        SPUtil.putInt(Const.KEY_OPENDOOR, Integer.parseInt(open_time.getText().toString().trim()));
        //修改本地IP
        updateSetting(device_ip.getText().toString().trim(),MySettingActivity.this);

        SPUtil.putString(Const.KEY_DEVICE_PASS, device_pass.getText().toString().trim());


        SPUtil.putInt(Const.KEY_PRESERVATION_DAY, preservation_day);


        //活体
        if(cb_choice.isChecked()==true) {
            SPUtil.putBoolean(Const.KEY_ISOPENLIVE,true);
        } else {
            SPUtil.putBoolean(Const.KEY_ISOPENLIVE,false);
        }

        //1：1
        if(cb_1_1open.isChecked()==true) {
            SPUtil.putBoolean(Const.KEY_ISOPEN1_1,true);
        } else {
            SPUtil.putBoolean(Const.KEY_ISOPEN1_1,false);
        }

        //1：n
        if(cb_1_Nopen.isChecked()==true) {
            SPUtil.putBoolean(Const.KEY_ISOPEN1_N,true);
        } else {
            SPUtil.putBoolean(Const.KEY_ISOPEN1_N,false);
        }

        SPUtil.putString(Const.KEY_VMSIP, vms_ip.getText().toString().trim());
        SPUtil.putInt(Const.KEY_VMSPROT, Integer.parseInt(vms_port.getText().toString().trim()));
        SPUtil.putString(Const.KEY_VMSUSERNAME, vms_uername.getText().toString().trim());
        SPUtil.putString(Const.KEY_VMSPASSWORD, vms_password.getText().toString().trim());

        Amendsuccess();
        Const.WEB_UPDATE=true;
    }

    private void Amendsuccess() {
        //修改成功
        DialogInterface.OnCancelListener onCancelListener ;
        AlertDialog dialog =new AlertDialog.Builder(MySettingActivity.this)
                .setTitle("修改成功")
                .setIcon(R.mipmap.timg)
                .show();
        try {
            Field mAlert = AlertDialog.class.getDeclaredField("mAlert");
            mAlert.setAccessible(true);
            Object mAlertController = mAlert.get(dialog);
            Field mMessage = mAlertController.getClass().getDeclaredField("mMessageView");
            mMessage.setAccessible(true);
            TextView mMessageView = (TextView) mMessage.get(mAlertController);
            mMessageView.setTextColor(Color.GREEN);
            mMessageView.setTextSize(25);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = 200;
            params.height = 100 ;
            dialog.getWindow().setAttributes(params);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


    // 修改设置（IP�?????
    public static int updateSetting(String deviceip,Context context) {
        if(deviceip.equals(CameraHelp.getIpAddress())) {
            return 1;
        }
        if(deviceip.equals("")) {
            return 2;
        }
        String[] Sip = deviceip.split("\\.");

        String str = Sip[0] + "." + Sip[1] + "." + Sip[2] + ".1";
        Log.i("aa", str + "," + deviceip);

        String[] staticIP = new String[]{deviceip,
                "255.255.255.0", str, "8.8.8.8"};
        Intent closeIntent = new Intent("com.snstar.networkparameters.ETH_CLOSE");
        context.sendBroadcast(closeIntent);

        Intent i = new Intent("com.snstar.networkparameters.ETHSETINGS");
        Bundle bundle = new Bundle();
        bundle.putSerializable("STATIC_IP", staticIP);
        i.putExtras(bundle);
        context.sendBroadcast(i);
        Intent iopen = new Intent("com.snstar.networkparameters.ETH_OPEN");
        context.sendBroadcast(iopen);

        //finish();

        // ((Activity) mContext).finish();
        return 3;
    }


    public void open() {
        initData();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_Sure:
                setData();
                break;
            case R.id.btn_Refresh:
                initData();
                break;
            case R.id.btn_Back:
                finish();
                break;
            default:
                break;
        }
    }

    @SuppressLint("NewApi")
    protected void hideBottomUIMenu() {
        // 隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
