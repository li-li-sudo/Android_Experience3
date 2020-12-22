package com.example.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;
/*天气预报*/
public class Forecast {
    public String date;     //日期 年月日

    @SerializedName("tmp")  //温度
    public Temperature temperature;

    @SerializedName("cond") //天气状况
    public More more;
    public class Temperature{
        public String max;  //最高温度
        public String min;  //最低温度
    }
    public class More{
        @SerializedName("txt_d")
        public String info;
    }

}
