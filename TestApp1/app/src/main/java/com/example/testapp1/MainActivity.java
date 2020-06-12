package com.example.testapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Toast;



public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    //검색버튼 클릭 시
    public void clickSearch(View view) {
        //ActivitySearch 실행
        Intent intent = new Intent(MainActivity.this, ActivitySearch.class);
        startActivity(intent);
    }

    //종료 버튼 클릭 시
    public void clickBack(View view) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Goodbye~~!" ,Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }



}