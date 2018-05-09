package com.sgkj.jsoncyf.mvccoolweather.been;

import com.google.gson.annotations.SerializedName;

/**
 * Created by JsonCyf on 2018/5/9.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond")
    public More more;

    public class More {
        @SerializedName("txt")
        public String info;
    }
}
