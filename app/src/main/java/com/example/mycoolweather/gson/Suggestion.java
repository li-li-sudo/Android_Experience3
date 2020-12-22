package com.example.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Suggestion {
    @SerializedName("comf")     //当前舒适度
    public Comfort comfort;

    @SerializedName("cw")       //洗车建议
    public CarWash carWash;
    public Sport sport;         //运动建议
    public class Comfort{
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{
        @SerializedName("txt")
        public String info;
    }
    public class Sport{
        @SerializedName("txt")
        public String info;
    }
}
