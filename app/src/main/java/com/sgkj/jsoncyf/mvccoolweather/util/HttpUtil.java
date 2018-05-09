package com.sgkj.jsoncyf.mvccoolweather.util;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by JsonCyf on 2018/5/9.
 */

public class HttpUtil {
    public static void sendOkhttpRequest(String strUrl, Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(strUrl).build();
        client.newCall(request).enqueue(callback);
    }
}
