package com.sgkj.jsoncyf.mvccoolweather.been;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by JsonCyf on 2018/5/9.
 */

public class Weather {
    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;

}
