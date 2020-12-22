package com.example.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;
/*现在的天气状况*/
public class Now {
    @SerializedName("tmp")  //当前温度
    public String temperature;

    @SerializedName("cond") //当前天气状况
    public More mroe;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
