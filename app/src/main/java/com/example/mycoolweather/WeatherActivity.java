package com.example.mycoolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import com.bumptech.glide.Glide;
import com.example.mycoolweather.util.*;
import com.example.mycoolweather.gson.*;
import com.example.mycoolweather.service.*;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;       //滑动菜单布局

    private Button navButton;

    public SwipeRefreshLayout swipeRefresh;     //下拉刷新
    private String mWeatherId;      //城市的天气id

    private ScrollView weatherLayout;
    private TextView titleCity; //城市
    private TextView titleUpdateTime;   //更新时间
    private TextView degreeText;        //温度
    private TextView weatherInfoText;   //天气情况
    private LinearLayout forecastLayout;    //天气预报
    private TextView aqiText;   //空气质量指数
    private TextView pm25Text;  //PM2.5
    private TextView comfortText;   //舒适度
    private TextView carWashText;   //洗车建议
    private TextView sportText;     //运动建议
    private TextView qltyText;      //空气质量

    private RadioButton collectButton;
    private Button searchButton;    //选择城市按钮


    private ImageView bingPicImg;   //背景图片
    /* 1.获取控件实例
    *  2.尝试从本地读取天气数据
    *  3.本地无数据时，从Intent中取出天气Id，调用requestWeather（）方法从服务器请求天气数据
    * （请求数据时隐藏ScrollView）*/
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        /*滑动菜单*/
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navButton = (Button) findViewById(R.id.nav_button);
        collectButton = (RadioButton) findViewById(R.id.collect_button);
        searchButton = (Button) findViewById(R.id.search_button);

        /*Button点击事件，调用DrawerLayout的openDrawer（）方法打开滑动菜单*/
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WeatherActivity.this,IndexActivity.class));
            }
        });
        /*实现状态栏与背景图融合到一起
        由于本功能是Android5.0以上系统才支持，所以在版本号大于或等于21时，才嫩实现*/
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            /*全屏，状态栏会盖在布局上*/
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏设置为透明色
        }


        /*初始化各控件*/
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = (TextView) findViewById(R.id.degree_text);
        weatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        aqiText = (TextView) findViewById(R.id.aqi_text);
        qltyText = (TextView) findViewById(R.id.qlty_text);
        pm25Text = (TextView) findViewById(R.id.pm25_text);
        comfortText = (TextView) findViewById(R.id.comfort_text);
        carWashText = (TextView) findViewById(R.id.car_wash_text);
        sportText = (TextView) findViewById(R.id.sport_text);

        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);



        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);     //设置下拉刷新进度条的颜色为colorPrimary

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String weatherString = prefs.getString("weather",null);

        if (weatherString != null){
            /*有缓存时直接解析天气数据*/
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }
        else {
            /*无缓存时去服务器查询天气*/
            mWeatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);       //设置加载过程中不可见
            requestWeather(mWeatherId);
        }
        /*下拉监听器*/
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                String weatherString = prefs.getString("weather",null);
                Weather weather = Utility.handleWeatherResponse(weatherString);
                mWeatherId = weather.basic.weatherId;
                requestWeather(mWeatherId);
            }
        });

        /***从SharedPreferences中读取缓存的背景图片，
         * 如果存在，则使用Glide加载图片，
         * 如果不存在，则调用loadBingPic()方法请求背景图片***/
        String bingPic=prefs.getString("bing_pic",null);
        if(bingPic!=null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }
    }
    /* 根据天气id请求城市天气信息
    * 无缓存时调用此方法*/
    public void requestWeather(final String weatherId){
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=1b4098f7d92b48cc8e87ee122ffc3fa9";   //接口地址
        /***向接口地址发出请求
         * 服务器将对应城市的天气信息一JSON格式返回
         * 在onResponse（）回调中调用Utility.handleWeatherResponse（）方法将返回的JSON数据转换成Weather对象
         * 将线程切换到主线程
         * 判断status状态，ok则天气请求成功，返回数据缓存到SharedPreferences中
         * 调用shoeWeatherInfo（）方法进行内容显示***/
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText=response.body().string();     //对应城市的天气信息（JSON格式）
                final Weather weather=Utility.handleWeatherResponse(responseText);  //JSON数据转换成Weather对象
                runOnUiThread(new Runnable() {  //将线程切换到主线程
                    @Override
                    public void run() {
                        /**天气请求成功，返回数据缓存到SharedPreferences中，调用shoeWeatherInfo（）方法进行内容显示**/
                        if(weather!=null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                        else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                        loadBingPic();  //每次请求天气信息时刷新背景图片
                    }
                });
            }
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);  //请求结束，调用swipeRefresh.setRefreshing（）方法，表示刷新事件结束，隐藏刷新进度条
                        loadBingPic();  //每次请求天气信息时刷新背景图片
                    }
                });
            }
        });

    }
    /*处理并展示Weather实体类中的数据  本地有缓存时使用
    * 从Weather对象获取数据，显示到相应的控件上*/
    private void showWeatherInfo(Weather weather){
        String provinceName = weather.basic.adminArea;
        String parentCityName = weather.basic.parentCity;
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature+"℃";
        String weatherInfo = weather.now.mroe.info;

        if(provinceName.equals(parentCityName) && parentCityName.equals(cityName))
            titleCity.setText(cityName);
        else if(provinceName.equals(parentCityName) && !parentCityName.equals(cityName))
            titleCity.setText(parentCityName +" " + cityName);
        else if(!provinceName.equals(parentCityName) && !parentCityName.equals(cityName))
            titleCity.setText(provinceName + " " + parentCityName + " "+ cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        /**动态加载forecast_item布局并设置相应的数据，并添加到父布局中**/
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText=view.findViewById(R.id.date_text);
            TextView infoText=view.findViewById(R.id.info_text);
            TextView maxText=view.findViewById(R.id.max_text);
            TextView minText=view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max+"℃");
            minText.setText(forecast.temperature.min+"℃");
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            String qlty = weather.aqi.city.qlty;
//            if("优".equals(qlty)){
//                qltyText.setTextColor(Color.GREEN);
//            }
//            else if("良".equals(qlty)){
//                qltyText.setTextColor(Color.YELLOW);
//            }
//            else if("轻度污染".equals(qlty)){
//                qltyText.setTextColor(Color.parseColor("#FFD700"));
//            }
//            else if("中度污染".equals(qlty)){
//                qltyText.setTextColor(Color.RED);
//            }
//            else if("重度污染".equals(qlty)){
//                qltyText.setTextColor(Color.parseColor("#8B1C62"));
//            }
//            else if("严重污染".equals(qlty)){
//                qltyText.setTextColor(Color.parseColor("#8B2323"));
//            }

            qltyText.setText(qlty);
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动建议："+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        weatherLayout.setVisibility(View.VISIBLE);  //可见
        /**激活AutoUpdateService服务**/
        Intent intent=new Intent(this,AutoUpdateService.class);
        startService(intent);
    }
    /* 加载每日一图*/
    private void loadBingPic(){
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        /**获取背景图的链接
         * 将链接缓存到SharedPreferences中
         * 将线程切换到主线程
         * 使用Glide加载图片**/
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();    //背景图链接
                /*缓存背景图链接*/
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {//线程切换到主线程
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);//使用Glide加载图片
                    }
                });
            }
        });
    }
}
