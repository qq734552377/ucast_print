package com.ucast.jnidiaoyongdemo.bmpTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;

import com.ucast.jnidiaoyongdemo.tools.ExceptionApplication;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.tools.SavePasswd;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by pj on 2016/6/8.
 * 描述：打印图片生成
 */
public class EpsonPicture {
    public final static String ALBUM_PATH
            = Environment.getExternalStorageDirectory().toString();
    public static final String TEMPBITPATH = EpsonPicture.ALBUM_PATH + File.separator + "Ucast/temp";
    private final static String BIT_NAME = "/Ucast/ucast.bmp";


    public final static int FONT_SIZE = 24 ;
    private static int LINE_STRING_NUMBER = SomeBitMapHandleWay.PRINT_WIDTH / ( FONT_SIZE / 2) ;
    private static int LINE_BIG_STRING_NUMBER = 21 ;
    private final static int OFFSET_X = 0 ;
    private final static int OFFSET_Y = 0 ;

    private final static int FONT_SIZE_TIMES = 1 ;
    private final static int LINE_HEIGHT = 40 ;
    public final static int SMALL_LINE_HEIGHT = 26 ;
    private final static String FONT = "simsun.ttc" ;
    private final static int BITMAP_END_POINT = 384 ;
    private final static int CUT_PAPER_HEIGHT = 40 ;
    private final static int SMALL_CUT_PAPER_HEIGHT = 30 ;
    public static Typeface FONT_TYPE = Typeface.createFromAsset(ExceptionApplication.getInstance().getAssets(),FONT);

