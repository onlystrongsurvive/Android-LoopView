package com.kevin.loopview.internal.loopimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by zhouwk on 2015/11/2 0002.
 */
public class LoopImage {

    /**
     * 连接超时时间
     */
    private static final int CONNECT_TIMEOUT = 5000;
    /**
     * 读取时间超时
     */
    private static final int READ_TIMEOUT = 10000;
    private LoopImageView imageView;

    private LoopImageCache imageCache;

    private String url;

    public LoopImage(LoopImageView imageView, String url) {
        this.imageView = imageView;
        this.url = url;
    }

    public Bitmap getBitmap(Context context) {
        if (imageCache == null) {
            imageCache = new LoopImageCache(context);
        }

        // 首先加载缓存中的图片
        Bitmap bitmap = null;
        if (url != null) {
            bitmap = imageCache.get(url);
            if (bitmap == null) {
                bitmap = getBitmapFromUrl(url);
                if (bitmap != null) {
                    imageCache.put(url, bitmap);
                }
            }
        }

        return bitmap;
    }

    private Bitmap getBitmapFromUrl(String url) {
        Bitmap bitmap = null;

        try {
            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            InputStream is = (InputStream) conn.getContent();
            // 图片适配View
            LoopImageUtils.ImageSize actualImageSize = LoopImageUtils.getImageSize(is);
            LoopImageUtils.ImageSize imageViewSize = LoopImageUtils.getImageViewSize(imageView);
            int inSampleSize = LoopImageUtils.calculateInSampleSize(actualImageSize, imageViewSize);
            try {
                is.reset();
            } catch (IOException e) {
                conn = new URL(url).openConnection();
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.setReadTimeout(READ_TIMEOUT);
                is = (InputStream) conn.getContent();
            }
            BitmapFactory.Options ops = new BitmapFactory.Options();
            ops.inJustDecodeBounds = false;
            ops.inSampleSize = inSampleSize;
            bitmap = BitmapFactory.decodeStream(is, null, ops);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public void removeFromCache(String url) {
        if (imageCache != null) {
            imageCache.remove(url);
        }
    }
}
