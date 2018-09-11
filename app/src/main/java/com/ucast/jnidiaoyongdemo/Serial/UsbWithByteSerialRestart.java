package com.ucast.jnidiaoyongdemo.Serial;


import com.ucast.jnidiaoyongdemo.Model.Config;
import com.ucast.jnidiaoyongdemo.globalMapObj.MermoyUsbWithByteSerial;
import com.ucast.jnidiaoyongdemo.mytime.MyTimeTask;
import com.ucast.jnidiaoyongdemo.mytime.MyTimer;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;

/**
 * Created by pj on 2016/6/6.
 */
public class UsbWithByteSerialRestart {

    private static MyTimer timer;

    private static boolean restart;

    public static void StartTimer() {
        timer = new MyTimer(new MyTimeTask(new Runnable() {
            public void run() {
                synchronized (UsbWithByteSerialRestart.class) {
                    try {
                        if (!restart)
                            return;
                        ExceptionApplication.gLogger.error("Usb serial error close!  We willl resart it.....");
                        UsbWithByteSerial oldUsbSerial = MermoyUsbWithByteSerial.GetChannel(Config.UsbWithByteSerialName);
                        UsbWithByteSerial padSerialPort = new UsbWithByteSerial(Config.UsbSerial);
                        boolean isOpen = padSerialPort.Open();
                        MermoyUsbWithByteSerial.Remove(Config.UsbWithByteSerialName);
                        MermoyUsbWithByteSerial.Add(padSerialPort);
                        restart = false;
                    } catch (Exception e) {
                        restart = true;
                    }
                }
            }
        }), 2000L, 4000L);
        timer.initMyTimer().startMyTimer();
    }

    public static void Check() {
        synchronized (UsbWithByteSerialRestart.class) {
            restart = true;
        }
    }
}
