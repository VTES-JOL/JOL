package deckserver.rich;

import java.util.Date;

public class ChatModel {

    private static int BUFFER_SIZE = 100;
    private String[] queue = new String[BUFFER_SIZE];
    private int idx = 0;
    private Date timestamp = new Date();

    public synchronized void addMessage(String msg) {
        int index = idx % BUFFER_SIZE;
        queue[index] = msg;
        idx++;
        timestamp = new Date();
    }

    public long getTimestamp() {
        return timestamp.getTime();
    }

    public int getIndex() {
        return idx;
    }

    public int getFirstIndex() {
        return Math.max(0, idx - BUFFER_SIZE + 1);
    }

    public synchronized String[] getMessagesForIndexes(int i0, int i1) {
        int sz = i1 - i0;
        if (sz > 100) {
            sz = 100;
            i0 = i1 - 99;
        }
        String[] ret = new String[sz];
        int begin = i0 % BUFFER_SIZE;
        for (int idx = 0; idx < ret.length; idx++) {
            ret[idx] = queue[begin++];
            begin %= BUFFER_SIZE;
        }
        return ret;
    }

}

