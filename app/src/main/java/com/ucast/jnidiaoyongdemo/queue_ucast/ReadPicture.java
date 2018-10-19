package com.ucast.jnidiaoyongdemo.queue_ucast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;


import com.ucast.jnidiaoyongdemo.Model.BitmapWithOtherMsg;
import com.ucast.jnidiaoyongdemo.Model.Common;
import com.ucast.jnidiaoyongdemo.Model.PictureModel;
import com.ucast.jnidiaoyongdemo.bmpTools.EpsonPicture;
import com.ucast.jnidiaoyongdemo.bmpTools.SomeBitMapHandleWay;
import com.ucast.jnidiaoyongdemo.tools.ArrayQueue;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;
import com.ucast.jnidiaoyongdemo.tools.SavePasswd;


/**
 * Created by Administrator on 2016/2/16.
 */
public class ReadPicture {

    private boolean _mDispose;
    public static int MIN_PACKAGE_HEGHT = 251;

    private int ONE_PAKGE_COUNT = (MIN_PACKAGE_HEGHT - 1) * (SomeBitMapHandleWay.PRINT_WIDTH / 8);

    private ArrayQueue<BitmapWithOtherMsg> _mQueues = new ArrayQueue<BitmapWithOtherMsg>(0x400);

    // Methods
    public ReadPicture() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                WRun();
            }
        }).start();
    }

    /// <summary>
    /// 添加列队
    /// </summary>
    /// <param name="iObj"></param>
    public void Add(BitmapWithOtherMsg iObj) {
        synchronized (ReadPicture.class) {
            //获取设置中的是否开启打印
            String isOpenPrint = SavePasswd.getInstace().readxml(SavePasswd.ISOPENPRINT,SavePasswd.OPENPRINT);
//            ExceptionApplication.gLogger.info("是否开启打印 --->" + isOpenPrint + "<----");
            boolean isOPen = isOpenPrint.equals(SavePasswd.CLOSEPRINT) ? false : true;
            if (isOPen) {
                _mQueues.enqueue(iObj);
            }
        }
    }

    /// <summary>
    /// 释放线程
    /// </summary>
    public void Dispose() {
        if (!_mDispose) {
            _mDispose = true;
        }
    }

    private BitmapWithOtherMsg GetItem() {
        synchronized (ReadPicture.class) {
            if (_mQueues.size() > 0) {
                return _mQueues.dequeue();
            }
            return null;
        }
    }


    private void OnRun() {
        BitmapWithOtherMsg item = GetItem();
        try {
            if (item != null) {
                String pathName = item.getPath();
                Bitmap bitmap = item.getBitmap();
                byte[] pictureByte = null;
                PictureModel info = null;
                if (pathName != null) {
                    //转换byte数组
                    pictureByte = getBmpByteFromBMPPath(pathName);
                    //解析获得了一张图片的完整信息PictureModel
                    if(pictureByte != null) {
                        info = WholeBytes(pictureByte);
                    }else{
                        info = new PictureModel();
                    }
                    info.setPath(pathName);
                }else if(bitmap != null){
                    pictureByte = EpsonPicture.turnBytes(bitmap);
                    //解析获得了一张图片的完整信息PictureModel
                    info = WholeBytes(pictureByte);
                    info.setPath(pathName);
                    if (bitmap != null && !bitmap.isRecycled()) {
                        bitmap.recycle();
                        bitmap = null;
                    }
                }else{
                    info = new PictureModel();
                }
                if(item.getChannel() != null)
                    info.setChannel(item.getChannel());
                info.setCutPapper(item.isCutPapper());
                //接收完数据数据后 开始加入到打印队列里并发送
                ListPictureQueue.Add(info);
            } else {
                Thread.sleep(3);
            }
        } catch (Exception e) {

        }
    }

    private void WRun() {
        while (!_mDispose) {
            OnRun();
        }
    }

    private byte[] HeadBytes(int total) {
        byte[] btHead = new byte[11];
        btHead[0] = 0x02;
        btHead[1] = 0x50;
        btHead[2] = 0x43;
        btHead[3] = 0x31;
        btHead[4] = 0x01;
        //数据长度
        btHead[4 + 1] = 0x02;
        btHead[5 + 1] = 0x00;
        //总包数
        btHead[6 + 1] = 0x01;
        btHead[7 + 1] = 0x00;
        //当前包数
        btHead[8 + 1] = 0x30;
        btHead[9 + 1] = 0x00;

        btHead[6 + 1] = (byte) (total & 0Xff);
        btHead[7 + 1] = (byte) ((total & 0Xff00) >> 8);
        return btHead;

    }
    private byte[] headBytes(int total) {
        byte[] btHead = new byte[10];
        btHead[0] = 0x42;//B
        btHead[1] = 0x4d;//M
        btHead[2] = 0x41;//A
        btHead[3] = 0x50;//P
        //总包数
        btHead[4] = 0x02;
        btHead[5] = 0x00;
        //当前包数
        btHead[6] = 0x01;
        btHead[7] = 0x00;
        //数据长度
        btHead[8] = 0x30;
        btHead[9] = 0x00;
        btHead[4] = (byte) (total & 0Xff);
        btHead[5] = (byte) ((total & 0Xff00) >> 8);
        return btHead;
    }


    private int PackageTotal(int data_size) {
        return data_size % ONE_PAKGE_COUNT == 0 ? data_size / ONE_PAKGE_COUNT : (data_size / ONE_PAKGE_COUNT) + 1;
    }

    private PictureModel WholeBytes(byte[] btData) {

        int package_total = PackageTotal(btData.length); //获取总包数


        byte[] btHead = headBytes(package_total);//包头
        PictureModel model = new PictureModel();
        int sum = btData.length % ONE_PAKGE_COUNT;
        byte[] sum_L_H = new byte[2];
        sum_L_H[0] = (byte) ((sum) & 0Xff);
        sum_L_H[1] = (byte) (((sum) & 0Xff00) >> 8);
        int t = 0;
        for (t = 0; t < btData.length / ONE_PAKGE_COUNT; t++) {
            byte[] content_senf = join(btHead, content_send_data(btData, t * ONE_PAKGE_COUNT, ONE_PAKGE_COUNT));
            content_senf[6] = (byte) ((t + 1) & 0Xff);
            content_senf[7] = (byte) (((t + 1) & 0Xff00) >> 8);
            content_senf[8] = (byte) ((ONE_PAKGE_COUNT ) & 0Xff);
            content_senf[9] = (byte) (((ONE_PAKGE_COUNT ) & 0Xff00) >> 8);

            model.BufferPicture.add(content_senf);
        }
        if (btData.length % ONE_PAKGE_COUNT != 0) {
            byte[] content_senf = join(btHead, content_send_data(btData, t * ONE_PAKGE_COUNT, sum));
            content_senf[6] = (byte) ((t + 1) & 0Xff);
            content_senf[7] = (byte) (((t + 1) & 0Xff00) >> 8);
            content_senf[8] = sum_L_H[0];
            content_senf[9] = sum_L_H[1];

            model.BufferPicture.add(content_senf);
        }


        model.setOutTime(SystemClock.elapsedRealtime());
        model.setTotal(package_total);
        return model;
    }

    private byte[] join(byte[] a1, byte[] a2) {
        byte[] result = new byte[a1.length + a2.length];
        System.arraycopy(a1, 0, result, 0, a1.length);
        System.arraycopy(a2, 0, result, a1.length, a2.length);
        return result;
    }

    // 图片分包
    private byte[] content_send_data(byte[] by, int start, int size) {
        byte[] send = new byte[size];
        System.arraycopy(by, start, send, 0, size);
        return send;
    }



    //图片base64之后包装
    private String getPackageString(String cmd, String type, String number, int total, int serial, int len, String data) {
        return "@" + cmd + "," + type + "," + number + "," + total + "," + serial + "," + len + "," + data + "$";
    }

    private byte[] TurnBytes(Bitmap bitmap) {
        int W = bitmap.getWidth();
        int H = bitmap.getHeight();

        byte[] bt = new byte[W / 8 * H];
        int idx = 0;
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < W; j = j + 8) {
                byte value = 0;
                for (int s = 0; s <= 7; s++) {
                    int a = bitmap.getPixel(j + s, i);
                    int aa = a & 0xff;
                    if (aa != 255) {
                        value |= 1 << s;
                    }
                }
                bt[idx] = value;
                idx++;
            }
        }
        return bt;
    }

    private byte[] getBmpByteFromBMPPath(String path) {
        String is_58 = SavePasswd.getInstace().getIp(SavePasswd.IS58PAPPER,"false");
        if (is_58.equals("true")){
            return EpsonPicture.turnBytes(BitmapFactory.decodeFile(path));
        }
        return getBmpByteFromBMPFile(path);
    }



    private byte[] getBmpByteFromBMPFile(String path){

        byte [] allFileData = EpsonPicture.getByteArrayFromFile(path);

        if (allFileData == null){
            return null;
        }

        int  w =  ((allFileData[18] << 0 ) & 0xFF)
                + ((allFileData[19] << 8 ) & 0xFF00)
                + ((allFileData[20] << 16) & 0xFF0000)
                + ((allFileData[21] << 24) & 0xFF000000);
        int  h =  ((allFileData[22] << 0 ) & 0xFF)
                + ((allFileData[23] << 8 ) & 0xFF00)
                + ((allFileData[24] << 16) & 0xFF0000)
                + ((allFileData[25] << 24) & 0xFF000000);

        int bitCount =  ((allFileData[28] << 0 ) & 0xFF)
                      + ((allFileData[29] << 8 ) & 0xFF00);
        //不是1位图数据
        if (bitCount != 1) {
            ExceptionApplication.gLogger.error("Bitmap is not 1 bit ! Paser bitCount = " + bitCount);
            return EpsonPicture.turnBytes(BitmapFactory.decodeFile(path));
        }
        int bmpLen = allFileData.length - 62;
        w = w / 8;
        if( bmpLen != w * h){
            ExceptionApplication.gLogger.error("Bitmap File data length is wrong");
            return EpsonPicture.turnBytes(BitmapFactory.decodeFile(path));
        }
        long oldTime = System.currentTimeMillis();
        byte[] bmpData = new byte[bmpLen];//1位图bmp的所有数据
        System.arraycopy(allFileData, 62, bmpData, 0, bmpLen);
        int print_width = SomeBitMapHandleWay.PRINT_WIDTH / 8;//设置打印机的打印宽度
        byte[] bt = new byte[print_width * h];//打印机的打印数据
        int copy_width = print_width < w ? print_width : w;//取数据选择宽度小的 防止数组越位
        for (int i = 0; i < h ; i ++) {
            for (int j = 0; j < copy_width ; j++) {
                bt[i * print_width + j] = EpsonPicture.fanWei(bmpData[bmpLen - (i + 1) * w + j]);
            }
        }
//        ExceptionApplication.gLogger.error("获取一张图片的数据的时间为："+ (System.currentTimeMillis() - oldTime) + "ms\n");
        return bt;
    }



}
