/*
 * Shuffle.java
 *
 * Created on November 10, 2004, 2:01 PM
 */

package util;

/**
 *
 * @author  gfinklan
 */
public class Shuffle extends DSRandom {
    
    public static Object[] shuffle(Object[] arr, int num) {
        if(num <= 0 || num > arr.length) num = arr.length;
        for(int i = num; i != 0; i--) {
            int index = getNumber(i);
            if(index == i) index = 0;
            Object tmp = arr[i - 1];
            arr[i - 1] = arr[index];
            arr[index] = tmp;
        }
        return arr;        
    }
    
    public static Object[] shuffle(Object[] arr) {
        return shuffle(arr,0);
    }
}
