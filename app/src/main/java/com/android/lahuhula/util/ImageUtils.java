package com.android.lahuhula.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.android.lahuhula.view.UserInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by lenovo on 2017/3/1.
 */

public class ImageUtils {
    private static final String TAG = ImageUtils.class.getSimpleName();

    public static final String PICTURE_URL = "http://sxfarm.cn/upload/";
    public static final String ALL_IMAGE_PATH_LIST = "all_image_path_list";
    public static final String SELECTED_IMAGE_PATH_LIST = "selected_image_path_list";
    public static final String CLICKED_IMAGE_INDEX = "clicked_image_index";
    public static final String SELECTED_IMAGE_COUNT = "selected_image_count";
    public static final int MAX_IMAGE_COUNT = 9;
    public static Bitmap mMyIconBitmap = null;

    public static final String MY_ICON = "/my_icon.jpg";
    public static final String HEAD_ICON_NAME = "/head.jpg";

    public static Bitmap saveMyIcon(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = getBitmapFormUri(context, uri);
        } catch (IOException e) {
            return null;
        }
        /*try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            return;
        }*/

        Log.d(TAG, "getAbsolutePath:" + context.getFilesDir().getAbsolutePath());
        File file = new File(context.getFilesDir().getAbsolutePath() + MY_ICON);
        FileOutputStream fos = null;

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Log.d(TAG,  MY_ICON + " file create failed");
                return null;
            }
        }

        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            bitmap = ImageCompress(bitmap);

            postHeadImage(context, bitmap);
        } catch (FileNotFoundException e) {
            Log.d(TAG, uri + " not find exception");
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                }catch (IOException e) {
                    Log.d(TAG, "fos close e:" + e);
                }
            }
        }
        if (bitmap != null) {
            mMyIconBitmap = bitmap;
            //bitmap.recycle();
            //bitmap = null;
        }
        return mMyIconBitmap;
    }

    public static void postHeadImage(Context context, Bitmap bitmap) {
        String Base64StrData = ImageUtils.bitmapToBase64String(bitmap).replace("+", "%2B");
        String pictureData = "owner=" + UserInfo.getUserPhoneNumber(context) + "&fileName=" +  "head.jpg&" + JsonUtils.POST_TILE + Base64StrData;
        JsonUtils.JsonResultData resultData = JsonUtils.circlePictureHttpPost(pictureData);
        if (resultData.isSuccess()) {
            Log.d("zhangtao", "post head image successful");
        }
    }

    public static File getMyIcon(Context context) {
        File file = new File(context.getFilesDir().getAbsolutePath() + MY_ICON);
        if (file != null && file.exists()) {
            Log.d(TAG, "getMyIcon:" + file.getAbsolutePath());
            return file;
        }
        return null;
    }

    public static Bitmap getMyIconBitmap(Context context) {
        if (mMyIconBitmap != null) {
            return mMyIconBitmap;
        }

        File file = new File(context.getFilesDir().getAbsolutePath() + MY_ICON);
        if (file != null && file.exists()) {
            Log.d(TAG, "getMyIcon:" + file.getAbsolutePath());
            mMyIconBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            return mMyIconBitmap;
        }
        return null;
    }

    public static Bitmap getBitmapFormUri(Context context, Uri uri) throws FileNotFoundException, IOException {
        InputStream input = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1))
            return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return ImageCompress(bitmap);//再进行质量压缩
    }

    public static  Bitmap readBitmapByPath(String path)   {
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inDither = false;
        bfOptions.inPurgeable = true;
        bfOptions.inInputShareable = true;
        bfOptions.inTempStorage = new byte[32 * 1024];

        File file = new File(path);
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            if(fs != null)
                return BitmapFactory.decodeFileDescriptor(fs.getFD(), null, bfOptions);
        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            if(fs != null) {
                try {
                    fs.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap,int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    public static String bitmapToBase64String(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static String bitmapToBase64String(Context context, String filePath) {
            //Bitmap bm = readBitmapByPath(filePath);
            Bitmap bm = BitmapFactory.decodeFile(filePath);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
            byte[] b = baos.toByteArray();
            return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static String bytesToBase64String(byte[] value) {
        return Base64.encodeToString(value, Base64.DEFAULT);
    }

    private static Bitmap ImageCompress(Bitmap bitmap) {
        // 图片允许最大空间 单位：KB
        double maxSize = 300.00;
        // 将bitmap放至数组中，意在bitmap的大小（与实际读取的原文件要大）
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        // 将字节换成KB
        double mid = b.length / 1024;
        // 判断bitmap占用空间是否大于允许最大空间 如果大于则压缩 小于则不压缩
        if (mid > maxSize) {
            // 获取bitmap大小 是允许最大大小的多少倍
            double i = mid / maxSize;
            // 开始压缩 此处用到平方根 将宽带和高度压缩掉对应的平方根倍
            bitmap = zoomImage(bitmap, bitmap.getWidth() / Math.sqrt(i),
                    bitmap.getHeight() / Math.sqrt(i));
        }
        return bitmap;
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }
}
