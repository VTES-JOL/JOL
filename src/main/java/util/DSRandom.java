/*
 * Shuffle.java
 *
 * Created on November 10, 2004, 2:01 PM
 */

package util;

import java.util.Random;

/**
 * @author gfinklan
 */
public class DSRandom {

    public static Random random = new Random();

    public static int getNumber(int index) {
        float rnum = random.nextFloat();
        rnum = rnum * index;
        return Math.round(rnum);

    }

    public static Object[] shuffle(Object[] arr) {
        for (int i = arr.length; i != 0; i--) {
            int index = getNumber(i);
            if (index == i) index = 0;
            Object tmp = arr[i - 1];
            arr[i - 1] = arr[index];
            arr[index] = tmp;
        }
        return arr;
    }

}
