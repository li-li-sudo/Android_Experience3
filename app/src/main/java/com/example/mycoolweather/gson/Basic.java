package com.example.mycoolweather.gson;

import com.google.gson.annotations.SerializedName;
/*基本信息*/
/*@SerializedName（）：注解方式使JSON字段与Java字段之间建立映射关系*/
public class Basic {
    @SerializedName("city")
    public String cityName;     //城市名

    @SerializedName("parent_city")
    public String parentCity;

    @SerializedName("admin_area")
    public String adminArea;

    @SerializedName("id")
    public String weatherId;      //城市的天气id

    public Update update;

    public class  Update{
        @SerializedName("loc")
        public String updateTime;   //更新时间
    }
}
