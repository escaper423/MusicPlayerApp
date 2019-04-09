package com.example.tutorial;

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
        sec = sec % 60;
        if (sec < 10) {
            return min + ":0" + sec;
        }
        else{
            return min +":" + sec;
        }
    }
}
