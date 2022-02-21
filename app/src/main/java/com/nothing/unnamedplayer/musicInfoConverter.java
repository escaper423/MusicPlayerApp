package com.nothing.unnamedplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

public class musicInfoConverter {

    public musicInfoConverter(){
    }

    public static int getProgressFormSpeedMult(float mult){
        return (int)((mult * 100f) - 25f);
    }

    public static float getSpeedMultFromProgress(int progress){
        return roundWithDecimalPoints((float)progress/100.0f + 0.25f , 2);
    }

    public static float roundWithDecimalPoints(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static String durationConvert(int duration){
        int sec = duration / 1000;
        int min = sec / 60;
        int hr = min / 60;
        String res = "";
        sec = sec % 60;
        //hours string
        if (hr > 0)
            res += hr + ":";
        //Minutes string. nothing to do.
        res += min;

        //seconds string
        if (sec < 10) {
            res += ":0" + sec;
        }
        else{
            res +=":" + sec;
        }
        return res;
    }

    public static String getStringFromBitmap(Bitmap bitmap){
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        byte[] b;
        ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                byteArrayBitmapStream);
        b = byteArrayBitmapStream.toByteArray();

        encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encodedImage;
    }

    public static Bitmap getBitmapFromString(String stringPicture) {
        byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }
}
