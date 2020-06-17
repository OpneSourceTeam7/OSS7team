package com.example.testapp1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.util.FusedLocationSource;


public class ActivityGPS extends AppCompatActivity implements OnMapReadyCallback{
 private static  final  int ACCESS_LOCATION_PERMISSON_REQUEST_COME =100;
 private FusedLocationSource locationSource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g_p_s);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }






    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
         locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSON_REQUEST_COME);
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

    }

    public void onRequestPermissonsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case ACCESS_LOCATION_PERMISSON_REQUEST_COME:
                locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }
    }

}