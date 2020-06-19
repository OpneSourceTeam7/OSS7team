package com.example.gasstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private Intent intent;
    private OilAdapter adapter;
    private ListView listView;
    private ImageButton searchButton;
    private ImageButton backButton;

    OilApi api = new OilApi(this);
    ArrayList<Oil> items =new ArrayList<Oil>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        intent = getIntent();

        adapter = new OilAdapter();
        listView = (ListView) findViewById(R.id.listview1);
        searchButton = (ImageButton)findViewById(R.id.searchButton);
        backButton = (ImageButton)findViewById(R.id.backButton);

        listView.setAdapter(adapter);

        String str = intent.getStringExtra("str");
        adapter.removeItemAll();

        new Thread(){
            @Override
            public void run() {
                items = api.oilSearchName(str);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData();
                    }
                });
            }
        }.start();

        //리스트에 있는 주유소를 클릭 했을 시
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // list의 item을 클릭했을 시 ActivityMap을 불러옴과 동시에 값 전달
                Oil oil = ((Oil)adapter.getItem(position));

                // new Intent(Intent를 통하여 액티비티 변경)
                Intent intent = new Intent(SearchActivity.this, InfoActivity.class);
                // putExtra(key, value)
                intent.putExtra("oil", oil);
                startActivity(intent);
            }
        });

        //버튼 동작 설정
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = intent.getStringExtra("str");
                items.clear();
                adapter.removeItemAll();


                new Thread(){
                    @Override
                    public void run() {
                        items = api.oilSearchName(str);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setData();
                            }
                        });
                    }
                }.start();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    private void setData() { //리스트 업데이트 메소드
        for (int i = 0; i < items.size(); i++) {
            adapter.addItem(items.get(i));
            adapter.notifyDataSetChanged();
        }
    }


}