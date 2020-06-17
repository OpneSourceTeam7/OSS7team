package com.example.testapp1;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;


public class ActivityMap extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "mapTest";

    private Intent intent;

    InfoWindow infoWindow = new InfoWindow();

    ArrayList<Oil> oilStation;

    NaverMap naverMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        intent = getIntent();
        //네이버맵 프레그멘트 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment) fm.findFragmentById(R.id.map);

        //오피넷 데이터에선 tm128좌표를 사용해서 네이버맵에 표시하려면 LatLng값으로 바꿔줘야함
        Tm128 tm = new Tm128(Double.parseDouble(intent.getStringExtra("x")), Double.parseDouble(intent.getStringExtra("y")));
        LatLng latLng = tm.toLatLng();

        //네이버맵 시작시 기본설정들
        if (mapFragment == null) {

            NaverMapOptions options = new NaverMapOptions()
                    .camera(new CameraPosition(latLng, 15));

            mapFragment = MapFragment.newInstance(options);
            fm.beginTransaction().add(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);


    }

    @UiThread
    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        // ...
        this.naverMap = naverMap;
        oilStation = (ArrayList<Oil>) intent.getSerializableExtra("oil"); // 주유소 정보 리스트 받아오기

        ArrayList<Marker> markers = new ArrayList<Marker>();
        //검색한 주유소들의 좌표값을 이용하여 지도에 모든 주유소 마커 표시
        for(int i=0;i<oilStation.size();i++){
            Tm128 tm = new Tm128(Double.parseDouble(oilStation.get(i).getX()), Double.parseDouble(oilStation.get(i).getY()));
            LatLng lat = tm.toLatLng();
            Marker marker = new Marker();
            marker.setTag(oilStation.get(i).getTitle()); // 마커의 tag에 주유소 상호명 저장
            marker.setPosition(lat);
            marker.setMap(naverMap);
            String adress = oilStation.get(i).getAdress();
            if(oilStation.get(i).getTitle().equals(intent.getStringExtra("title"))){
                infoWindow.open(marker);
            }

            markers.add(marker);

            //마커 클릭시 발생하는 이벤트
            marker.setOnClickListener( overlay -> {
                //마커 클릭 시 주유소의 이름을 표시하고 토스트로 화면에 주소 표시
                Toast.makeText(this, adress, Toast.LENGTH_SHORT).show();
                infoWindow.open(marker);
                // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                return true;
            });
     // gps 수정
            //수정


        }

        //마커 위에 텍스트를 표시하는 것
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(ActivityMap.this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                // 정보 창이 열린 마커의 tag를 텍스트로 노출하도록 반환
                return (CharSequence)infoWindow.getMarker().getTag();
            }
        });

    }



    //돌아가기 버튼
    public void clickBack(View view) {
        finish();
    }


    @UiThread
    public void clickBig(View view) {

        Tm128 tm = new Tm128(Double.parseDouble(intent.getStringExtra("x")), Double.parseDouble(intent.getStringExtra("y")));
        LatLng latLng = tm.toLatLng();
        CameraPosition x = new CameraPosition(latLng, 6);
        naverMap.setCameraPosition(x);


    }

}