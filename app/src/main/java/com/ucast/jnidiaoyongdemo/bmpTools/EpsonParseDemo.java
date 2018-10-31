package com.ucast.jnidiaoyongdemo.bmpTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import com.ucast.jnidiaoyongdemo.Model.BitmapWithOtherMsg;
import com.ucast.jnidiaoyongdemo.Model.ReadPictureManage;
import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;
import com.ucast.jnidiaoyongdemo.tools.SavePasswd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/1/8.
 */

public class EpsonParseDemo {

    /* 产生钱箱驱动脉冲 */
    public static final byte[] MONEY_BOX = new byte[]{0x1B, 0x70, 0x00, 0x45, 0x45};


    /*设置字号*/
    public static final byte[] FONT_SIZE = new byte[]{0x1B, 0x21};
    /*设置字号   1倍*/
    public static final byte[] FONT_SIZE_1 = new byte[]{0x1D, 0x21};
    /*设置字号   2倍*/
    public static final byte[] FONT_SIZE_2 = new byte[]{0x1D, 0x21, 0x01};

    /*设置着重操作*/
    public static final byte[] FONT_BOLD = new byte[]{0x1B, 0x45};
    /*位图打印操作*/
    public static final byte[] BIT_PRINT_START = new byte[]{0x1D, 0x76, 0x30};
    /*设置着重*/
    public static final byte[] FONT_BOLD_YES = new byte[]{0x1B, 0x45, 0x01};
    /*取消着重*/
    public static final byte[] FONT_BOLD_NO = new byte[]{0x1B, 0x45, 0x00};
    /*对齐方式   左对齐 0x00/0x30 居中 0x01/0x31 右对齐 0x02/0x32*/
    public static final byte[] FONT_JUSTIFICATION = new byte[]{0x1B, 0x61};

    public static final String startEpsonStr = "1D 38 4C";
    public static final byte[] STARTEPSONBYTE = {0x1D,0x38,0x4C};
    public static final byte[] OPENMONEYBOX = {0x1B,0x70};
    public static final byte[] OPENMONEYBOX_2 = {0x1B,0x3D};
    public static final String endEpsonStr = "1D 28 4C";
    public static final byte[] ENDEPSONBYTE = {0x1D,0x28,0x4C};



    /**
     *  将指定byte数组以16进制的形式返回
     * */
    public static String printHexString(byte[] b) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            if(b[i] == 0x00){
                r.append("00 ");
                continue;
            }else if(b[i] == 0xff){
                r.append("FF ");
                continue;
            }
            String hex = Integer.toHexString(b[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            r.append(hex.toUpperCase() + " ");
        }

        return r.toString();

    }
    /**
     *  将element 数据添加到数组的尾部  返回新的数组
     * */
    public static byte[] addAByte(byte element, byte[] res) {
        if (res == null) {
            byte[] newBy = new byte[1];
            newBy[0] = element;
            return newBy;
        }
        byte[] newBy = new byte[res.length + 1];
        System.arraycopy(res, 0, newBy, 0, res.length);
        newBy[res.length] = element;

        return newBy;

    }


