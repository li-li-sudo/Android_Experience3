package com.example.mycoolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    /*与服务器交互，发起HTTP请求时调用此方法，传入请求地址，注册一个回调，即可处理服务器响应请求*/
    public static void sendOkHttpRequest(String address, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
