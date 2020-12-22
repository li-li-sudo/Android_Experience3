package com.example.mycoolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends AppCompatActivity {
    private Button search_btn;
    private ListView listView = null;
    List<String> list = new ArrayList();   //存放列表的信息
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        search_btn = (Button) findViewById(R.id.search_button);
        listView = (ListView) findViewById(R.id.list_collect);
        /*展示所有收藏的城市*/
        showAll();
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(IndexActivity.this,ChooseAreaFragment.class));
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent intent=new Intent(IndexActivity.this,WeatherActivity.class);
                intent.putExtra("id",String.valueOf(id));
                startActivity(intent);//启动第二个activity并把i传递过去
            }
        });
    }
    /*展示所有收藏的城市*/
    public void showAll(){
        list = new ArrayList();
        final  DatabaseHelper dbHelper = new DatabaseHelper(IndexActivity.this, "MyCity.db",null,3);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        /*query table：表名，columns：列名 selection：where的约束条件    groupby：需要group by的列    having  对group by结果进一步约束 orderBy 查询结果排列方式*/
        Cursor cursor = db.query("city",null,null,null,null,null,null);
        if (cursor.moveToFirst()){
            do{
                String cityname = cursor.getString(cursor.getColumnIndex("cityname"));
                list.add(cityname);
            }while(cursor.moveToNext());
        }
        cursor.close();
        final ArrayAdapter<String> adapter = new ArrayAdapter(
                IndexActivity.this,android.R.layout.simple_list_item_1,list);
        listView =  (ListView)findViewById(R.id.list_item);
        listView.setAdapter(adapter);
    }
}
