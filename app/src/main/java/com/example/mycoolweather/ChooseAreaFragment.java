package com.example.mycoolweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.example.mycoolweather.db.*;
import com.example.mycoolweather.util.*;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;  //进度对话框
    private TextView titleText;
    private Button backButton;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();    //显示的数据

    private List<Province> provinceList;    //省列表
    private List<City> cityList;    //市列表
    private List<County> countyList;    //县列表

    private Province selectedProvince;  //选中的省份
    private City selectedCity;  //选中的城市

    private int currentLevel;   //当前选中的级别

    @Override
    /*onCreateView：
    * 1.获取控件实例
    * 2.初始化ArrayAdapter，并将其设置为ListView的适配器*/
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = (TextView)view.findViewById(R.id.title_text);
        backButton = (Button)view.findViewById(R.id.back_button);
        listView = (ListView)view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }
    /*onActivityCreated：
    * 给ListView和Button设置点击事件*/
    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*listView点击事件*/
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /*依据当前级别判断调用查询市级还是县级数据*/
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*当前省级，查询市级*/
                if(currentLevel == LEVEL_PROVINCE){
                    selectedProvince = provinceList.get(position);
                    queryCities();
                /*当前市级，查询省级*/
                }
                else if (currentLevel == LEVEL_CITY){
                    selectedCity=cityList.get(position);
                    queryCounties();
                }
                /*当前县级，启动WeatherActivity，并传入当前选中县的天气id*/
                else if(currentLevel == LEVEL_COUNTY) {
                    String weatherId = countyList.get(position).getWeatherId();
                    if (getActivity() instanceof MainActivity) {    //instanceof判断一个对象是否属于某个类的实例
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                    else if (getActivity() instanceof WeatherActivity){
                        WeatherActivity activity = (WeatherActivity)getActivity();
                        activity.drawerLayout.closeDrawers();   //关闭滑动菜单
                        activity.swipeRefresh.setRefreshing(true);  //显示下拉刷新进度条
                        activity.requestWeather(weatherId); //请求新城市的天气信息
                    }
                }
            }
        });
        /*返回按钮点击事件
        * 当前县级，返回市级
        * 当前市级，返回县级
        * 当前省级，不可返回，返回按钮会自动隐藏*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }
    /*查询全国所有的省，优先从数据库查询，如果没有查询到再去服务器上查询 */
    private void queryProvinces(){
        titleText.setText("中国");    //标题为中国
        backButton.setVisibility(View.GONE);    //隐藏返回按钮（省级列表不能再返回）
        provinceList = LitePal.findAll(Province.class);//读取省级数据
        /*数据库中查询，若有则直接显示在界面上*/
        if(provinceList.size() > 0){
            dataList.clear();
            for(Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        /*若无，则从服务器中查询*/
        else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }
    /*查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询*/
    private void queryCities(){
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList= LitePal.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        /*数据库中查询*/
        if(cityList.size()>0){
            dataList.clear();
            for(City city:cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        /*服务器中查询*/
        else {
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }
    }
    /* 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询*/
    private void queryCounties(){
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList=LitePal.where("cityid=?",String.valueOf(selectedCity.getId())).find(County.class);
        /*数据库中查询*/
        if(countyList.size()>0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }
        /*服务器中查询*/
        else {
            int provinceCode=selectedProvince.getProvinceCode();
            int cityCode=selectedCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+cityCode;
            queryFromServer(address,"county");
        }
    }
    /* 根据传入的地址和类型从服务器上查询省市县数据
    * address：服务器的url
    * type：当前级别，省，市或县*/
    private void queryFromServer(String address,final String type){
        showProgressDialog();
        /* 调用HttpUtil的sendOkHttpRequest方法向服务器发送请求，
        * 响应的数据回调到onResponse（）方法中
        * 调用Utility的handleProvinceResponse（）方法解析处理服务器返回的数据，并存储到数据库中
        * 再次调用queryProvince（）方法*/
        HttpUtil.sendOkHttpRequest(address, new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                /**从服务器获取数据**/
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                /*重新加载显示数据*/
                if(result){
                    /* query方法涉及到UI操作，需在主线程中调用
                    * runOnUiThread（）：实现从子线程切换到主线程*/
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //通过runOnUiThread()方法回到主线程处理逻辑
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    /* 显示进度对话框 */
    private void showProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /* 关闭进度对话框 */
    private void closeProgressDialog(){
        if(progressDialog != null){
            progressDialog.dismiss();
        }
    }
}