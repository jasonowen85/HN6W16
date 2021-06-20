package com.adayo.adayodemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.adayo.systemserviceproxy.SystemServiceConst;
import com.adayo.systemserviceproxy.SystemServiceManager;

import java.util.List;

/**
 * 6W平台获取SN、VIN、Sign、Sign id
 * @author dengz
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private TextView tvSignBin;
    private TextView tvSignId;
    private TextView tvSn;
    private TextView tvVin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgets();
        Boolean res = SystemServiceManager.getInstance().conectsystemService();
        Log.i(TAG, "onCreate res = " + res);
    }

    private void initWidgets() {
        findViewById(R.id.btn_get_sn).setOnClickListener(this);
        tvSn = findViewById(R.id.tv_sn);
        findViewById(R.id.btn_get_vin).setOnClickListener(this);
        tvVin = findViewById(R.id.tv_vin);
        findViewById(R.id.btn_get_sign_bin).setOnClickListener(this);
        tvSignBin = findViewById(R.id.tv_sign_bin);
        findViewById(R.id.btn_get_sign_id).setOnClickListener(this);
        tvSignId = findViewById(R.id.tv_sign_id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_get_sn:
                String sn = SystemServiceManager.getInstance().
                        getSystemConfigInfo((byte) SystemServiceConst.SYS_CONFIG_SERNUM);
                if(!TextUtils.isEmpty(sn)){
                    tvSn.setText(sn);
                }
                Log.i(TAG, "sn = " + sn);
                break;
            case R.id.btn_get_vin:
                String vid = SystemServiceManager.getInstance().
                        getSystemConfigInfo((byte) SystemServiceConst.SYS_CONFIG_VIN);
                if(!TextUtils.isEmpty(vid)){
                    tvVin.setText(vid);
                }
                Log.i(TAG, "vid = " + vid);
                break;
            case R.id.btn_get_sign_bin:
                String sign = SystemServiceManager.getHuSNSign();
                if(!TextUtils.isEmpty(sign)){
                    tvSignBin.setText(sign);
                }
                Log.i(TAG, "sign = " + sign);
                break;
            case R.id.btn_get_sign_id:
                String id = SystemServiceManager.getHuIdSign();
                if(!TextUtils.isEmpty(id)){
                    tvSignId.setText(id);
                }
                Log.i(TAG, "id = " + id);
                break;
            default:
                break;
        }
    }
}
