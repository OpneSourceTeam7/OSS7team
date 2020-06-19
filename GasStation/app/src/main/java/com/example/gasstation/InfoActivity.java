package com.example.gasstation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import android.util.Log;

public class InfoActivity extends AppCompatActivity {

    private Intent intent;
    private ImageButton searchButton;
    private ImageButton backButton;

    private TextView title;
    private TextView h;//휘발유
    private TextView g;//경유
    private TextView addr;//주소

    OilApi api = new OilApi(this);
    Oil item= new Oil();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        searchButton = (ImageButton)findViewById(R.id.searchButton);
        backButton = (ImageButton)findViewById(R.id.backButton);

        title = (TextView)findViewById((R.id.titleT));
        h = (TextView)findViewById((R.id.gasolineInfo));
        g = (TextView)findViewById((R.id.dieselInfo));
        addr = (TextView)findViewById((R.id.addrInfo2));

        //String str = intent.getStringExtra("str");
        intent = getIntent();

        item = (Oil) intent.getSerializableExtra("oil");
        Log.d("intentText=", item.getTitle());

        try {
            Thread searchThread = new Thread() {
                @Override
                public void run() {
                    item = api.detailGas(item.getCode()); // 주유소 정보 리스트 받아오기 37.8253, 127.5165
                }
            };
            searchThread.start();
            searchThread.join();

        } catch(InterruptedException e){e.getStackTrace();}

        //상세정보 설정
        title.setText(item.getTitle());
        h.setText(item.getH1() + "원");
        g.setText(item.getG()+"원");
        addr.setText(item.getAdress());

    }


}