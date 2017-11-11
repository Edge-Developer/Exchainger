package com.exchainger.exchainger;

/**
 * Created by OPEYEMI OLORUNLEKE on 9/25/2017.
 */

public class Singleton {
    private static Singleton sSingleton;
    private int count = 0;

    private Singleton() {
    }


    public static Singleton getInstance() {
        if (sSingleton == null) {
            sSingleton = new Singleton();
        }
        return sSingleton;
    }

    public boolean showAd() {
        boolean b = (count % 10 == 0);
        count++;
        return b;
    }
}
