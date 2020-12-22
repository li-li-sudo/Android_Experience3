package com.example.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public String status; //请求的状态，ok表示成功，失败返回具体原因
    public Basic basic;//城市的基本信息
    public AQI aqi; //当前空气质量状况
    public Now now; //当前天气信息
    public Suggestion suggestion;   //生活建议
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;//天气预报
}
