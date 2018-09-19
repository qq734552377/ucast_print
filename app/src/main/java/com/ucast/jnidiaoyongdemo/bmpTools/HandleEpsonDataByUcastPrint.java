package com.ucast.jnidiaoyongdemo.bmpTools;

import android.graphics.Bitmap;

import com.ucast.jnidiaoyongdemo.Model.BitmapWithOtherMsg;
import com.ucast.jnidiaoyongdemo.Model.ReadPictureManage;
import com.ucast.jnidiaoyongdemo.queue_ucast.ReadPicture;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.tools.YinlianHttpRequestUrl;
import com.ucast.jnidiaoyongdemo.xutilEvents.TishiMsgEvent;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.List;

/**
 * Created by pj on 2018/4/23.
 */
public class HandleEpsonDataByUcastPrint {

    public static void serialString(byte[] string,boolean isUpload) {
//        MyTools.writeToFile(EpsonPicture.TEMPBITPATH + File.separator + "templog.txt",System.currentTimeMillis() + " 开始解析数据");

        try {
            if(isContainByteArr(string, EpsonParseDemo.STARTEPSONBYTE)){
//                if (isContainOpenMoneyBox(string,EpsonParseDemo.OPENMONEYBOX)){
//                    MyTools.openMoneyBox();
//                }
                List<String> paths = EpsonParseDemo.parseEpsonBitData(string);
//                MyTools.writeToFile(EpsonPicture.TEMPBITPATH + File.separator + "templog.txt",System.currentTimeMillis() + " 解析完成为多张图片");
                String p = SomeBitMapHandleWay.compoundOneBitPic(paths);
//                MyTools.writeToFile(EpsonPicture.TEMPBITPATH + File.separator + "templog.txt",System.currentTimeMillis() + " 拼接完多张图片");
                // todo ==>>2018.8.28 只能打一张  拼接完在打
//                ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(p,true));
                if (isUpload) {
                    MyTools.uploadFileByQueue(p);
                }
                return;
            }
            printOne(string,isUpload);
        } catch (Exception e) {
            ExceptionApplication.gLogger.info("paser bitmap error ");
            e.printStackTrace();
        }
    }

    public static String printOne(byte[] data,boolean isUpload){
        String path = "" ;
        List<byte[]> byteList = EpsonParseDemo.getEpsonFromByteArr(data);
        try {
            List<PrintAndDatas> printdatas = EpsonParseDemo.parseEpsonByteList(byteList);

            List<PrintAndDatas> goodPrintdatas = EpsonParseDemo.makeListIWant(printdatas);

            List<Bitmap> bmps = EpsonParseDemo.parseEpsonBitDataAndStringReturnBitmap(goodPrintdatas);

            path = SomeBitMapHandleWay.compoundOneBitPicWithBimaps(bmps);
            //取出文字
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < goodPrintdatas.size(); i++) {
                PrintAndDatas one = goodPrintdatas.get(i);
                if (!one.isBit){
                    sb.append(one.getDatas());
                }
            }
            if (path != null && !path.equals("") && isUpload){
                MyTools.uploadDataAndFileWithURLByQueue(sb.toString(),path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return path;
        }

    }
    private static boolean isContainByteArr(byte[] src,byte[] item){
        if (item.length < 3){
            return false;
        }
        boolean isContain = false;
        for (int i = 0; i < src.length; i++) {
            if (src[i] == item[2] && i > 1){
                if(src[i-1] == item[1] && src[i-2] == item[0]){
                    isContain = true;
                    return isContain;
                }
            }
        }
        return isContain;
    }

    public static boolean isContainOpenMoneyBox(byte[] src,byte[] item){
        boolean isContain = false;
        int len = src.length;//> 200 ? 200 : src.length;
        for (int i = 0; i < len  ; i++) {
            if (src[i] == item[1] && i > 0){
                if (src[i-1] == item[0]){
                    isContain = true;
                    return isContain;
                }
            }
        }
        return isContain;
    }

}
