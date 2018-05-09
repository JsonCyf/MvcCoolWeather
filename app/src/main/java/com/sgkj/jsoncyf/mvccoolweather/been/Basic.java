package com.sgkj.jsoncyf.mvccoolweather.been;

import com.google.gson.annotations.SerializedName;

/**
 * Created by JsonCyf on 2018/5/9.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;

    @SerializedName("id")
    public String weatherId;

    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
