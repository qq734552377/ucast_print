package com.ucast.jnidiaoyongdemo;

import android.app.Dialog;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.ucast.jnidiaoyongdemo.Model.Config;
import com.ucast.jnidiaoyongdemo.Serial.PrinterSerialRestart;
import com.ucast.jnidiaoyongdemo.advAct.AdvActivity;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.xutilEvents.AdvActEvent;
import com.ucast.jnidiaoyongdemo.xutilEvents.MediapalyEvent;
import com.ucast.jnidiaoyongdemo.xutilEvents.MoneyBoxEvent;
import com.ucast.jnidiaoyongdemo.Model.MyUsbManager;
import com.ucast.jnidiaoyongdemo.jsonObject.HeartBeatResult;
import com.ucast.jnidiaoyongdemo.queue_ucast.ListPictureQueue;
import com.ucast.jnidiaoyongdemo.globalMapObj.MermoyKeyboardSerial;
import com.ucast.jnidiaoyongdemo.globalMapObj.MermoyPrinterSerial;
import com.ucast.jnidiaoyongdemo.globalMapObj.MermoyUsbWithByteSerial;
import com.ucast.jnidiaoyongdemo.protocol_ucast.MsCardProtocol;
import com.ucast.jnidiaoyongdemo.protocol_ucast.PrinterProtocol;
import com.ucast.jnidiaoyongdemo.Model.ReadPictureManage;
import com.ucast.jnidiaoyongdemo.queue_ucast.UploadDataQueue;
import com.ucast.jnidiaoyongdemo.Serial.KeyBoardSerial;
import com.ucast.jnidiaoyongdemo.Serial.OpenPrint;
import com.ucast.jnidiaoyongdemo.Serial.UsbWithByteSerial;
import com.ucast.jnidiaoyongdemo.jsonObject.BaseHttpResult;
import com.ucast.jnidiaoyongdemo.mytime.MyTimeTask;
import com.ucast.jnidiaoyongdemo.mytime.MyTimer;
import com.ucast.jnidiaoyongdemo.socket.net_print.NioNetPrintServer;
import com.ucast.jnidiaoyongdemo.tools.MyDialog;
import com.ucast.jnidiaoyongdemo.tools.SavePasswd;
import com.ucast.jnidiaoyongdemo.tools.YinlianHttpRequestUrl;
import com.ucast.jnidiaoyongdemo.xutilEvents.TishiMsgEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by pj on 2016/11/21.
 */
public class UpdateService extends Service {

    public static boolean connected;

    private static final long MONEYBOXTISHITIME = 1000L * 8;
    private static long oldMoneyBoxTime ;
    private static Dialog moneyBoxDialog;
    private Dialog msgDialog;

    private String last_request_time = "";
    private String last_alive_time = "";
    private boolean isFirstStart = true;

