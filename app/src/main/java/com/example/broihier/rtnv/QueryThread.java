package com.example.broihier.rtnv;

import android.net.sip.SipAudioCall;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by broihier on 11/13/16.
 */

public class QueryThread <Token> extends HandlerThread {
    UpdateQuotes updater;
    Handler mHandler;
    Handler mResponseHandler;

    public interface Listener<Token> {
        void onQueryComplete(Token token, String value);
    }

    Listener<Token> mListener;

    public void setListener(Listener<Token> listener) {
        mListener = listener;
    }
    Map<Token,String> results = Collections.synchronizedMap(new HashMap<Token,String>());

    private static String TAG = "rtnv";
    public QueryThread(Handler responseHandler) {
        super(TAG);
        updater = new UpdateQuotes();
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mHandler = new Handler() {
            public void handleMessage(Message message) {
                Token token = (Token)message.obj;
                Log.d(TAG,"Got request for: "+message);
                handleRequest(token);
            }
        };
    }

    public void queueQuery (Token token, String ticker) {
        results.put(token,ticker);
        mHandler.obtainMessage(1,ticker).sendToTarget();
    }
    private void handleRequest(final Token message){

        updater.setTicker(results.get(message));
        Log.d(TAG,"getting a value for Ticker "+results.get(message));
        updater.get();
        Log.d(TAG,"storing the value received: "+updater.getResult());
        results.put(message,updater.getResult());

        mResponseHandler.post(new Runnable() {
            public void run () {
                Log.d("rtnv","deleting the result for "+message.toString()+" from map");
                String value;
                value = results.get(message);
                if (value == null) {
                    value = "Not found";
                }
                mListener.onQueryComplete(message,value);
                results.remove(message);
            }
        });
    }

    @Override
    public void run() {
        super.run();
        Log.d(TAG,"background thread running");
    }
}
