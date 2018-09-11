package com.ucast.jnidiaoyongdemo;

import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.VideoView;

import com.ucast.jnidiaoyongdemo.bmpTools.EpsonPicture;
import com.ucast.jnidiaoyongdemo.testActs.BaseNavActivity;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.tools.SavePasswd;
import com.ucast.jnidiaoyongdemo.tools.YinlianHttpRequestUrl;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.Event;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

@ContentView(R.layout.activity_setting)
public class SettingActivity extends BaseNavActivity {

    @ViewInject(R.id.upload_pic_edt)
    EditText upload_pic_host;
    @ViewInject(R.id.heart_beat_edt)
    EditText heart_beat_host;
    @ViewInject(R.id.moneybox_ed)
    EditText money_box_time;
    @ViewInject(R.id.select_papper)
    RadioGroup select_papper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        x.view().inject(this);
        initToolbar(getString(R.string.settings));
        select_papper.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.papper_80:
                        SavePasswd.getInstace().save(SavePasswd.IS58PAPPER,"false");
                        break;
                    case R.id.papper_58:
                        SavePasswd.getInstace().save(SavePasswd.IS58PAPPER,"true");
                        break;
                }
            }
        });
        String is_58 = SavePasswd.getInstace().getIp(SavePasswd.IS58PAPPER,"false");
        if (is_58.equals("true")){
            select_papper.check(R.id.papper_58);
        }else{
            select_papper.check(R.id.papper_80);
        }
        upload_pic_host.setText(save.readxml(SavePasswd.BMPUPLOADHOST,YinlianHttpRequestUrl.ANALYZEHOST));
        heart_beat_host.setText(save.readxml(SavePasswd.HEARTBEATHOST,YinlianHttpRequestUrl.HEART_BEAT_HOST));
        money_box_time.setText(save.getIp(SavePasswd.MONEYTIME,"100"));
    }

    @Event(R.id.set_host_btn)
    private void setHost(View v){
        String new_upload_pic_host = upload_pic_host.getText().toString().trim();
        String new_heartbeat_host = heart_beat_host.getText().toString().trim();
        //存到xml中
        save.save(SavePasswd.BMPUPLOADHOST,new_upload_pic_host);
        save.savexml(SavePasswd.BMPUPLOADHOST,new_upload_pic_host);
        save.save(SavePasswd.HEARTBEATHOST,new_heartbeat_host);
        save.savexml(SavePasswd.HEARTBEATHOST,new_heartbeat_host);
        YinlianHttpRequestUrl.ANALYZEHOST = new_upload_pic_host;
        YinlianHttpRequestUrl.HEART_BEAT_HOST= new_heartbeat_host;
        YinlianHttpRequestUrl.setMainServiceUrl();

        showToast("设置成功");
    }

    @Event(R.id.moneybox_btn)
    private void setMoneyBoxTime(View v){
        String time = money_box_time.getText().toString().trim();
        save.save(SavePasswd.MONEYTIME,time);
        MyTools.openMoneyBox();
        showToast("设置钱箱参数成功");
    }
}
