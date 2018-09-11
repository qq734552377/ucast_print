package com.ucast.jnidiaoyongdemo.bmpTools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.ucast.jnidiaoyongdemo.Model.BitmapWithOtherMsg;
import com.ucast.jnidiaoyongdemo.Model.ReadPictureManage;
import com.ucast.jnidiaoyongdemo.tools.MyTools;
import com.ucast.jnidiaoyongdemo.tools.YinlianHttpRequestUrl;

import java.util.List;

import static com.ucast.jnidiaoyongdemo.tools.CrashHandler.TAG;

/**
 * Created by pj on 2018/1/18.
 */

public class SomeBitMapHandleWay {

    public final static int PRINT_WIDTH = 576 ;
    public final static int WIDTH_58 = 384 ;

    /**
     *  左移 192个点
     */
    public static Bitmap move_192(Bitmap src){
        if (src == null)
            return  null;
        // 定义输出的bitmap
        Bitmap bitmap = Bitmap.createBitmap(PRINT_WIDTH, src.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Rect srcRect = new Rect(0,0,WIDTH_58,src.getHeight());
        Rect destRect = new Rect(PRINT_WIDTH - WIDTH_58, 0, PRINT_WIDTH, src.getHeight());
        canvas.drawBitmap(src,srcRect,destRect,null);

        return bitmap;
    }



    /**
     * 把两个位图覆盖合成为一个位图，以底层位图的长宽为基准
     * @param backBitmap 在底部的位图
     * @param frontBitmap 盖在上面的位图
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap backBitmap, Bitmap frontBitmap) {

        if (backBitmap == null || backBitmap.isRecycled()
                || frontBitmap == null || frontBitmap.isRecycled()) {
            Log.e(TAG, "backBitmap=" + backBitmap + ";frontBitmap=" + frontBitmap);
            return null;
        }
        Bitmap bitmap = backBitmap.copy(Bitmap.Config.RGB_565, true);
        Canvas canvas = new Canvas(bitmap);
        Rect baseRect  = new Rect(0, 0, backBitmap.getWidth(), backBitmap.getHeight());
        Rect frontRect = new Rect(0, 0, frontBitmap.getWidth(), frontBitmap.getHeight());
        canvas.drawBitmap(frontBitmap, frontRect, baseRect, null);
        return bitmap;
    }
    /**
     * 把两个位图覆盖合成为一个位图，左右拼接
     * @param leftBitmap
     * @param rightBitmap
     * @param isBaseMax 是否以宽度大的位图为准，true则小图等比拉伸，false则大图等比压缩
     * @return
     */
    public static Bitmap mergeBitmap_LR(Bitmap leftBitmap, Bitmap rightBitmap, boolean isBaseMax) {

        if (leftBitmap == null || leftBitmap.isRecycled()
                || rightBitmap == null || rightBitmap.isRecycled()) {
            return null;
        }
        int height = 0; // 拼接后的高度，按照参数取大或取小
        if (isBaseMax) {
            height = leftBitmap.getHeight() > rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        } else {
            height = leftBitmap.getHeight() < rightBitmap.getHeight() ? leftBitmap.getHeight() : rightBitmap.getHeight();
        }

        // 缩放之后的bitmap
        Bitmap tempBitmapL = leftBitmap;
        Bitmap tempBitmapR = rightBitmap;

        if (leftBitmap.getHeight() != height) {
            tempBitmapL = Bitmap.createScaledBitmap(leftBitmap, (int)(leftBitmap.getWidth()*1f/leftBitmap.getHeight()*height), height, false);
        } else if (rightBitmap.getHeight() != height) {
            tempBitmapR = Bitmap.createScaledBitmap(rightBitmap, (int)(rightBitmap.getWidth()*1f/rightBitmap.getHeight()*height), height, false);
        }

        // 拼接后的宽度
        int width = tempBitmapL.getWidth() + tempBitmapR.getWidth();

        // 定义输出的bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        // 缩放后两个bitmap需要绘制的参数
        Rect leftRect = new Rect(0, 0, tempBitmapL.getWidth(), tempBitmapL.getHeight());
        Rect rightRect  = new Rect(0, 0, tempBitmapR.getWidth(), tempBitmapR.getHeight());

        // 右边图需要绘制的位置，往右边偏移左边图的宽度，高度是相同的
        Rect rightRectT  = new Rect(tempBitmapL.getWidth(), 0, width, height);

        canvas.drawBitmap(tempBitmapL, leftRect, leftRect, null);
        canvas.drawBitmap(tempBitmapR, rightRect, rightRectT, null);
        return bitmap;
    }


    /**
     * 把两个位图覆盖合成为一个位图，上下拼接
     * @return
     */
    public static Bitmap mergeBitmap_TB(Bitmap topBitmap, Bitmap bottomBitmap) {

        if (topBitmap == null || topBitmap.isRecycled()
                || bottomBitmap == null || bottomBitmap.isRecycled()) {
            return null;
        }
        int width = PRINT_WIDTH;

        width = topBitmap.getWidth() >= bottomBitmap.getWidth() ? topBitmap.getWidth() : bottomBitmap.getWidth();
        width = width > PRINT_WIDTH ? width : PRINT_WIDTH;

        Bitmap tempBitmapT = topBitmap;
        Bitmap tempBitmapB = bottomBitmap;

//        if (topBitmap.getWidth() != width) {
//            tempBitmapT = Bitmap.createScaledBitmap(topBitmap, width, (int)(topBitmap.getHeight()*1f/topBitmap.getWidth()*width), false);
//        } else if (bottomBitmap.getWidth() != width) {
//            tempBitmapB = Bitmap.createScaledBitmap(bottomBitmap, width, (int)(bottomBitmap.getHeight()*1f/bottomBitmap.getWidth()*width), false);
//        }

        int height = tempBitmapT.getHeight() + tempBitmapB.getHeight() ;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Rect topRect = new Rect(0, 0, width, tempBitmapT.getHeight());
        Rect bottomRect  = new Rect(0, 0, width, tempBitmapB.getHeight());

        Rect topRectT  = new Rect(0, 0, width, tempBitmapT.getHeight());
        Rect bottomRectT  = new Rect(0, tempBitmapT.getHeight(), width , height );

        canvas.drawBitmap(tempBitmapT, topRect, topRectT, null);
        canvas.drawBitmap(tempBitmapB, bottomRect, bottomRectT, null);


        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }
    /**Bitmap缩小的方法*/
    public static Bitmap scallTo(Bitmap bitmap) {
        float newWith = 384f;
        float with = (float) bitmap.getWidth();
        float scale = with / newWith;
        if(scale < 1.0f){
            return  bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scale,scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);

        return resizeBmp;
    }


    /** 添加头尾的预留切纸位*/
    public static Bitmap addHeadAndEndCutPosition(Bitmap src){
        return addHeadAndEndCutPosition(src,0,0);
    }

    /** 添加头尾的预留切纸位*/
    public static Bitmap addHeadAndEndCutPosition(Bitmap src,int headHeight,int endHeight){
        if (src == null || src.isRecycled()) {
            return null;
        }

        if(headHeight == 0 && endHeight == 0){
            return src;
        }

        int width = src.getWidth() > PRINT_WIDTH ? src.getWidth() : PRINT_WIDTH ;

        int height =  src.getHeight() + headHeight + endHeight;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Rect topRectSrc = new Rect(0, 0, width, src.getHeight());
        Rect topRectDest  = new Rect(0, headHeight, width, height - endHeight);

        canvas.drawBitmap(src, topRectSrc, topRectDest, null);

        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap;
    }

    public static String compoundOneBitPic(List<String>  paths){
        String path = "";
        int pathNum = paths.size();
        if ( pathNum< 1) {
            Bitmap bit = BitmapFactory.decodeFile(paths.get(0));
            path = EpsonPicture.saveBmpUse1Bit(SomeBitMapHandleWay.addHeadAndEndCutPosition(bit),null);
            return path;
        }
        Bitmap allBitMap = null;
        for (int i = 0; i < pathNum; i++) {
            if (allBitMap == null){
                allBitMap= BitmapFactory.decodeFile(paths.get(i));
            }
            if(i + 1 == pathNum){
                break;
            }
            Bitmap backBitMap = BitmapFactory.decodeFile(paths.get(i + 1));
            allBitMap = SomeBitMapHandleWay.mergeBitmap_TB(allBitMap,backBitMap);
        }

        if (allBitMap != null){
            Bitmap bit = SomeBitMapHandleWay.addHeadAndEndCutPosition(allBitMap);
            if (bit == null )
                return "";
            path = EpsonPicture.saveBmpUse1Bit(bit, null);
        }

        return path;
    }
    public static String compoundOneBitPicWithBimaps(List<Bitmap>  paths){

        String path = "";
        int pathNum = paths.size();
        if ( pathNum< 1) {
            Bitmap bit = paths.get(0);
            path = EpsonPicture.saveBmpUse1Bit(SomeBitMapHandleWay.addHeadAndEndCutPosition(bit),null);
            return path;
        }
        Bitmap allBitMap = null;
        for (int i = 0; i < pathNum; i++) {
            if (allBitMap == null){
                allBitMap= paths.get(i);
            }
            if(i + 1 == pathNum){
                break;
            }
            Bitmap backBitMap = paths.get(i + 1);
            allBitMap = SomeBitMapHandleWay.mergeBitmap_TB(allBitMap,backBitMap);
        }

        if (allBitMap != null){
            Bitmap bit = SomeBitMapHandleWay.addHeadAndEndCutPosition(allBitMap);
            if (bit == null )
                return "";
            path = EpsonPicture.saveBmpUse1Bit(bit, null);
        }
        return path;
    }

}
