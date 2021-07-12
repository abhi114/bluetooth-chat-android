package com.example.bluetooth_chat;

import android.content.Context;

import java.util.logging.Handler;

public class ChatUtils {
    private Context context;
    private final Handler handler;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING =2;
    public static final int STATE_CONNECTED =3;
    private int state;

    public ChatUtils(Context context, Handler handler){
        this.context = context;
        this.handler = handler;
        state = STATE_NONE;

    }
    
}
