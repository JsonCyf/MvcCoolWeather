package com.sgkj.jsoncyf.mvccoolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.sgkj.jsoncyf.mvccoolweather.activity.WeatherActivity;
import com.sgkj.jsoncyf.mvccoolweather.been.Weather;
import com.sgkj.jsoncyf.mvccoolweather.util.HttpUtil;
import com.sgkj.jsoncyf.mvccoolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        updateWeather();
        updateBingPic();

        AlarmManager manager= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;//这是8小时的毫秒数
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);

        return super.onStartCommand(intent, flags, startId);
    }
    //更新背景图片
    private void updateBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }
    //更新天气
    private void updateWeather() {
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (!TextUtils.isEmpty(weatherString)){
            //有缓存时直接解析天气数据
            Weather weather = Utility.handlerWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weaherUrl="http:guolin.tech/api/weather?cityid="+weatherId+"&key=c695e75518ee4e5e865e23ef2d2dae98";
            HttpUtil.sendOkhttpRequest(weaherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseString = response.body().string();
                    Weather weatherNow = Utility.handlerWeatherResponse(responseString);
                    if (weatherNow!=null&&TextUtils.equals("ok",weatherNow.status)){
                        SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather",responseString);
                        editor.apply();
                    }
                }
            });
        }
    }
}
