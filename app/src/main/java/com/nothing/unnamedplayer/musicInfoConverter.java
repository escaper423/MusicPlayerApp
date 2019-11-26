package com.nothing.unnamedplayer;

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
}
