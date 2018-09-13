package com.ucast.jnidiaoyongdemo.Serial;


import com.ucast.jnidiaoyongdemo.Model.Config;
import com.ucast.jnidiaoyongdemo.globalMapObj.MermoyPrinterSerial;
import com.ucast.jnidiaoyongdemo.mytime.MyTimeTask;
import com.ucast.jnidiaoyongdemo.mytime.MyTimer;
import com.ucast.jnidiaoyongdemo.protocol_ucast.MsCardProtocol;
import com.ucast.jnidiaoyongdemo.protocol_ucast.PrinterProtocol;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;

/**
 * Created by pj on 2016/6/6.
 */
public class PrinterSerialRestart {

    private static MyTimer timer;

    private static boolean restart;
    public static boolean re_send = false;

    public static void StartTimer() {
        timer = new MyTimer(new MyTimeTask(new Runnable() {
            public void run() {
                synchronized (PrinterSerialRestart.class) {
                    try {
                        if (!restart)
                            return;
                        ExceptionApplication.gLogger.error("Printer serial error close!  We willl resart it.....");
                        MermoyPrinterSerial.Remove(Config.PrinterSerialName);
                        OpenPrint print = new OpenPrint(Config.PrinterSerial);
                        boolean isOpen = print.Open();
                        if (isOpen){
//                            print.Send(PrinterProtocol.getPrinterSwitchOnProtocol());
//                            print.Send(MsCardProtocol.getOpenMsCardProtocol());
                            MermoyPrinterSerial.Add(print);
                            restart = false;
                        }
                    } catch (Exception e) {
                        restart = true;
                    }
                }
            }
        }), 2000L, 4000L);
        timer.initMyTimer().startMyTimer();
    }

    public static void Check() {
        synchronized (PrinterSerialRestart.class) {
            restart = true;
            re_send = true;
        }
    }
}
