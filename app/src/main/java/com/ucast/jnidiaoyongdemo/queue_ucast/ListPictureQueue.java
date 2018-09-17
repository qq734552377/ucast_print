package com.ucast.jnidiaoyongdemo.queue_ucast;

import android.os.SystemClock;

import com.ucast.jnidiaoyongdemo.R;
import com.ucast.jnidiaoyongdemo.Serial.PrinterSerialRestart;
import com.ucast.jnidiaoyongdemo.bmpTools.EpsonParseDemo;
import com.ucast.jnidiaoyongdemo.bmpTools.EpsonPicture;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.xutilEvents.MediapalyEvent;
import com.ucast.jnidiaoyongdemo.xutilEvents.MoneyBoxEvent;
import com.ucast.jnidiaoyongdemo.Model.PictureModel;
import com.ucast.jnidiaoyongdemo.Model.SendPackage;
import com.ucast.jnidiaoyongdemo.mytime.MyTimeTask;
import com.ucast.jnidiaoyongdemo.mytime.MyTimer;
import com.ucast.jnidiaoyongdemo.protocol_ucast.PrinterProtocol;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;


import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ListPictureQueue {

    private static List<PictureModel> list = new ArrayList();

    private static MyTimer timer;



    public static void StartTimer() {
        timer = new MyTimer(new MyTimeTask(new Runnable() {
            public void run() {
                synchronized (ListPictureQueue.class) {

                    if (list.size() <= 0)
                        return;
                    PictureModel info = list.get(0);
                    long time = SystemClock.elapsedRealtime() - info.getOutTime();
                    if (time < 60000)
                        return;
                    SendAgain(false);
                }
            }

        }), 2000L, 800L);
        timer.initMyTimer().startMyTimer();
    }

    public static void Remove() {
        if (list.size() <= 0) {
            return;
        }
        list.remove(0);
    }
    public static void safeRemove() {
        synchronized (ListPictureQueue.class) {
            if (list.size() <= 0) {
                return;
            }
            if (PrinterSerialRestart.re_send){
                SendAgain(true);
                return;
            }
            PictureModel info = list.get(0);
            int baoSize = info.BufferPicture.size();
            int curIndex = info.getCurtNum();
            if (baoSize == curIndex ) {
                list.remove(0);
                SendAgain(false);
            }else{
                SendAgain(false);
            }
        }
    }

    public static void Clean() {
        synchronized (ListPictureQueue.class) {
            if (list.size() <= 0)
                return;
            list.remove(0);
        }
    }

    public static void EndTime() {
        if (timer == null)
            return;
        timer.stopMyTimer();
    }

    public static void Add(PictureModel model) {
        synchronized (ListPictureQueue.class) {
            if (list.size() <= 0) {
                list.add(model);
                SendFirst();
            }else {
                list.add(model);
            }
        }
    }


    public static void SendFirst() {
            if (list.size() <= 0)
                return;
            PictureModel info = list.get(0);
            if (info == null ) {
                return;
            }
            if(info.isCutPapper() && info.BufferPicture.size() <= 0){
                SendPackage.sendToPrinter(PrinterProtocol.getPrinterCutPaperProtocol());
                ExceptionApplication.gLogger.info("info cutPapper is true --- > " );
                return;
            }
            byte[] str = info.BufferPicture.get(0);
            info.setOutTime(SystemClock.elapsedRealtime());
            info.setCurtNum(1);
            SendPackage.sendToPrinter(str);
            EventBus.getDefault().post(new MoneyBoxEvent(true));
    }

    public static void SendAgain(boolean isSendAgain) {
        if (list.size() <= 0)
            return;
        PictureModel info = list.get(0);
        if (info == null) {
            return;
        }
        if(info.isCutPapper() && info.BufferPicture.size() <= 0){
            SendPackage.sendToPrinter(PrinterProtocol.getPrinterCutPaperProtocol());
            ExceptionApplication.gLogger.info("info cutPapper is true --- > " );
            return;
        }
        int curIndex = 0;
        byte[] str = info.BufferPicture.get(curIndex);
        info.setOutTime(SystemClock.elapsedRealtime());
        if (!isSendAgain)
            info.setCurtNum(1);
        SendPackage.sendToPrinter(str);
        if(isSendAgain) {
            PrinterSerialRestart.re_send = false;
            ExceptionApplication.gLogger.info(" send same Picture: " + curIndex + " package");
        }else {
//            ExceptionApplication.gLogger.info(" send next Picture first package");
        }
    }

    public static void SendByIndex(int index) {
        synchronized (ListPictureQueue.class) {
            ResultSend(index);
        }
    }

    public static void ResultSend(int index) {
        if (list.size() <= 0)
            return;
        try {
            PictureModel info = list.get(0);
            if(info.isCutPapper() && info.BufferPicture.size() <= 0){
                SendPackage.sendToPrinter(PrinterProtocol.getPrinterCutPaperProtocol());
                ExceptionApplication.gLogger.info("info cutPapper is true --- > " );
                return;
            }
            int baoSize = info.BufferPicture.size();
            //如果index为0 已经打印完一包了 发送队列中的下一包数据
            if (index == 0){
                if (list.size() <= 0)
                    return;
                if (info.isCutPapper()) {
                    SendPackage.sendToPrinter(PrinterProtocol.getPrinterCutPaperProtocol());
                    return;
                }
                list.remove(0);
                SendAgain(false);
                return;
            }

            //发送打印第1包 包序号号为0    返回  index=2 准备打印第2包 包序号为1
            if (index > 0 && baoSize >= index) {
                byte[] buffer = info.BufferPicture.get(index - 1);
                //设置打印的当前包序号
                info.setCurtNum(index);
                info.setOutTime(SystemClock.elapsedRealtime());
                SendPackage.sendToPrinter(buffer);
//                MyTools.writeToFile(EpsonPicture.TEMPBITPATH + File.separator + "templog.txt",System.currentTimeMillis() + " 发送第" + index + "包完成");
            }

            if (baoSize < index | index < 0) {
                return;
            }
        } catch (Exception e) {
        }
    }

}