    KeyBoardSerial keyBoardSerial = null;
    UsbWithByteSerial usbPort = null;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return this.START_STICKY;
    }

    @Override
    public void onCreate() {

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        startForeground(1, notification);
        super.onCreate();
        startTimer();
        startMoneyBoxTimer();
        copyCfg("ums.bmp");
        copyCfg("ucast.bmp");
        OpenPrint print = new OpenPrint(Config.PrinterSerial);
        boolean isOpen = print.Open();
        if (isOpen){
//            print.Send(PrinterProtocol.getPrinterSwitchOnProtocol());
//            print.Send(MsCardProtocol.getOpenMsCardProtocol());
            MermoyPrinterSerial.Add(print);
        }

        PrinterSerialRestart.StartTimer();
//        UsbSerialRestart.StartTimer();
//        ListPictureQueue.StartTimer();
        UploadDataQueue.StartTimer();
        ReadPictureManage.GetInstance();

//        NioTcpServer tcpServer = new NioTcpServer(7700);
//        new Thread(tcpServer).start();

        //开启网口打印监听
        NioNetPrintServer netPrintServer = new NioNetPrintServer();
        new Thread(netPrintServer).start();

        String isOpenPrint = SavePasswd.getInstace().readxml(SavePasswd.ISOPENPRINT,SavePasswd.OPENPRINT);
        boolean isClose = isOpenPrint.equals(SavePasswd.CLOSEPRINT);
        setIsClosePrintMode(isClose);

        String netPrintUploadstr = SavePasswd.getInstace().readxml(SavePasswd.ISNETPRINTUPLOADTOSERVICE,SavePasswd.CLOSE);
        boolean isCloseNetPrintUpload = netPrintUploadstr.equals(SavePasswd.CLOSE);
        setCloseNetPrinterUploadToService(isCloseNetPrintUpload);

        registUsbBroadcast();
        moneyBoxDialog = MyDialog.showIsOpenMoneyBoxDialog();
        moneyBoxDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                startAdv();
            }
        });
        moneyBoxDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                EventBus.getDefault().post(new AdvActEvent(false));
            }
        });
        EventBus.getDefault().register(this);
        YinlianHttpRequestUrl.writeToTempFile();

        initMedia();
    }

    public void initUSbAndKeyboard(){
        if (keyBoardSerial == null && usbPort == null) {
            keyBoardSerial = new KeyBoardSerial(Config.KeyboardSerial);
            keyBoardSerial.Open();
            MermoyKeyboardSerial.Add(keyBoardSerial);
            usbPort = new UsbWithByteSerial(Config.UsbSerial);
            usbPort.Open();
            MermoyUsbWithByteSerial.Add(usbPort);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showIsOpenMoneyBox(MoneyBoxEvent event){
        if (!event.isShow()){
            if (moneyBoxDialog.isShowing()) {
                moneyBoxDialog.dismiss();
            }
            return;
        }
        if (moneyBoxDialog != null){
            oldMoneyBoxTime = System.currentTimeMillis();
            if (moneyBoxDialog.isShowing())
                return;
            moneyBoxDialog.show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showTishiMsg(TishiMsgEvent event){
        msgDialog = MyDialog.showUpdateResult(event.getMsg());
        msgDialog.show();
    }

    /**
     * 当服务被杀死时重启服务
     * */
    public void onDestroy() {
        stopForeground(true);
        Intent localIntent = new Intent();
        localIntent.setClass(this, UpdateService.class);
        EventBus.getDefault().unregister(this);
        if(receiver != null){
            unregisterReceiver(receiver);
        }
        this.startService(localIntent);    //销毁时重新启动Service
    }

    public void startAdv(){
        Intent start_adv_intent = new Intent(ExceptionApplication.getInstance(), AdvActivity.class);
        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        ExceptionApplication.getInstance().startActivity(start_adv_intent);
    }

    private BroadcastReceiver receiver;
    private IntentFilter intentFilter;
    public void registUsbBroadcast() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case UsbManager.ACTION_USB_ACCESSORY_ATTACHED:
                    case UsbManager.ACTION_USB_ACCESSORY_DETACHED:
                        UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                        break;
                    case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    case UsbManager.ACTION_USB_DEVICE_DETACHED:
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        break;
                    case MyUsbManager.ACTION_USB_STATE:
                        connected = intent.getBooleanExtra(MyUsbManager.USB_CONNECTED, false);
                        if (connected)
                            initUSbAndKeyboard();
                        if( !connected){
                            String usb_state = "0";
                            try {
                                usb_state = MyTools.loadFileAsString("/sys/devices/ff580000.usb/driver/vbus_status");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            if (usb_state.equals("1\n")){
                                connected = true;
                                initUSbAndKeyboard();
//                                EventBus.getDefault().post(new TishiMsgEvent(connected ? "连接着" : "没连上"));
                                return;
                            }
                            String url= YinlianHttpRequestUrl.TIMEUPDATEURL;
                            getSystemTime(url.trim());
                        }
//                        EventBus.getDefault().post(new TishiMsgEvent(connected ? "连接着" : "没连上"));
                        break;
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(MyUsbManager.ACTION_USB_STATE);
        registerReceiver(receiver, intentFilter);
    }

    public MyTimer timer;
    public MyTimer moneyBoxtimer;
    public void startTimer() {
        timer = new MyTimer(new MyTimeTask(new Runnable() {
            @Override
            public void run() {
                String url= YinlianHttpRequestUrl.TIMEUPDATEURL;
                getSystemTime(url.trim());
            }
        }), 1000*2L, 1*1000*60L);
        timer.initMyTimer().startMyTimer();
    }
    public void startMoneyBoxTimer() {
        moneyBoxtimer = new MyTimer(new MyTimeTask(new Runnable() {
            @Override
            public void run() {
                if(System.currentTimeMillis() - oldMoneyBoxTime < MONEYBOXTISHITIME){
                    return;
                }
                oldMoneyBoxTime = System.currentTimeMillis();
                EventBus.getDefault().postSticky(new MoneyBoxEvent(false));
            }
        }), 1000*2L, 1*1000*2L);
        moneyBoxtimer.initMyTimer().startMyTimer();
    }

    private static final String TAG = "UpdateService";
    public void getSystemTime(String url){
        //MyTools.writeToFileNoappend(Config.DEVICESTARTPATH,Config.DEVICE_ID + ",last_time," + MyTools.millisToDateString(System.currentTimeMillis()));
        //获取文件的时间
        String file_time_list = "";
        last_request_time = SavePasswd.getInstace().getIp(SavePasswd.LASTREQUESTTIME,MyTools.millisToDateString(System.currentTimeMillis()));
        last_alive_time = "";
        try {
            file_time_list = MyTools.loadFileAsString(Config.DEVICESTARTPATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (file_time_list.equals("")){
            //第一次启动app时没有记录的时候
            file_time_list = MyTools.millisToDateString(System.currentTimeMillis());
        }else{
            //有时间记录，取出最后一条比较一下小于2分钟 更新时间 大于2分钟 追加时间  LastRequestTime LastAliveTimeList
            String[] all_alive_time = file_time_list.trim().split(",");
            file_time_list = "";
            for (int i = 0; i < all_alive_time.length; i++) {
                if (i < all_alive_time.length - 1){
                    file_time_list += all_alive_time[i] + ",";
                    continue;
                }
                if (i > 1){
                    for (int j = 0; j < i; j++) {
                        if (j % 2 == 0){
                            last_alive_time += all_alive_time[j] + "_";
                            continue;
                        }
                        if ( j < i - 1 ){
                            last_alive_time += all_alive_time[j] + ",";
                            continue;
                        }
                        last_alive_time += all_alive_time[j];
                    }
                }
                Date last_date = MyTools.StringToDate(all_alive_time[i]);
                if (last_date ==null) {
                    last_date = new Date();
                    file_time_list = "";
                    last_alive_time = "";
                }
                long duration = System.currentTimeMillis() - last_date.getTime();
                if (duration > 1000 * 60 * 2) {
                    file_time_list += all_alive_time[i] + ",";
                    file_time_list += MyTools.millisToDateString(System.currentTimeMillis()) + ",";
                    if (!last_alive_time.equals(""))
                        last_alive_time += ",";
                    last_alive_time += all_alive_time[i] + "_" + MyTools.millisToDateString(System.currentTimeMillis());
//                    EventBus.getDefault().post(new TishiMsgEvent(last_alive_time));
                }
                file_time_list += MyTools.millisToDateString(System.currentTimeMillis());

            }
        }
        MyTools.writeToFileNoappend(Config.DEVICESTARTPATH,file_time_list);

        RequestParams params = new RequestParams(url);
        params.addBodyParameter("DeviceID",Config.DEVICE_ID);
        params.addBodyParameter("IsConnect",connected ? "true" : "false");
        params.addBodyParameter("LastRequestTime",last_request_time);
        params.addBodyParameter("LastAliveTimeList",last_alive_time);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseHttpResult base = JSON.parseObject(result, BaseHttpResult.class);
                if(base.getMsgType().equals(BaseHttpResult.SUCCESS) && base.getData() != null && !base.getData().equals("")){
                    //请求时间持续更新  和 断电时间去掉上传过的断电时间段
                    last_request_time = MyTools.millisToDateString(System.currentTimeMillis());
                    SavePasswd.getInstace().save(SavePasswd.LASTREQUESTTIME,last_request_time);
                    String file_time_list = "";
                    try {
                        file_time_list = MyTools.loadFileAsString(Config.DEVICESTARTPATH);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int last_index = file_time_list.lastIndexOf(",");
                    if (last_index != -1 ){
                        MyTools.writeToFileNoappend(Config.DEVICESTARTPATH,file_time_list.substring(last_index + 1));
                    }
                    //////////////////////////END
                    HeartBeatResult heartBeatResult = JSON.parseObject(base.getData(),HeartBeatResult.class);
                    boolean isCloseModle = heartBeatResult.IsOpenPrintModel.equals(SavePasswd.CLOSEPRINT);
                    setIsClosePrintMode(isCloseModle);
                    if(heartBeatResult.getIsNetPrintUploadToService() == null || heartBeatResult.getIsNetPrintUploadToService().equals(SavePasswd.CLOSE)){
                        setCloseNetPrinterUploadToService(true);
                    }else{
                        setCloseNetPrinterUploadToService(false);
                    }
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }

    public void setIsClosePrintMode(boolean isClose){
        if(isClose){
            SavePasswd.getInstace().save(SavePasswd.ISOPENPRINT,SavePasswd.CLOSEPRINT);
            SavePasswd.getInstace().savexml(SavePasswd.ISOPENPRINT,SavePasswd.CLOSEPRINT);
        }else {
            SavePasswd.getInstace().save(SavePasswd.ISOPENPRINT,SavePasswd.OPENPRINT);
            SavePasswd.getInstace().savexml(SavePasswd.ISOPENPRINT,SavePasswd.OPENPRINT);
        }
    }

    public void setCloseNetPrinterUploadToService(boolean isClose){
        if(isClose){
            SavePasswd.getInstace().save(SavePasswd.ISNETPRINTUPLOADTOSERVICE,SavePasswd.CLOSE);
            SavePasswd.getInstace().savexml(SavePasswd.ISNETPRINTUPLOADTOSERVICE,SavePasswd.CLOSE);
        }else {
            SavePasswd.getInstace().save(SavePasswd.ISNETPRINTUPLOADTOSERVICE,SavePasswd.OPEN);
            SavePasswd.getInstace().savexml(SavePasswd.ISNETPRINTUPLOADTOSERVICE,SavePasswd.OPEN);
        }
    }


    public void setTime(String mytime){
        Date mydate=StringToDate(mytime);
        long curMs=mydate.getTime();
        boolean isSuc = SystemClock.setCurrentTimeMillis(curMs);//需要Root权限
        Log.e(TAG, "setTime: "+isSuc );
    }
    private Date StringToDate(String s){
        Date time=null;
        SimpleDateFormat sd=new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            time=sd.parse(s);
        } catch (java.text.ParseException e) {
            System.out.println("输入的日期格式有误！");
            e.printStackTrace();
        }
        return time;
    }

    public void copyCfg(String picName) {
        String dirPath = Environment.getExternalStorageDirectory().getPath() + "/"+picName;
        FileOutputStream os = null;
        InputStream is = null;
        int len = -1;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream("assets/"+picName);
            os = new FileOutputStream(dirPath);
            byte b[] = new byte[1024];
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            is.close();
            os.close();
        } catch (Exception e) {
        }
    }
    MediaPlayer mediaPlayer = null;
    public void initMedia(){
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();
        setResource(R.raw.beep);
    }
    public void setResource(@RawRes int id){
        AssetFileDescriptor file = getResources().openRawResourceFd(id);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            file.close();
            mediaPlayer.setVolume(1.0f, 1.0f);
            mediaPlayer.prepare();
        } catch (IOException e) {
            mediaPlayer = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void startMedia(MediapalyEvent event){
//        mediaPlayer.stop();
        mediaPlayer.reset();
        setResource(event.getSource());
        mediaPlayer.start();
    }

}
