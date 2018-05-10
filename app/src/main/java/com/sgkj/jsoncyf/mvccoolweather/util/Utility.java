package com.sgkj.jsoncyf.mvccoolweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.orhanobut.logger.Logger;
import com.sgkj.jsoncyf.mvccoolweather.been.Weather;
import com.sgkj.jsoncyf.mvccoolweather.db.City;
import com.sgkj.jsoncyf.mvccoolweather.db.County;
import com.sgkj.jsoncyf.mvccoolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by JsonCyf on 2018/5/9.
 */

public class Utility {
    /***
     * 解析和处理服务器返回的省级数据
     */
    public static boolean handleProvinceResponse(String reponse){
        if (!TextUtils.isEmpty(reponse)){
            try {
                JSONArray allProvinces=new JSONArray(reponse);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.setProvinceName(provinceObject.getString("name"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     * */

    public static boolean handleCityResponse(String response,int provinceId){

        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray  allCitys=new JSONArray(response);
                for (int i = 0; i < allCitys.length(); i++) {
                    JSONObject cityObject = allCitys.getJSONObject(i);
                    City city=new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     * */
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try {
                JSONArray allCounties=new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *将返回的Json数据解析成Weather实体类
     * */
    public static Weather handlerWeatherResponse(String response){
        try {
            Log.e("Weatherresponse",response);
            JSONObject jsonObject=new JSONObject(response);
            JSONArray heWeather = jsonObject.getJSONArray("HeWeather");
            String weatherContent = heWeather.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }












}
