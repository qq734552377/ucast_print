package com.ucast.jnidiaoyongdemo.tools;

import android.text.TextUtils;
import android.util.Log;

import com.ucast.jnidiaoyongdemo.Serial.UsbWithByteSerial;

import java.lang.reflect.Method;

/**
 * Created by pj on 2018/9/25.
 */
public class SystemUtils {
    private static final String TAG = "SystemUtils";
    public static String SYSUSBKEY = "sys.usb.config";
    public static String getSystemPropertyForJava(String key,String defVal){
        try {
            Class clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, key);
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.d(TAG, "Unable to read system properties");
        }
        return defVal;

    }
    public static void setSystemPropertyForJava(String key, String value) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, value );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String USBPROPERITY = "printer,acm,hid,adb";

    public static void setSysUsbToNone(){
        SystemUtils.setSystemPropertyForJava(SYSUSBKEY,"none");
        USBPROPERITY = SystemUtils.getSystemPropertyForJava(SYSUSBKEY,"printer,acm,hid,adb");
    }

    public static void setSysUsbToUSb(){
        SystemUtils.setSystemPropertyForJava(SYSUSBKEY,"printer,acm,hid,adb");
    }

}
