package com.example.mycoolweather.gson;

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;  //空气质量指数
        public String pm25; //Pm2.5
        public String qlty;
    }
}