    public static String getBitMap(List<PrintAndDatas> printAndDatasList) {

        int width = EpsonPicture.getPrintWidth();
        int line_sizes = 0 ;
        for (int i = 0; i < printAndDatasList.size(); i++) {
            PrintAndDatas one = printAndDatasList.get(i);
            List<String> list = getLineStringDatas(one.datas);
            line_sizes += list.size() * one.FONT_SIZE_TIMES;
        }
        int Height = line_sizes * LINE_HEIGHT;
        Bitmap bmp = Bitmap.createBitmap(width, Height + 4, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);


        int cur_line = 0;
        for (int i = 0; i < printAndDatasList.size(); i++) {

            PrintAndDatas one = printAndDatasList.get(i);
            List<String> list = getLineStringDatas(one.datas);
            Paint print = new Paint();
            print.setColor(Color.BLACK);
            print.setTextSize(one.FONT_SIZE);
            if(one.FONT_SIZE_TIMES ==2){
                print.setTextSize(one.FONT_SIZE * one.FONT_SIZE_TIMES *3 / 4);
            }

            print.setTypeface(Typeface.MONOSPACE);
            Typeface font = Typeface.createFromAsset(ExceptionApplication.getInstance().getAssets(),FONT);
            print.setTypeface(Typeface.create(font,Typeface.NORMAL));
            for (int j = 0; j < list.size(); j++) {
                canvas.drawText(list.get(j), one.OFFSET_X, cur_line * one.LINE_HEIGHT +one.OFFSET_Y * one.FONT_SIZE_TIMES, print);
                cur_line ++;
            }
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        String path = saveBmpUse1Bit(bmp ,null);
        return path;
    }

    /**
     *  将给定的打印数据生成bmp图片 返回路径
     * */
    public static String getBitMapByString(String string ,String outPath) {
//        Bitmap bmp = getBitMapByStringReturnBitmaPath(string);
//        String path = saveBmpUse1Bit(bmp ,outPath);
//        if (bmp != null && !bmp.isRecycled()) {
//            bmp.recycle();
//            bmp = null;
//        }
        return getBitMapByStringReturnBitmaPath(string);
    }

    /**
     *  将给定的打印数据生成bmp图片 返回Bitmap的文件路径
     * */
    public static String getBitMapByStringReturnBitmaPath(String string) {
        int width = EpsonPicture.getPrintWidth();
        LINE_STRING_NUMBER = width / ( FONT_SIZE / 2) ;
        int firstEnterIndex = string.indexOf("\n");
        if (firstEnterIndex != -1 && firstEnterIndex + 1 < width / 12 ) {  //小于一行的空格数据全部忽略
            if (string.substring(0,firstEnterIndex).replace(" ","").equals(""))
                string = string.substring(firstEnterIndex + 1, string.length());
        }
        List<String> list = getLineStringDatas(string);
        int Height = list.size() * SMALL_LINE_HEIGHT;
        Bitmap bmp = Bitmap.createBitmap(width, Height , Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        Paint print = new Paint();
        print.setColor(Color.BLACK);
        print.setTextSize(FONT_SIZE);
        print.setTypeface(Typeface.create(FONT_TYPE,Typeface.NORMAL));
//        print.setTypeface(Typeface.MONOSPACE);
        int offsetY =  4;
        for (int i = 0; i < list.size(); i++) {
            canvas.drawText(list.get(i), OFFSET_X, i * SMALL_LINE_HEIGHT + SMALL_LINE_HEIGHT - offsetY, print);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_2552" + ".bmp";
        saveBmpUse1Bit(bmp,bmpPath);

        canvas = null;
        if (bmp != null && !bmp.isRecycled()){
            bmp.recycle();
            bmp = null;
        }

        return bmpPath;
    }
    /**
     *  将给定的打印数据生成bmp图片 返回Bitmap的文件路径
     * */
    public static String getBitMapByStringReturnBitmaPath(PrintAndDatas printAndDatas) {
        if (printAndDatas.datas.equals(""))
            return null;
        int width = EpsonPicture.getPrintWidth();
        LINE_STRING_NUMBER = width / ( FONT_SIZE / 2) ;
        String string = printAndDatas.getDatas();
        int firstEnterIndex = string.indexOf("\n");
        if (firstEnterIndex != -1 && firstEnterIndex + 1 < width / 12 ) {  //小于一行的空格数据全部忽略
            if (string.substring(0,firstEnterIndex).replace(" ","").equals(""))
                string = string.substring(firstEnterIndex + 1, string.length());
        }
        List<String> list = getLineStringDatas(string);
        if (printAndDatas.getJustification() == 1){//文字居中
            list = getCenterString(list,LINE_STRING_NUMBER);
        }else if (printAndDatas.getJustification() == 2){//文字右对齐
            list = getRightString(list,LINE_STRING_NUMBER);
        }
        if (list.size() == 0)
            return null;
        int Height = list.size() * SMALL_LINE_HEIGHT;
        Bitmap bmp = Bitmap.createBitmap(width, Height , Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        Paint print = getPaint();
//        print.setTypeface(Typeface.MONOSPACE);
        int offsetY =  4;
        for (int i = 0; i < list.size(); i++) {
            canvas.drawText(list.get(i), OFFSET_X, i * SMALL_LINE_HEIGHT + SMALL_LINE_HEIGHT - offsetY, print);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "")+"_2552" + ".bmp";
        saveBmpUse1Bit(bmp,bmpPath);

        canvas = null;
        if (bmp != null && !bmp.isRecycled()){
            bmp.recycle();
            bmp = null;
        }

        return bmpPath;
    }
    /**
     *  将不超过单行的文字生成对应的图片 返回Bitmap的文件路径
     * */
    public static String getBitMapByPrintAndDatasReturnBitmap(PrintAndDatas one_data) {
        if (one_data.datas.equals(""))
            return null;
        String string = one_data.datas.replace("\n","");
        int width = 0;
        try {
            width = string.getBytes("GB18030").length * FONT_SIZE / 2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        width = (width + 7) / 8 * 8;
        int print_width = EpsonPicture.getPrintWidth();
        LINE_STRING_NUMBER = print_width / (FONT_SIZE * one_data.bitWidthRate / 2);
        // 如果文字的内容超过一行
        if ( width * one_data.bitWidthRate > print_width){
            List<String> list = getLineStringDatas(one_data.getDatas());
            if (one_data.getJustification() == 1){//文字居中
                list = getCenterString(list,LINE_STRING_NUMBER);
            }else if (one_data.getJustification() == 2){//文字右对齐
                list = getRightString(list,LINE_STRING_NUMBER);
            }
            if (list.size() == 0)
                return null;
            int height = SMALL_LINE_HEIGHT * list.size();
            width = print_width / one_data.bitWidthRate;
            int offsetY= 4;
            Bitmap bmp = Bitmap.createBitmap(width,height,Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(Color.WHITE);
            Paint paint = getPaint();
            for (int i = 0; i < list.size(); i++) {
                canvas.drawText(list.get(i), OFFSET_X, i * SMALL_LINE_HEIGHT + SMALL_LINE_HEIGHT - offsetY,paint);
            }
            canvas.save(Canvas.ALL_SAVE_FLAG);
            canvas.restore();
            paint.setTypeface(null);

            byte[] sources = getOneBitBytesFromBitmap(bmp);

            canvas = null;
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
                bmp = null;
            }

            if(one_data.bitWidthRate == 2){
                sources =EpsonPicture.getTwiceWidthData(sources , width / 8);
            }
            if (one_data.bitHeightRate == 2){
                sources =EpsonPicture.getTwiceHeighData(sources , width * one_data.bitWidthRate / 8);
            }
            String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "") + "_2557" +".bmp";
            int save_width = width * one_data.bitWidthRate;
            EpsonParseDemo.saveAsBitmapWithByteDataUse1Bit(sources,save_width, bmpPath);
            return bmpPath;

        }
        int height = SMALL_LINE_HEIGHT;
        int offsetY = 4;

        Bitmap bmp = Bitmap.createBitmap(width, height , Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        Paint print = getPaint();

        canvas.drawText(string, OFFSET_X, height - offsetY, print);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        print.setTypeface(null);
        byte[] sources = getOneBitBytesFromBitmap(bmp);
        canvas = null;
        if (bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        if(one_data.bitWidthRate == 2){
            sources =EpsonPicture.getTwiceWidthData(sources , width / 8);
        }
        if (one_data.bitHeightRate == 2){
            sources =EpsonPicture.getTwiceHeighData(sources , width * one_data.bitWidthRate / 8);
        }
        String bmpPath = EpsonPicture.TEMPBITPATH + File.separator + "ucast_bit_and_string_" + UUID.randomUUID().toString().replace("-", "") + "_2557" +".bmp";
        int save_width = width * one_data.bitWidthRate;
        EpsonParseDemo.saveAsBitmapWithByteDataUse1Bit(sources,save_width, bmpPath);
        return bmpPath;
    }
    /**
     *  将给定的打印数据生成bmp图片 返回Bitmap
     * */
    public static Bitmap getBitMapByStringReturnBigBitmap(String string) {
        int width = SomeBitMapHandleWay.PRINT_WIDTH;
        String is_58 = SavePasswd.getInstace().getIp(SavePasswd.IS58PAPPER,"false");
        LINE_BIG_STRING_NUMBER = 32 ;
        if (is_58.equals("true")){
            width = SomeBitMapHandleWay.WIDTH_58;
            LINE_BIG_STRING_NUMBER = 21 ;
        }
        List<String> list = getBigLineStringDatas(string);
        //删除空格过多的地方  使文字居中
        for (int i = 0; i < list.size(); i++) {
            String one_data = list.get(i);
            byte[] one_data_bytes = one_data.getBytes();
            int head_blank_size = 0;
            for (int j = 0; j < one_data_bytes.length; j++) {
                if (one_data_bytes[j] == 0x20){
                    head_blank_size++ ;
                }else{
                    break;
                }
            }
            String available_str = one_data.replace(" ","");
            int head_blanks = (LINE_BIG_STRING_NUMBER - available_str.getBytes().length) / 2;
            if (head_blanks < head_blank_size) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < head_blanks; j++) {
                    sb.append(" ");
                }
                sb.append(available_str);
                list.set(i,sb.toString());
            }else {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < head_blank_size*2/3; j++) {
                    sb.append(" ");
                }
                sb.append(available_str);
                list.set(i,sb.toString());
            }
        }
        int Height = list.size() * LINE_HEIGHT;
        Bitmap bmp = Bitmap.createBitmap(width, Height + 8 , Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);
        Paint print = new Paint();
        print.setColor(Color.BLACK);
        print.setTextSize(36);
        print.setTypeface(Typeface.create(FONT_TYPE,Typeface.NORMAL));
//        print.setTypeface(Typeface.MONOSPACE);
        int offsetY = 2;
        for (int i = 0; i < list.size(); i++) {
            canvas.drawText(list.get(i), 0, i * LINE_HEIGHT + CUT_PAPER_HEIGHT - offsetY, print);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();

        return bmp;
    }
    /**
     * 通过字符串获取分行的数据
     *
     * */
    public static List<String> getLineStringDatas(String string){
        string = string.replace("\r","");
//        String[] dataString = null;
//        dataString = string.split("\n");

        List<String> src = new ArrayList<>();
        int offset = 0;
        int index = string.indexOf("\n",offset);
        while (index != -1){
            src.add(string.substring(offset,index));
            offset = index + 1;
            index = string.indexOf("\n",offset);
        }

        if (offset < string.length()){
            src.add(string.substring(offset));
        }

        List<String> list = new ArrayList<>();
        List<String> splistlist;
        for (int i = 0; i < src.size(); i++) {
            String stringData = src.get(i);
            byte[] one =null;
            try {
                 one = stringData.getBytes("GB18030");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int len = one == null ? stringData.getBytes().length : one.length;
            if ( len > LINE_STRING_NUMBER) {
                splistlist = splitString(stringData);
                for (int t = 0; t < splistlist.size(); t++) {
                    list.add(splistlist.get(t));
                }
            } else {
                list.add(stringData);
            }
        }

        return list;
    }

    /**
     * 通过字符串获取分行的数据
     *
     * */
    public static List<String> getBigLineStringDatas(String string){
        String[] dataString = null;
        dataString = string.replace("\r","").split("\n");
        List<String> list = new ArrayList<>();
        List<String> splistlist;
        for (int i = 0; i < dataString.length; i++) {
            byte[] one =null;
            try {
                one = dataString[i].getBytes("GB18030");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            int len = one == null ? dataString[i].getBytes().length : one.length;
            if ( len > LINE_BIG_STRING_NUMBER) {
                splistlist = splitBigString(dataString[i]);
                for (int t = 0; t < splistlist.size(); t++) {
                    list.add(splistlist.get(t));
                }
            } else {
                list.add(dataString[i]);
            }
        }

        return list;
    }

    /**
     * 拆分字符串
     *
     * @param data
     * @return
     */
    public static List<String> splitString(String data) {
        List<String> list = new ArrayList<>();
        String string = "";
        int offert = 0;
        for (int i = 0; i < data.length(); i++) {
            String s = data.substring(i, i + 1);
            if (s.getBytes().length > 1) {
                string += s;
                offert = offert + 2;
            } else {
                string += s;
                offert++;
            }
            if (offert >= LINE_STRING_NUMBER) {
                list.add(string);
                string = "";
                offert = 0;
            }
        }
        list.add(string);
        return list;
    }

    /**
     * 拆分字符串
     *
     * @param data
     * @return
     */
    public static List<String> splitBigString(String data) {
        List<String> list = new ArrayList<>();
        String string = "";
        int offert = 0;
        for (int i = 0; i < data.length(); i++) {
            String s = data.substring(i, i + 1);
            if (s.getBytes().length > 1) {
                string += s;
                offert = offert + 2;
            } else {
                string += s;
                offert++;
            }
            if (offert >= LINE_BIG_STRING_NUMBER) {
                list.add(string);
                string = "";
                offert = 0;
            }
        }
        list.add(string);
        return list;
    }

    /**
     * 保存图片 位图深度为24
     *
     * @param bitmap
     * @return 生成bmp的绝对路径
     */
    public static String saveBmp(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();
        // 图像数据大小
        int bufferSize = nBmpHeight * (nBmpWidth * 3 );
        try {
            File dirFile = new File(ALBUM_PATH);
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            File myCaptureFile = new File(ALBUM_PATH + BIT_NAME);
            FileOutputStream fileos = new FileOutputStream(myCaptureFile);
            // bmp文件头
            int bfType = 0x4d42;
            long bfOffBits = 14 + 40;
            long bfSize = bfOffBits + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;

            // 保存bmp文件头ͷ
            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);
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
            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);
            // 像素扫描
            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    if(Color.red(clr) > 156){
                        bmpData[nRealCol * wWidth + wByteIdex] = (byte) 0xFF;
                        bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) 0xFF;
                        bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) 0xFF;
                    }else{
                        bmpData[nRealCol * wWidth + wByteIdex] = (byte) 0x00;
                        bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) 0x00;
                        bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) 0x00;
                    }

//                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
//                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
//                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }
            fileos.write(bmpData);
            fileos.flush();
            fileos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return ALBUM_PATH + BIT_NAME;
    }

    /**
     * 保存为bmp图片 位图深度为1
     *
     * @param bitmap
     * @return 生成bmp的绝对路径
     */
    public static String saveBmpUse1Bit(Bitmap bitmap,String outPath){
        if (bitmap == null)
            return null;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        byte[] datas = getSaveOnebitBmpData(bitmap);

        int line_byte_num = w / 8;
        int saveBmpHeight = h;
        int saveBmpWidth = ((w + 31)/32) * 32;
        int bufferSize =  saveBmpHeight * saveBmpWidth / 8;

        byte[] header = addBMPImageHeader(62 + bufferSize );
        byte[] infos = addBMPImageInfosHeader(saveBmpWidth, saveBmpHeight,bufferSize);
        byte[] colortable = addBMPImageColorTable();

        // 像素扫描 并用0x00补位
        byte bmpData[] = new byte[bufferSize];
        for (int i = 0; i < saveBmpHeight; i++) {
            for (int j = 0; j < saveBmpWidth / 8 ; j++) {
                int srcDataIndex = i * line_byte_num + j;
                int destDataIndex = i * (saveBmpWidth / 8) + j;

                if(j < line_byte_num) {
                    bmpData[destDataIndex] = datas[srcDataIndex];
                }else{
                    bmpData[destDataIndex] = 0x00;
                }
            }
        }
        String path = "";
        try {
            File dirFile = new File(ALBUM_PATH + "/Ucast/" +MyTools.millisToDateStringOnlyYMD(System.currentTimeMillis()));
            if (!dirFile.exists()) {
                dirFile.mkdir();
            }
            if (outPath == null) {
                path = ALBUM_PATH + "/Ucast/" + MyTools.millisToDateStringOnlyYMD(System.currentTimeMillis()) + File.separator + MyTools.millisToDateStringNoSpace(System.currentTimeMillis()) +"_"+ UUID.randomUUID().toString().replace("-", "") + ".bmp";
            }else{
                path = outPath;
            }
            File myCaptureFile = new File(path);
            FileOutputStream fileos = new FileOutputStream(myCaptureFile);

            fileos.write(header);
            fileos.write(infos);
            fileos.write(colortable);
            fileos.write(bmpData);

            fileos.flush();
            fileos.close();

        } catch (Exception e){
            return null;
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return path;
    }


    // BMP文件头
    public static byte[] addBMPImageHeader(int size) {
        byte[] buffer = new byte[14];
        buffer[0] = 0x42;
        buffer[1] = 0x4D;
        buffer[2] = (byte) (size >> 0);
        buffer[3] = (byte) (size >> 8);
        buffer[4] = (byte) (size >> 16);
        buffer[5] = (byte) (size >> 24);
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        buffer[8] = 0x00;
        buffer[9] = 0x00;
        //  buffer[10] = 0x36;
        buffer[10] = 0x3E;
        buffer[11] = 0x00;
        buffer[12] = 0x00;
        buffer[13] = 0x00;
        return buffer;
    }
    // BMP文件信息头
    public static byte[] addBMPImageInfosHeader(int w, int h, int size) {
        byte[] buffer = new byte[40];
        buffer[0] = 0x28;
        buffer[1] = 0x00;
        buffer[2] = 0x00;
        buffer[3] = 0x00;

        buffer[4] = (byte) (w >> 0);
        buffer[5] = (byte) (w >> 8);
        buffer[6] = (byte) (w >> 16);
        buffer[7] = (byte) (w >> 24);

        buffer[8] = (byte) (h >> 0);
        buffer[9] = (byte) (h >> 8);
        buffer[10] = (byte) (h >> 16);
        buffer[11] = (byte) (h >> 24);

        buffer[12] = 0x01;
        buffer[13] = 0x00;

        buffer[14] = 0x01;
        buffer[15] = 0x00;

        buffer[16] = 0x00;
        buffer[17] = 0x00;
        buffer[18] = 0x00;
        buffer[19] = 0x00;

        buffer[20] = (byte) (size >> 0);
        buffer[21] = (byte) (size >> 8);
        buffer[22] = (byte) (size >> 16);
        buffer[23] = (byte) (size >> 24);

        //  buffer[24] = (byte) 0xE0;
        //  buffer[25] = 0x01;
        buffer[24] = (byte) 0xC3;
        buffer[25] = 0x0E;
        buffer[26] = 0x00;
        buffer[27] = 0x00;

        //  buffer[28] = 0x02;
        //  buffer[29] = 0x03;
        buffer[28] = (byte) 0xC3;
        buffer[29] = 0x0E;
        buffer[30] = 0x00;
        buffer[31] = 0x00;

        buffer[32] = 0x00;
        buffer[33] = 0x00;
        buffer[34] = 0x00;
        buffer[35] = 0x00;

        buffer[36] = 0x00;
        buffer[37] = 0x00;
        buffer[38] = 0x00;
        buffer[39] = 0x00;
        return buffer;
    }
    //bmp调色板
    public static byte[] addBMPImageColorTable() {
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xFF;
        buffer[1] = (byte) 0xFF;
        buffer[2] = (byte) 0xFF;
        buffer[3] = 0x00;

        buffer[4] = 0x00;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        return buffer;
    }

    //将bitmap对象中像素数据转换成位图深度为1的bmp数据
    private static byte[] getSaveOnebitBmpData(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int len = w * h;
        int[] b = new int[ len ];
        bitmap.getPixels(b, 0, w, 0, 0, w, h);//取得BITMAP的所有像素点


        int bufflen = 0;
        int[] tmp = new int[3];
        int index = 0,bitindex = 1;
        //将8字节变成1个字节,不足补0
        if (len% 8 != 0){
            bufflen = len / 8 + 1;
        } else {
            bufflen = len / 8;
        }
        //BMP图像数据大小，必须是4的倍数，图像数据大小不是4的倍数时用0填充补足
        if (bufflen % 4 != 0){
            bufflen = bufflen + bufflen%4;
        }

        byte[] buffer = new byte[bufflen];

        for (int i = len - 1; i >= w; i -= w) {
            // DIB文件格式最后一行为第一行，每行按从左到右顺序
            int end = i, start = i - w + 1;
            for (int j = start; j <= end; j++) {

                tmp[0] = b[j]  & 0x000000FF;
                tmp[1] = b[j]  & 0x0000FF00;
                tmp[2] = b[j]  & 0x00FF0000;

                if (bitindex > 8) {
                    index += 1;
                    bitindex = 1;
                }

                if (tmp[0] + tmp[1] +tmp[2] != 0x00FFFFFF) {
                    buffer[index] = (byte) (buffer[index] | (0x01 << 8-bitindex));
                }
                bitindex++;
            }
        }

        return buffer;
    }
    //将bitmap对象中像素数据转换成位图深度为1的bmp数据 非存储的数据格式
    private static byte[] getOneBitBytesFromBitmap(Bitmap bitmap) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int len = w * h;
        int[] src = new int[ len ];
        bitmap.getPixels(src, 0, w, 0, 0, w, h);//取得BITMAP的所有像素点
        int bufflen = 0;
        int[] tmp = new int[3];
        int index = 0,bitindex = 1;
        //将8字节变成1个字节,不足补0
        if (len% 8 != 0){
            bufflen = len / 8 + 1;
        } else {
            bufflen = len / 8;
        }
        byte[] dest = new byte[bufflen];
        for (int i = 0; i <= len - w ; i += w) {
            int end = i + w - 1, start = i;
            for (int j = start; j <= end; j++) {
                tmp[0] = src[j]  & 0x000000FF;
                tmp[1] = src[j]  & 0x0000FF00;
                tmp[2] = src[j]  & 0x00FF0000;
                if (bitindex > 8) {
                    index += 1;
                    bitindex = 1;
                }
                if (tmp[0] + tmp[1] +tmp[2] != 0x00FFFFFF) {
                    dest[index] = (byte) (dest[index] | (0x01 << 8-bitindex));
                }
                bitindex++;
            }
        }

        return dest;
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

    /**
     * 8位位图的颜色调板
     */
    protected static byte[] addBMP8ImageInfosHeaderTable() {
        byte[] buffer = new byte[256 * 4];

        //生成颜色表
        for (int i = 0; i < 256; i++) {
            buffer[0 + 4 * i] = (byte) i;   //Blue
            buffer[1 + 4 * i] = (byte) i;   //Green
            buffer[2 + 4 * i] = (byte) i;   //Red
            buffer[3 + 4 * i] = (byte) 0x00;   //保留值
        }

        return buffer;
    }
    //单色位图的颜色调板
    private static void addBMPImageColorTable(FileOutputStream stream) throws IOException{
        byte[] buffer = new byte[8];
        buffer[0] = (byte) 0xFF;
        buffer[1] = (byte) 0xFF;
        buffer[2] = (byte) 0xFF;
        buffer[3] = 0x00;

        buffer[4] = 0x00;
        buffer[5] = 0x00;
        buffer[6] = 0x00;
        buffer[7] = 0x00;
        stream.write(buffer);
    }

    //测试方法
    public static void strToBmp() throws IOException {
        String path = ALBUM_PATH + BIT_NAME;

        String path_res = ALBUM_PATH + "/point_data_result.bmp";
        String path_txt = ALBUM_PATH + "/point_data.txt";

        FileOutputStream fos = null;
        FileOutputStream fosToTxt = null;

        try {
            File write_file = new File(path_res);
            byte[] datas = turnBytes(BitmapFactory.decodeFile(path));
            fos = new FileOutputStream(write_file);

            fosToTxt = new FileOutputStream(path_txt);
            fosToTxt.write(datas);

            int nBmpWidth = 384;
            int line_byte_num = nBmpWidth/8;
            int nBmpHeight = datas.length/line_byte_num;
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            int bufferSize = nBmpHeight * (nBmpWidth * 3);
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
                        int one  =( clr >> (7 - i)) & 0x01;
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
        } catch (Exception e) {

        }finally {
            fos.close();
            fosToTxt.close();
        }

    }

    //存成24位图像
    public static void saveDataToBmp(byte[] datas){
        String path = ALBUM_PATH + BIT_NAME;

        String path_res = ALBUM_PATH + "/point_data_result.bmp";

        FileOutputStream fos = null;

        int nBmpWidth = 384;

        try {
            File write_file = new File(path_res);
            fos = new FileOutputStream(write_file);


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
                        int one  =( clr >> (7 - i)) & 0x01;
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

        }finally {
        }
    }

    //效率不怎么高的取图片数据
    public static byte[] TurnBytes(Bitmap bitmap) {
        int W = bitmap.getWidth();
        int PW = SomeBitMapHandleWay.PRINT_WIDTH;
        int copyW = PW < W ? PW : W ;
        int H = bitmap.getHeight();
        byte[] bt = new byte[PW / 8 * H];
        for (int i = 0; i < H; i++) {
            for (int j = 0; j < copyW; j = j + 8) {
                byte value = 0;
                for (int s = 0; s <= 7; s++) {
                    int a = bitmap.getPixel(j + s, i);
                    int aa = a & 0xff;
                    if (aa != 255) {
                        value |= 1 << s;
                    }
                }
                bt[i * PW / 8 + j / 8] = value;
            }
        }
        return bt;
    }

    public static byte[] turnBytes(Bitmap bitmap) {
        if (bitmap == null)
            return null;
        String is_58 = SavePasswd.getInstace().getIp(SavePasswd.IS58PAPPER,"false");
        if (is_58.equals("true")){
            bitmap = SomeBitMapHandleWay.move_192(bitmap);
        }
        int w = bitmap.getWidth();
        int PW = SomeBitMapHandleWay.PRINT_WIDTH;
        int copyW = PW < w ? PW : w ;
        int h = bitmap.getHeight();
        byte[] bt = new byte[PW / 8 * h];
        int len = w * h;
        int[] b = new int[ len ];
        bitmap.getPixels(b, 0, w, 0, 0, w, h);//取得BITMAP的所有像素点
        for (int i = 0; i < h; i ++) {
            for (int j = 0; j < copyW; j = j + 8) {
                byte value = 0;
                for (int s = 0; s <= 7; s++) {
                    if (j + s > w -1){
                        continue;
                    }
                    int a = b[i * w + j + s];
                    int red = Color.red(a);
                    int green = Color.green(a);
                    int blue = Color.blue(a);
                    int gray =(int) (red*0.299 + green*0.587 + blue*0.114);
                    if (gray < 150) {
                        value |= 1 << s;
                    }
                }
                bt[i * PW / 8 + j / 8] = value;
            }
        }
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        return bt;
    }

    /**
     * the traditional io way
     *
     * @param filename
     * @return
     */
    public static byte[] getByteArrayFromFile(String filename) {

        File f = new File(filename);
        if (!f.exists()) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                in.close();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *  将一整张位图数据的高度增加两倍
     * */
    public static byte[] getTwiceHeighData(byte[] res , int width){

        int resLength = res.length;
        if( resLength % width != 0) {
            return res;
        }
        byte[] des = new byte[resLength * 2];
        for (int i = 0; i < resLength/width; i++) {
            System.arraycopy(res, i * width, des, 2 * i * width  , width);
            System.arraycopy(res, i * width, des, 2 * i * width + width  , width);
        }
        return des;
    }

    /**
     *  将一整张位图数据的宽度增加两倍
     * */
    public static byte[] getTwiceWidthData(byte[] res , int width){
        int resLength = res.length;
        if( resLength % width != 0) {
            return res;
        }
        byte[] des = new byte[resLength * 2];
        for (int i = 0; i < resLength; i++) {
            byte one = res[i];
            byte front = 0x00;
            byte  back= 0x00;
            for (int j = 0; j < 4; j++) {
                int  front_one_bit = 0x01 & (one >> (7 - j));
                if(front_one_bit == 1) {
                    front = (byte) (front | (0x01 << (7 - 2 * j)));
                    front = (byte) (front | (0x01 << (6 - 2 * j)));
                }
                int  back_one_bit = 0x01 & (one >> (3 - j));
                if(back_one_bit == 1) {
                    back = (byte) (back | (0x01 << (7 - 2 * j)));
                    back = (byte) (back | (0x01 << (6 - 2 * j)));
                }
            }
            des[2 * i ] = front;
            des[2 * i + 1 ] = back;
        }

        return des;
    }

     //获取基于打印纸宽度的居中的图片数据 宽度是单位8个点即1个byte
    public static byte[] getCenterBitData(byte[] src,int width){
        int printWidth = EpsonPicture.getPrintWidth();
        int one_line_max_number = printWidth / 8;
        if (width >= one_line_max_number)
            return src;
        int offsetNumber = (one_line_max_number - width) / 2;
        return getOffsetBitData(src,width,offsetNumber);
    }
    //获取基于打印纸宽度的右对齐的图片数据 宽度是单位8个点即1个byte
    public static byte[] getRightBitData(byte[] src,int width){
        int printWidth = EpsonPicture.getPrintWidth();
        int one_line_max_number = printWidth / 8;
        if (width >= one_line_max_number)
            return src;
        int offsetNumber = one_line_max_number - width;
        return getOffsetBitData(src,width,offsetNumber);
    }
    //获取偏移量为offsetNumber的图片数据 宽度是单位8个点即1个byte
    public static byte[] getOffsetBitData(byte[] src, int width, int offsetNumber){
        int height = src.length / width;
        byte[] dest = new byte[(offsetNumber + width) * height];
        for (int i = 0; i < height; i++) {
            System.arraycopy(src,i * width,dest,offsetNumber * (i + 1) + width * i,width);
        }
        return dest;
    }

    //获取基于打印纸宽度的居中文字
    public static List<String> getCenterString(List<String> src,int oneLineNumber){
        for (int i = 0; i < src.size(); i++) {
            String one = src.get(i);
            int one_string_number =  0;
            try {
                one_string_number = one.getBytes("GB18030").length;
            }catch (Exception e){}
            if (one_string_number >= oneLineNumber)
                continue;
            int offsetBlankNumber = (oneLineNumber - one_string_number) / 2;
            if (offsetBlankNumber <= 0)
                continue;
            src.set(i,getOffsetString(one,offsetBlankNumber));
        }
        return src;
    }
    //获取基于打印纸宽度的右对齐文字
    public static List<String> getRightString(List<String> src,int oneLineNumber){
        for (int i = 0; i < src.size(); i++) {
            String one = src.get(i);
            int one_string_number =  0;
            try {
                one_string_number = one.getBytes("GB18030").length;
            }catch (Exception e){}
            if (one_string_number >= oneLineNumber)
                continue;
            int offsetBlankNumber = oneLineNumber - one_string_number;
            src.set(i,getOffsetString(one,offsetBlankNumber));
        }
        return src;
    }

    //获取前面加偏移的空格
    public static String getOffsetString(String src,int offsetBlankNumber){
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < offsetBlankNumber; j++) {
            sb.append(" ");
        }
        sb.append(src);
        return sb.toString();
    }


    public static byte fanWei(byte src){
        byte des = 0x00;
        for (int i = 0; i < 8 ; i++) {
            if (((src >> i) & 0x01) == 1){
                des = (byte) (des | 0x01 << (7 - i));
            }
        }
        return des;
    }

    public static int getPrintWidth() {
        int width = SomeBitMapHandleWay.PRINT_WIDTH;
        String is_58 = SavePasswd.getInstace().getIp(SavePasswd.IS58PAPPER, "false");
        if (is_58.equals("true")) {
            width = SomeBitMapHandleWay.WIDTH_58;
        }
        return width;
    }
    public static Paint getPaint() {
        Paint print = new Paint();
        print.setColor(Color.BLACK);
        print.setTextSize(FONT_SIZE);
        print.setTypeface(Typeface.create(FONT_TYPE,Typeface.NORMAL));
//        print.setFlags(Paint.LINEAR_TEXT_FLAG)  ;
        return print;
    }
    public static Paint getPaint(PrintAndDatas printAndDatas) {
        Paint print = new Paint();
        print.setColor(Color.BLACK);
        print.setTextSize(FONT_SIZE);
        print.setTypeface(Typeface.create(FONT_TYPE,Typeface.NORMAL));
        return print;
    }
}