    public static List<String> getEpsonFromStringArr(String[] bytes) {
        List<String> epsonListString = new ArrayList<>();
        int indexStr = -1;
        System.out.println(epsonListString.toString());
        for (int i = 0; i < bytes.length; i++) {
            if (bytes[i].equals("1B") || bytes[i].equals("1C") || bytes[i].equals("1D")) {
                indexStr++;
                String a = new String(bytes[i]);
                epsonListString.add(a);
            } else {
                if (indexStr == -1) {
                    continue;
                }
                String res = (String) epsonListString.get(indexStr);
                res += " " + bytes[i];
                epsonListString.set(indexStr, res);
            }
        }

//          System.out.println(epsonListString.toString());
        for (int i = 0; i < epsonListString.size(); i++) {
            System.out.println(epsonListString.get(i));
        }

        return epsonListString;
    }
    /**
     *  将数据分割成1B 1C 1D 开头
     *
     *  剔除了1B 70 n l m
     *  剔除了1B 2A n nL nM
     *
     * */
    public static List<byte[]> getEpsonFromByteArr(byte[] datas) {
        List<byte[]> epsonList = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < datas.length;) {
            if(datas[i] == 0x1B || datas[i] ==0x1C || datas[i] == 0x1D ) {
                if(datas[i] == 0x1B && i + 1 < datas.length && datas[i + 1] == 0x2A){
                    index++;
                    //第一次解析1B 2A 的位图协议
                    int mode = datas[i + 2] & 0xFF;
                    int number = (datas[i + 3] & 0xFF) + (datas[i + 4] << 8 & 0xff00);
                    if (mode == 32 || mode == 33){
                        number *= 3;
                    }
                    int bitArrLen = number + 5;
                    byte[] headBy = new byte[bitArrLen];
                    System.arraycopy(datas, i, headBy, 0, bitArrLen);
                    epsonList.add(headBy);

                    i = i + bitArrLen;
                }else if(datas[i] == 0x1D && i + 1 < datas.length && datas[i + 1] == 0x76) {
                    index++;
                    int width = (datas[i + 4] & 0xFF) + (datas[i + 5] << 8 & 0xff00);
                    int height = (datas[i + 6] & 0xFF) + (datas[i + 7] << 8 & 0xff00);

                    int bitArrLen = width * height + 8;

                    byte[] headBy = new byte[bitArrLen];
                    System.arraycopy(datas, i, headBy, 0, bitArrLen);
                    epsonList.add(headBy);

                    i = i + bitArrLen;
                }else if(datas[i] == 0x1B && i + 1 < datas.length && datas[i + 1] == 0x70){
                    //打开钱箱指令
//                    MyTools.openMoneyBox();
                    i = i + 5;
                }else {
                    index++;
                    byte[] headBy = addAByte(datas[i], null);
                    epsonList.add(headBy);
                    i++;
                }
            }else {
                if(index == -1) {
                    i++;
                    continue;
                }
                byte[] res = (byte [])epsonList.get(index);
                epsonList.set(index ,addAByte(datas[i], res));
                i++;
            }
        }
        return epsonList;
    }


    /**
     *判断数组是否相等
     * */
    public static boolean isArrEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        } else {
            for (int i = 0; i < a.length; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }
    /**
     *判断数组是否相等
     * */
    public static boolean isArr2HeadEqual(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        } else {
            for (int i = 0; i < 2; i++) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     *  将Epson协议中夹杂的打印文字给找出来
     * */
    public static List<PrintAndDatas> parseEpsonByteList(List<byte[]> lists) throws UnsupportedEncodingException {
        List<PrintAndDatas> printLists = new ArrayList<>();
        PrintAndDatas one_data = null;
        for (int i = 0; i < lists.size(); i++) {
            byte[] b = lists.get(i);
            if (one_data == null) {
                one_data = new PrintAndDatas();
            }
            switch (b.length) {
                case 2:
                    //目前可以忽略
                    continue;
                case 3:
                    setPrintAndDataWithEpson(one_data, b);
                    break;
                default:
                    if (b.length > 3) {
                        int position = 0;

                        boolean isPasered = firstPaser(b,one_data,printLists);
                        if (isPasered){
                            one_data = null;
                        }else{
                            for (int j = 0; j < b.length; j++) {
                                if (b[j] == 0x21){
                                    position = j + 2;
                                    setDataToList(position,b,one_data,printLists);
                                    one_data = null;
                                    break;
                                }
                                if (b[j] == 0x00 || b[j] == 0x01 || b[j] == 0x11) {
                                    position = j + 1;
                                    setDataToList(position,b,one_data,printLists);
                                    one_data = null;
                                    break;
                                }

                                if(j > 3){
                                    position = j;
                                    setDataToList(position,b,one_data,printLists);
                                    one_data = null;
                                    break;
                                }
                                if (b[j] == 0x0A || b[j] == 0x20 || b[j] == 0x2D) {
                                    position = j;
                                    setDataToList(position,b,one_data,printLists);
                                    one_data = null;
                                    break;
                                }

                            }
                        }


                    }
                    break;
            }
        }
        return printLists;
    }

    private static boolean firstPaser(byte[] b,PrintAndDatas one_data,List<PrintAndDatas> printLists) {
        boolean isPasered = false;
        int position = 0;
        switch (b[0]){
            case 0x1B :
                byte b_1B_1 = b[1];
                if (    b_1B_1 == 0x20 ||
                        b_1B_1 == 0x21 ||
                        b_1B_1 == 0x25 ||
                        b_1B_1 == 0x2D ||
                        b_1B_1 == 0x33 ||
                        b_1B_1 == 0x3D ||
                        b_1B_1 == 0x3F ||
                        b_1B_1 == 0x44 ||
                        b_1B_1 == 0x45 ||
                        b_1B_1 == 0x47 ||
                        b_1B_1 == 0x4A ||
                        b_1B_1 == 0x4B ||
                        b_1B_1 == 0x52 ||
                        b_1B_1 == 0x55 ||
                        b_1B_1 == 0x61 ||
                        b_1B_1 == 0x72 ||
                        b_1B_1 == 0x7B ||
                        b_1B_1 == 0x75   ){
                    position = 3;
                    setDataToList(position,b,one_data,printLists);
                    isPasered = true;
                }
                if(     b_1B_1 == 0x26 ||
                        b_1B_1 == 0x32 ||
                        b_1B_1 == 0x3C ||
                        b_1B_1 == 0x70   ){
                    position = 2;
                    setDataToList(position,b,one_data,printLists);
                    isPasered = true;
                }
                if (b_1B_1 == 0x2A){
                    position = 5;
                    set1B_2A_DataToList(position,b,one_data,printLists);
                    isPasered = true;
                }

                break;
            case 0x1C :
                    byte b_1C_1 = b[1];
                    if(     b_1C_1 == 0x26 ||
                            b_1C_1 == 0x32   ) {
                        position = 2;
                        setDataToList(position, b, one_data, printLists);
                        isPasered = true;
                    }

                if(     b_1C_1 == 0x57  ) {
                    position = 3;
                    setDataToList(position, b, one_data, printLists);
                    isPasered = true;
                }
                break;
            case 0x1D :
                byte b_1D_1 = b[1];
                if( b_1D_1 ==  0x76 ) {
                    position = 8;
                    byte[] datas = new byte[b.length - position];
                    System.arraycopy(b, position, datas, 0, datas.length);

                    int mode = b[3] & 0xFF;
                    int with = (b[4] & 0xFF) + (b[5] << 8 & 0xff00);
                    int height = (b[6] & 0xFF) + (b[7] << 8 & 0xff00);

                    int widthRate = 1;
                    int heightRate = 1;

                    if (mode == 3 || mode == 51) {
                        widthRate = 2;
                        heightRate = 2;
                    } else if (mode == 2 || mode == 50) {
                        widthRate = 1;
                        heightRate = 2;
                    } else if (mode == 1 || mode == 49) {
                        widthRate = 2;
                        heightRate = 1;
                    } else {
                        widthRate = 1;
                        heightRate = 1;
                    }

                    one_data.isBit = true;
                    one_data.bitWidthRate = widthRate;
                    one_data.bitHeightRate = heightRate;
                    one_data.bitWidth = with;
                    one_data.bitHeight = height;
                    one_data.setBitDatasByte(datas);

                    printLists.add(one_data);

                    isPasered = true;
                }

                if(     b_1D_1 == 0x49 ||
                        b_1D_1 == 0x56 ||
                        b_1D_1 == 0x61 ||
                        b_1D_1 == 0x72 ){
                    position = 3;
                    setDataToList(position, b, one_data, printLists);
                    isPasered = true;
                }

                break;
        }
        return isPasered;

    }

    public static void setDataToList(int cutPosition,byte[] b,PrintAndDatas one_data,List<PrintAndDatas> printLists){
        int position = cutPosition;
        byte[] datas = new byte[b.length - position];
        System.arraycopy(b, position, datas, 0, datas.length);

        byte[] lastEpson = new byte[position];
        System.arraycopy(b, 0, lastEpson, 0, position);

        setPrintAndDataWithEpson(one_data, lastEpson);

        try {
            one_data.datas = new String(datas, "GB18030");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (!one_data.datas.equals("") && isRightData(datas)) {
            printLists.add(one_data);
        }
    }
    public static void set1B_2A_DataToList(int cutPosition,byte[] b,PrintAndDatas one_data,List<PrintAndDatas> printLists){
        int position = cutPosition;
        int endLen = 2;


        byte[] lastEpson = new byte[position];
        System.arraycopy(b, 0, lastEpson, 0, position);
        int mode = lastEpson[2] & 0xFF; //模式
        int dataLen = (lastEpson[3] & 0xFF) + (lastEpson[4] & 0xFF) * 256; //数据长度
        if (mode == 32 || mode == 33) {
            dataLen *= 3;
        }


        byte[] datas = new byte[dataLen];
        System.arraycopy(b, position, datas, 0, datas.length);

        setPrintAndDataWithEpson(one_data, lastEpson);
        if (mode == 0 || mode == 1){
            byte[] bitData = getBitDataBy1B2A(datas);
            one_data.isBit = true;
            one_data.setBitDatasByte(bitData);
            one_data.bitHeight = 8;
            one_data.bitWidth = bitData.length / one_data.bitHeight;
            //1B 2A 位图协议时 保持上一个位图的对齐方式
            if (printLists.size() > 0 && printLists.get(printLists.size() - 1).isBit){
                one_data.setJustification(printLists.get(printLists.size() - 1).getJustification());
            }
            printLists.add(one_data);
        }else if(mode == 32 || mode == 33){
            int one_line_len = datas.length / 3;
            byte[] b_24_1 = new byte[one_line_len];
            byte[] b_24_2 = new byte[one_line_len];
            byte[] b_24_3 = new byte[one_line_len];

            for (int i = 3; i < datas.length + 1 ; i += 3) {
                b_24_1[i/3 - 1] = datas[i - 3];
                b_24_2[i/3 - 1] = datas[i - 2];
                b_24_3[i/3 - 1] = datas[i - 1];
            }
            byte[] bitData_1 = getBitDataBy1B2A(b_24_1);
            byte[] bitData_2 = getBitDataBy1B2A(b_24_2);
            byte[] bitData_3 = getBitDataBy1B2A(b_24_3);
            byte[] bitData = new byte[bitData_1.length + bitData_2.length + bitData_3.length];
            System.arraycopy(bitData_1,0,bitData,0,bitData_1.length);
            System.arraycopy(bitData_2,0,bitData,bitData_1.length,bitData_2.length);
            System.arraycopy(bitData_3,0,bitData,bitData_1.length + bitData_2.length,bitData_3.length);
            one_data.isBit = true;
            one_data.setBitDatasByte(bitData);
            one_data.bitHeight = 24;
            one_data.bitWidth = bitData.length / one_data.bitHeight;
            //1B 2A 位图协议时 保持上一个位图的对齐方式
            if (printLists.size() > 0 && printLists.get(printLists.size() - 1).isBit){
                one_data.setJustification(printLists.get(printLists.size() - 1).getJustification());
            }
            printLists.add(one_data);
        }

    }


    /**
     *  剔除一些不是打印数据的方法
     * */
    public static boolean isRightData(byte[] data) {
        boolean isRight = false;
        if(data.length == 1) {
            if(data[0] == 0x0A || data[0] == 0x0D || (data[0] >= 0x20 && data[0] <= 0x7E)) {
                isRight = true;
            }else {
                isRight =false;
            }
        }else {
            isRight = true;
            if(data.length < 7) {
                for (int i = 0; i < data.length; i++) {
                    if(data[i] == 0x00) {
                        isRight =false;
                        return isRight;
                    }
                }
            }
        }
        return isRight;
    }

    // 将1B 2A 的数据解析为位图数据
    public static byte[] getBitDataBy1B2A(byte[] src){
        int len = (src.length + 7) / 8 * 8;
        byte[] dest = new byte[len];
        int w = len / 8 ;
        int h = 8;
        for (int i = 0; i < src.length; i++) {
            byte oneSrc = src[i];
            for (int j = 7; j >= 0; j--) {
                int src_one_bit_num = (oneSrc >> j & 0x01);//元数据的位值  从高位开始
                int indexDest = (7 - j) * w + i / 8;//对应目标数组中byte的索引
                int dest_bit_index = 7 - i % 8;//目标byte的第几位 从低位开始
                dest[indexDest] |= src_one_bit_num << dest_bit_index;
            }
        }

        return dest;
    }

    /**
     *    将多个PrintAndDatas对象中的文字数据拼接为一个
     *
     *    当PrintAndDatas为位图数据时单独列为一项
     * */
    public static List<PrintAndDatas> makeListIWant(List<PrintAndDatas> lists) {
        List<PrintAndDatas> goodList = new ArrayList<>();

        int goodindex = -1 ;
        for (int i = 0; i < lists.size(); i++) {
            PrintAndDatas one_data = lists.get(i);
            if(!one_data.isBit) {
                if(goodindex == -1 || goodindex >= goodList.size()) {
                    goodList.add(one_data);
                    goodindex ++ ;
                    if (goodList.size() > 0 && goodList.get(goodList.size() - 1).isBit){
                        goodindex --;
                    }
                }else {
                    PrintAndDatas one_good_data = goodList.get(goodList.size() - 1);
                    if (one_good_data.FONT_SIZE_TIMES == one_data.FONT_SIZE_TIMES && one_good_data.getJustification() == one_data.getJustification())
                        one_good_data.addDatas(one_data.getDatas());
                    else {
                        if (one_good_data.getDatas().replace(" ","").equals("")){
                            one_good_data.addDatas(one_data.getDatas());
                            one_good_data.FONT_SIZE_TIMES = one_data.FONT_SIZE_TIMES;
                            one_good_data.bitWidthRate = one_data.bitWidthRate;
                            one_good_data.bitHeightRate = one_data.bitHeightRate;
                        }else {
                            goodList.add(one_data);
                            goodindex++;
                        }
                    }
                    continue;
                }

            }else {
                goodList.add(one_data);
                goodindex += 2 ;
            }
        }
        return goodList;

    }

    public static void setPrintAndDataWithEpson(PrintAndDatas one_data, byte[] b) {
        byte[] b_2 = new byte[]{ b[0], b[1]};
        if (isArr2HeadEqual(b_2, FONT_SIZE)) {
            if (b[2] == 0x00)
                one_data.FONT_SIZE_TIMES = 1;
            else{
                one_data.FONT_SIZE_TIMES = 2;
                int heightRate = b[2] >> 4 & 0x01;
                int widthRate = b[2] >> 5 & 0x01;
                one_data.bitHeightRate = heightRate + 1;
                one_data.bitWidthRate = widthRate + 1;
            }
        } else if (isArr2HeadEqual(b_2, FONT_SIZE_1)){
            if (b[2] == 0x00)
                one_data.FONT_SIZE_TIMES = 1;
            else {
                one_data.FONT_SIZE_TIMES = 2;
                int heightRate = b[2] & 0x01;
                int widthRate = b[2] >> 4 & 0x01;
                one_data.bitHeightRate = heightRate + 1;
                one_data.bitWidthRate = widthRate + 1;
            }
        }else if (isArr2HeadEqual(b_2,FONT_JUSTIFICATION)){
            //解析1B 61 的对齐方式的协议
            byte geshi = b[2];

            if (geshi == 0x01 || geshi == 0x31){//居中
                one_data.setJustification(1);
            }else if (geshi == 0x02 || geshi == 0x32){//右对齐
                one_data.setJustification(2);
            }else {//左对齐是默认方式
                one_data.setJustification(0);
            }
        }else if (isArrEqual(b, FONT_BOLD_NO)) {
            one_data.FONT_SIZE_TYPE = 0;
        } else if (isArrEqual(b, FONT_BOLD_YES)) {
            one_data.FONT_SIZE_TYPE = 1;
        }
    }
    /**
     *    解析  纯位图数据  生成对应的位图图片  返回对应的图片路径集合
     * */
    public static List<String> parseEpsonBitData(String datas) {
        List<String> bmpPaths = new ArrayList<>();
        try {
            int lineNumStart = 39;
            int dataStart = 51;
            String str = datas.trim();
            List<EpsonBitData> bitPicLists = new ArrayList<>();


            while (str.indexOf(startEpsonStr) >= 0) {
                int start = str.indexOf(startEpsonStr);
                int end = str.indexOf(endEpsonStr);
                if (start > end)
                    return null;
                String oneBitData = str.substring(start, end);

                String lineNumstr = oneBitData.substring(lineNumStart, lineNumStart + 6);
                String[] numStrs = lineNumstr.split(" ");
                int high = Integer.parseInt(numStrs[1].substring(0), 16);
                int low = Integer.parseInt(numStrs[0].substring(0), 16);
                int line_num = (high * 256 + low + 7) / 8;
                String dataStr = oneBitData.substring(dataStart);

                if (bitPicLists.size() > 0) {
                    EpsonBitData lastBit = bitPicLists.get(bitPicLists.size() - 1);
                    if (lastBit.getWith() == line_num * 8) {
                        lastBit.addStringDatas(dataStr);
                        str = str.substring(end + 21);
                        continue;
                    }
                }
                EpsonBitData oneBit = new EpsonBitData();
                oneBit.setWith(line_num * 8);
                oneBit.setStringDatas(dataStr);
                bitPicLists.add(oneBit);
                str = str.substring(end + 21);
            }
            for (int i = 0; i < bitPicLists.size(); i++) {
                String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_" + UUID.randomUUID().toString().replace("-", "")+"_" +i + ".bmp";

                EpsonBitData one = bitPicLists.get(i);
                saveAsBitmapWithByteDataUse1Bit(one.getByteFromStringDatas(), one.getWith(), bmpPath);
                bmpPaths.add(bmpPath);
            }
        } catch (Exception e) {
            return null;
        }
        return bmpPaths;
    }

    /**
     *    解析  纯位图数据  生成对应的位图图片  返回对应的图片路径集合
     * */
    public static List<String> parseEpsonBitData(byte[] datas) {
        List<String> bmpPaths = new ArrayList<>();
        try {
            int lineNumStart = 39 / 3;
            int dataStart = 51 / 3;
            List<EpsonBitData> bitPicLists = new ArrayList<>();
            while (isContainByteArr(datas,STARTEPSONBYTE)) {
                int start = getByteArrIndex(datas,STARTEPSONBYTE);
                int end = getByteArrIndex(datas,ENDEPSONBYTE);
                if (start > end)
                    return null;
                int len = end - start;
                byte[] oneBitData = new byte[len];
                System.arraycopy(datas,start,oneBitData,0,oneBitData.length);
                int high = oneBitData[lineNumStart + 1] & 0xFF;
                int low = oneBitData[lineNumStart ]  & 0xFF;
                int line_num = (high * 256 + low + 7) / 8;

                byte[] bitdata = new byte[len - dataStart];
                System.arraycopy(oneBitData,dataStart,bitdata,0,bitdata.length);

                if (bitPicLists.size() > 0) {
                    EpsonBitData lastBit = bitPicLists.get(bitPicLists.size() - 1);
                    if (lastBit.getWith() == line_num * 8) {
                        lastBit.addDatasByte(bitdata);
                        byte[] temp = new byte[datas.length - end -7];
                        System.arraycopy(datas,end+ 7,temp,0,temp.length);
                        datas = new byte[temp.length];
                        System.arraycopy(temp,0,datas,0,temp.length);
                        continue;
                    }
                }
                EpsonBitData oneBit = new EpsonBitData();
                oneBit.setWith(line_num * 8);
                oneBit.setDatasByte(bitdata);
                bitPicLists.add(oneBit);
                byte[] temp = new byte[datas.length - end -7];
                System.arraycopy(datas,end+ 7,temp,0,temp.length);
                datas = new byte[temp.length];
                System.arraycopy(temp,0,datas,0,temp.length);
            }
            for (int i = 0; i < bitPicLists.size(); i++) {
                String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_" + UUID.randomUUID().toString().replace("-", "")+"_" +i + ".bmp";
                EpsonBitData one = bitPicLists.get(i);
                saveAsBitmapWithByteDataUse1Bit(one.getDatasByte(), one.getWith(), bmpPath);
                bmpPaths.add(bmpPath);
                // todo ==>>2018.8.28 只能打一张  拼接完在打
                if (i == bitPicLists.size() -1 ) {
                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(bmpPath,true));
                }else{
                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(bmpPath,false));
                }
            }
        } catch (Exception e) {
            ExceptionApplication.gLogger.info("paser error --> " +e.toString());
            return null;
        }
        return bmpPaths;
    }




    public static boolean isContainByteArr(byte[] src,byte[] item){
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

    public static int getByteArrIndex(byte[] src,byte[] item){
        if (item.length < 3){
            return -1;
        }
        int isContain = -1;
        for (int i = 0; i < src.length; i++) {
            if (src[i] == item[2] && i > 1){
                if(src[i-1] == item[1] && src[i-2] == item[0]){
                    isContain = i - 2;
                    return isContain;
                }
            }
        }
        return isContain;
    }

    /**
     *    解析 文字和位图混合的数据  生成对应的位图图片  返回对应的图片路径
     * */
    public static List<String> parseEpsonBitDataAndString ( List<PrintAndDatas> lists){
        List<String> bmpPaths = new ArrayList<>();

        for (int i = 0; i < lists.size(); i++) {
            PrintAndDatas one_data = lists.get(i);
            if (one_data.isBit){
                String bmpPath = EpsonPicture.ALBUM_PATH + File.separator + "Ucast/" + "ucast_bit_and_string_" + i + ".bmp";
                saveAsBitmapWithByteDataUse1Bit(one_data.bitDatasByte, one_data.bitWidth * 8, bmpPath);
                bmpPaths.add(bmpPath);
            }else{
                String bmpPath = EpsonPicture.ALBUM_PATH + File.separator + "Ucast/" + "ucast_string_" + i + ".bmp";
                bmpPaths.add(EpsonPicture.getBitMapByString(one_data.getDatas(),bmpPath));
            }
        }
        return bmpPaths;
    }
    /**
     *    解析 文字和位图混合的数据  生成对应的位图图片  返回bitmapd的路径集合
     * */
    public static List<String> parseEpsonBitDataAndStringReturnBitmapPaths(List<PrintAndDatas> lists){
//        List<Bitmap> bmps = new ArrayList<>();
        List<String> textPaths = new ArrayList<>();
        List<String> paths = new ArrayList<>();
        int print_width = EpsonPicture.getPrintWidth();
        int allCount = lists.size();
        for (int i = 0; i < allCount;) {
            PrintAndDatas one_data = lists.get(i);
            if (one_data.isBit){
                String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_" +i + ".bmp";
                byte[] bmpData = one_data.bitDatasByte;
                int widthNoRate = one_data.bitWidth;
                int widthWithRate = one_data.getBitWidth();
                if(one_data.bitWidthRate == 2){
                    bmpData =EpsonPicture.getTwiceWidthData(bmpData , widthNoRate);
                }
                if (one_data.bitHeightRate == 2){
                    bmpData =EpsonPicture.getTwiceHeighData(bmpData , widthWithRate);
                }
                if (one_data.getJustification() == 1){ // 居中
                    bmpData = EpsonPicture.getCenterBitData(bmpData,widthWithRate);
                    widthWithRate = bmpData.length / one_data.getBitHeight();
                }else if (one_data.getJustification() == 2){// 右对齐
                    bmpData = EpsonPicture.getRightBitData(bmpData,widthWithRate);
                    widthWithRate = bmpData.length / one_data.getBitHeight();
                }
                saveAsBitmapWithByteDataUse1Bit(bmpData, widthWithRate * 8 , bmpPath);
//                if (i == lists.size() - 1 ) {
//                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(bmpPath,true));
//                }else{
//                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(bmpPath,false));
//                }
                paths.add(bmpPath);
//                bmps.add(BitmapFactory.decodeFile(bmpPath));
                i++;
            }else{
                String one = "";
                int addTimes = 1;
                if (one_data.FONT_SIZE_TIMES == 2){
                    one = EpsonPicture.getBitMapByPrintAndDatasReturnBitmap(one_data);
                    if (one == null || one.equals("")) {
                        i = i + addTimes;
                        continue;
                    }
                    if (i + addTimes < allCount ){//没有越界
                        PrintAndDatas second_data = lists.get(i + addTimes);
                        if (!second_data.isBit ) {//不是位图数据
                            String lastString = one_data.getDatas();
                            String secondString = second_data.getDatas();
                            if (one_data.FONT_SIZE_TIMES == second_data.FONT_SIZE_TIMES && one_data.bitWidthRate == second_data.bitWidthRate && one_data.bitHeightRate == second_data.bitHeightRate) {
                                if (!lastString.substring(lastString.length() - 1, lastString.length()).equals("\n") && !secondString.substring(0, 1).equals("\n")) {
                                    //上一个字符串最后一位不是\n 可能还需判断长度是否超过
                                    one_data.addDatas(second_data.datas);
                                    String newData = one_data.datas;
                                    int byteNum = 0;
                                    try {
                                        byteNum = newData.getBytes("GB18030").length;
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    int width = byteNum * one_data.bitWidthRate * EpsonPicture.FONT_SIZE / 2;
                                    if (width > print_width) {
                                        int space_num = countCharNum(newData, " ");
                                        int other_num = byteNum - space_num;
                                        int print_space_num = (print_width - other_num * one_data.bitWidthRate * EpsonPicture.FONT_SIZE / 2) / (one_data.bitWidthRate * EpsonPicture.FONT_SIZE / 2);
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(newData.substring(0, newData.indexOf(" ")));
                                        for (int j = 0; j < print_space_num; j++) {
                                            sb.append(" ");
                                        }
                                        sb.append(newData.substring(newData.lastIndexOf(" ") + 1, newData.length()));
                                        one_data.datas = sb.toString();
                                    }
                                    one = EpsonPicture.getBitMapByPrintAndDatasReturnBitmap(one_data);
                                    addTimes++;
                                }
                            }
                            if(one_data.FONT_SIZE_TIMES != second_data.FONT_SIZE_TIMES){
                                if (!lastString.substring(lastString.length()-1,lastString.length()).equals("\n") && !secondString.substring(0,1).equals("\n")) {
                                    //上一个字符串最后一位不是\n 可能还需判断长度是否超过
                                    String[] lastStringArr = lastString.split("\n");
                                    int x_offset = 0;
                                    try {
                                        x_offset = lastStringArr[lastStringArr.length -1].getBytes("GB18030").length * EpsonPicture.FONT_SIZE * one_data.bitWidthRate / 2;
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    int enterIndex = secondString.indexOf("\n");
                                    String pingjieStr = "";
                                    if(enterIndex != -1)
                                        pingjieStr = secondString.substring(0,enterIndex);
                                    else
                                        pingjieStr = secondString;
                                    String endStr = secondString.substring(enterIndex + 1,secondString.length());
                                    int second_string_len = 0;
                                    try {
                                        second_string_len = pingjieStr.replace("\n","").getBytes("GB18030").length * EpsonPicture.FONT_SIZE * second_data.bitWidthRate / 2;
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    if (x_offset + second_string_len <= print_width) {
                                        second_data.setDatas(pingjieStr);
                                        one = pingJieAtBottom(one, second_data, x_offset);
                                        if (enterIndex == -1 || enterIndex == secondString.length() -1 ){
                                            addTimes++;
                                        }else {
                                            second_data.setDatas(endStr);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {//FONT_SIZE_TIMES 不为2的
                    one = EpsonPicture.getBitMapByStringReturnBitmaPath(one_data);
                    if (one == null || one.equals("")) {
                        i = i + addTimes;
                        continue;
                    }
                    if (i + addTimes < allCount ){//没有越界
                        PrintAndDatas second_data = lists.get(i + addTimes);
                        if (!second_data.isBit){//不是位图数据
                            if (one_data.FONT_SIZE_TIMES != second_data.FONT_SIZE_TIMES){//字体不同
                                String lastString = one_data.getDatas();
                                String secondString = second_data.getDatas();
                                if (!lastString.substring(lastString.length()-1,lastString.length()).equals("\n") && !secondString.substring(0,1).equals("\n")){
                                    //上一个字符串最后一位不是\n 可能还需判断长度是否超过
                                    String[] lastStringArr = lastString.split("\n");
                                    int x_offset = 0;
                                    try {
                                         x_offset = lastStringArr[lastStringArr.length -1].getBytes("GB18030").length * EpsonPicture.FONT_SIZE * one_data.bitWidthRate / 2;
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    int enterIndex = secondString.indexOf("\n");
                                    String pingjieStr = "";
                                    if(enterIndex != -1)
                                        pingjieStr = secondString.substring(0,enterIndex);
                                    else
                                        pingjieStr = secondString;
                                    String endStr = secondString.substring(enterIndex + 1,secondString.length());
                                    int second_string_len = 0;
                                    try {
                                        second_string_len = pingjieStr.replace("\n","").getBytes("GB18030").length * EpsonPicture.FONT_SIZE * second_data.bitWidthRate / 2;
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                    if (x_offset + second_string_len <= print_width) {
                                        second_data.setDatas(pingjieStr);
                                        one = pingJieSmallAndBigFont(one, second_data, x_offset);
                                        if (enterIndex == -1 || enterIndex == secondString.length() -1 ){
                                            addTimes++;
                                        }else {
                                            second_data.setDatas(endStr);
                                        }
                                        if (i + addTimes < allCount) {//没有越界
                                            PrintAndDatas third_data = lists.get(i + addTimes);
                                            if (!third_data.isBit) {//不是位图数据
                                                if (second_data.FONT_SIZE_TIMES != third_data.FONT_SIZE_TIMES) {//字体不同
                                                    lastString = second_data.getDatas();
                                                    String thirdString = third_data.getDatas();
                                                    if (!lastString.substring(lastString.length() - 1, lastString.length()).equals("\n") && !thirdString.substring(0, 1).equals("\n")) {
                                                        //上一个字符串最后一位不是\n 可能还需判断长度是否超过
                                                        x_offset += second_string_len;
                                                        int third_string_len = 0;
                                                        try {
                                                            third_string_len = thirdString.replace("\n","").getBytes("GB18030").length * EpsonPicture.FONT_SIZE * third_data.bitWidthRate / 2;
                                                        } catch (UnsupportedEncodingException e) {
                                                            e.printStackTrace();
                                                        }
                                                        if (x_offset + third_string_len <= print_width) {
                                                            one = pingJieAtBottom(one, third_data, x_offset);
                                                            addTimes++;
                                                            if (i + addTimes < allCount) {//没有越界
                                                                PrintAndDatas fourth_data = lists.get(i + addTimes);
                                                                if (!third_data.isBit) {//不是位图数据
                                                                    if (third_data.FONT_SIZE_TIMES != fourth_data.FONT_SIZE_TIMES) {//字体不同
                                                                        lastString = third_data.getDatas();
                                                                        String fourthString = fourth_data.getDatas();
                                                                        if (!lastString.substring(lastString.length() - 1, lastString.length()).equals("\n") && !fourthString.substring(0, 1).equals("\n")) {
                                                                            //上一个字符串最后一位不是\n 可能还需判断长度是否超过
                                                                            x_offset += third_string_len;
                                                                            int fourth_string_len = 0;
                                                                            try {
                                                                                fourth_string_len = fourthString.replace("\n","").getBytes("GB18030").length * EpsonPicture.FONT_SIZE * fourth_data.bitWidthRate / 2;
                                                                            } catch (UnsupportedEncodingException e) {
                                                                                e.printStackTrace();
                                                                            }
                                                                            if (x_offset + fourth_string_len <= print_width) {
                                                                                one = pingJieAtBottom(one, fourth_data, x_offset);
                                                                                addTimes++;
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }


//                if (i == allCount - 1 ) {
//                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(one,true));
//                }else{
//                    ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(one,false));
//                }
//                bmps.add(one);
                if (one != null || !one.equals(""))
                    paths.add(one);
//                textPaths.add(one);
//                String textBmp = null;
//                if (i == allCount - 1 ) {//如果是最后一个 直接送给打印机
//                    textBmp = SomeBitMapHandleWay.compoundOneBitPicWithBimapsReturnBitmap(textPaths);
//                    textPaths.clear();
//                    if (textBmp != null ){
//                        ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(textBmp,true));
//                    }
//                }else{
//                    if (i + addTimes < allCount){//没有越界
//                        PrintAndDatas next_data = lists.get(i + addTimes);
//                        if (next_data.isBit){
//                            textBmp = SomeBitMapHandleWay.compoundOneBitPicWithBimapsReturnBitmap(textPaths);
//                            textPaths.clear();
//                            if (textBmp != null ){
//                                ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(textBmp,false));
//                            }
//                        }
//                    }else if(i + addTimes == allCount){
//                        textBmp = SomeBitMapHandleWay.compoundOneBitPicWithBimapsReturnBitmap(textPaths);
//                        textPaths.clear();
//                        if (textBmp != null ){
//                            ReadPictureManage.GetInstance().GetReadPicture(0).Add(new BitmapWithOtherMsg(textBmp,true));
//                        }
//                    }
//                }
                i = i + addTimes;
            }
        }
        return paths;
    }

    //统计字符在字符串中出现的次数
    public static int countCharNum(String src,String one_char){
        int times = 0;
        for (int i = 0; i < src.length(); i++) {
            if (src.substring(i,i+1).equals(one_char)){
                times ++ ;
            }
        }
        return times;
    }

    //拼接小字体和大字体的方法
    public static String pingJieSmallAndBigFont(String srcPath,PrintAndDatas one_data,int x_offset){
        int print_width = EpsonPicture.getPrintWidth();
        Bitmap src = BitmapFactory.decodeFile(srcPath);
        Bitmap one = BitmapFactory.decodeFile(EpsonPicture.getBitMapByPrintAndDatasReturnBitmap(one_data));
        Bitmap dest = Bitmap.createBitmap(print_width, src.getHeight() - EpsonPicture.SMALL_LINE_HEIGHT + one.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(dest);
        canvas.drawColor(Color.WHITE);
        Rect topSrcRect = new Rect(0,0,src.getWidth(),src.getHeight() - EpsonPicture.SMALL_LINE_HEIGHT);
        Rect topDestRect = new Rect(0,0,src.getWidth(),src.getHeight() - EpsonPicture.SMALL_LINE_HEIGHT);

        Rect leftSrcRect = new Rect(0,src.getHeight() - EpsonPicture.SMALL_LINE_HEIGHT,src.getWidth(),src.getHeight());
        Rect leftDestRect = new Rect(0,dest.getHeight() - EpsonPicture.SMALL_LINE_HEIGHT,src.getWidth(),dest.getHeight());

        Rect rightSrcRect = new Rect(0,0,one.getWidth(),one.getHeight());
        Rect rightDestRect = new Rect(x_offset,dest.getHeight() - one.getHeight(),one.getWidth() + x_offset,dest.getHeight());

        canvas.drawBitmap(src,topSrcRect,topDestRect,null);
        canvas.drawBitmap(src,leftSrcRect,leftDestRect,null);
        canvas.drawBitmap(one,rightSrcRect,rightDestRect,null);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_2552" + ".bmp";
        EpsonPicture.saveBmpUse1Bit(dest,bmpPath);

        canvas =null;
        if (src != null && !src.isRecycled()){
            src.recycle();
            src = null;
        }
        if (one != null && !one.isRecycled()){
            one.recycle();
            one = null;
        }
        if (dest != null && !dest.isRecycled()){
            dest.recycle();
            dest = null;
        }

        return bmpPath;
    }

    //直接在图片的底部拼接上一段图片
    public static String pingJieAtBottom(String srcPath,PrintAndDatas one_data,int x_offset){
        int print_width = EpsonPicture.getPrintWidth();
        String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_2553" + ".bmp";
        Bitmap src = BitmapFactory.decodeFile(srcPath);
        Bitmap one = BitmapFactory.decodeFile(EpsonPicture.getBitMapByPrintAndDatasReturnBitmap(one_data));
        Bitmap dest = Bitmap.createBitmap(print_width, src.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(dest);
        canvas.drawColor(Color.WHITE);
        Rect topSrcRect = new Rect(0,0,src.getWidth(),src.getHeight());
        Rect topDestRect = new Rect(0,0,src.getWidth(),src.getHeight());

        Rect rightSrcRect = new Rect(0,0,one.getWidth(),one.getHeight());
        Rect rightDestRect = new Rect(x_offset,dest.getHeight() - one.getHeight(),one.getWidth() + x_offset,dest.getHeight());

        canvas.drawBitmap(src,topSrcRect,topDestRect,null);
        canvas.drawBitmap(one,rightSrcRect,rightDestRect,null);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        EpsonPicture.saveBmpUse1Bit(dest,bmpPath);
        canvas = null;
        if (src != null && !src.isRecycled()){
            src.recycle();
            src = null;
        }
        if (one != null && !one.isRecycled()){
            one.recycle();
            one = null;
        }

        return bmpPath;
    }


    public static void saveAsBitmapWithByteData(byte[] datas ,int with ,String path) {
        FileOutputStream fos = null;
        int nBmpWidth = with;

        try {
            fos = new FileOutputStream(new File(path));
            int line_byte_num = nBmpWidth/8;
            int nBmpHeight = datas.length/line_byte_num;
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            int bufferSize = nBmpHeight * (nBmpWidth*3);
            // bmp文件头
            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;
            // 保存bmp文件头ͷ
            writeWord(fos, bfType);
            writeDword(fos, bfSize);
            writeWord(fos, bfReserved1);
            writeWord(fos, bfReserved2);
            writeDword(fos, bfOffBits);
            // bmp信息头
            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;
            // 保存bmp信息头ͷ
            writeDword(fos, biSize);
            writeLong(fos, biWidth);
            writeLong(fos, biHeight);
            writeWord(fos, biPlanes);
            writeWord(fos, biBitCount);
            writeDword(fos, biCompression);
            writeDword(fos, biSizeImage);
            writeLong(fos, biXpelsPerMeter);
            writeLong(fos, biYPelsPerMeter);
            writeDword(fos, biClrUsed);
            writeDword(fos, biClrImportant);
            // 像素扫描
            byte bmpData[] = new byte[bufferSize];

            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth/8; wRow++) {
                    int index_24 = line_byte_num * nCol + wRow;
                    int clr = datas[index_24];
                    for (int i = 0; i < 8; i++) {
                        int one  =( clr >> (7-i)) & 0x01;
                        if(one == 1) {
                            bmpData[nRealCol * wWidth + wByteIdex] = (byte) (0x00);
                            bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) (0x00 & 0xFF);
                            bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) (0x00 & 0xFF);
                        }else {
                            bmpData[nRealCol * wWidth + wByteIdex] = (byte) (0xff);
                            bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) (0xff & 0xFF);
                            bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) (0xff & 0xFF);
                        }
                        wByteIdex += 3;
                    }


                }
            fos.write(bmpData);
            fos.flush();
            fos.close();

        } catch (Exception e) {

        }
    }

    /**
     *    将位图数据按照给定的宽度并在给定的路径生成bmp图片
     *
     *    宽度是点的宽度  如384 一般为8 的倍数
     * */
    public static void saveAsBitmapWithByteDataUse1Bit(byte[] datas , int width, String path) {
        FileOutputStream fos = null;
        int nBmpWidth = width;

        try {
            fos = new FileOutputStream(new File(path));

            int line_byte_num = nBmpWidth/8;
            int hHeight = datas.length/line_byte_num;
            int wWidth = ((nBmpWidth + 31)/32)*32;
            int bufferSize =  hHeight * wWidth / 8;

            fos.write(EpsonPicture.addBMPImageHeader(bufferSize + 62));
            fos.write(EpsonPicture.addBMPImageInfosHeader(wWidth ,hHeight,datas.length));
            fos.write(EpsonPicture.addBMPImageColorTable());
            //像素扫描 并用0x00补位
            byte bmpData[] = new byte[bufferSize];

            for (int i = 0; i < hHeight; i++) {
                for (int j = 0; j < wWidth / 8 ; j++) {
                    int srcDataIndex = i * line_byte_num + j;
                    int destDataIndex = (hHeight - i - 1) * (wWidth / 8) + j;

                    if(j < line_byte_num) {
                        bmpData[destDataIndex] = datas[srcDataIndex];
                    }else{
                        bmpData[destDataIndex] = 0x00;
                    }
                }
            }
            fos.write(bmpData);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            System.out.println(e.toString());
        }


    }

    protected static void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    protected static void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    protected static void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    public static byte[] addByteArrToByteArr(byte[] dest ,byte[] src,int srcLen) {
        if(dest == null) {
            byte[] datas = new byte[srcLen];
            System.arraycopy(src, 0, datas, 0, srcLen);
            return datas;
        }
        byte[] datas = new byte[dest.length + srcLen];

        System.arraycopy(dest, 0, datas, 0, dest.length);
        System.arraycopy(src, 0, datas, dest.length, srcLen);

        return datas;
    }

}
