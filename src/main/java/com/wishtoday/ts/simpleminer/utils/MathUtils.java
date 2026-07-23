package com.wishtoday.ts.simpleminer.utils;

public class MathUtils {
    public static long fastDivide(int x, int y) {
        if (x < 0 || y <= 0)
            throw new IllegalArgumentException("x and y must be positive. y must be greater than 0");
        int a;
        int b;
        /*if (y == 64) {
            a = x >> 6;
            b = x & 63;
        } else if (y == 16) {
            a = x >> 4;
            b = x & 15;
        } else if (y == 1) {
            a = x;
            b = 0;
        } else {
            a = x / y;
            b = x % y;
        }*/
        if ((y & (y - 1)) == 0) {
            int shift = Integer.numberOfTrailingZeros(y);
            a = x >> shift;
            b = x & (y - 1);
        } else {
            a = x / y;
            b = x % y;
        }
        return ((long) a) << 32 | (long) b;
    }
}
