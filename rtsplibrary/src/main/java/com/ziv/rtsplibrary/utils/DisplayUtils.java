package com.ziv.rtsplibrary.utils;

public class DisplayUtils {
    public static int align(int d, int a) {
        return (((d) + (a - 1)) & ~(a - 1));
    }
}
