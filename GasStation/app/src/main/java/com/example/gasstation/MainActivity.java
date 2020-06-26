package com.example.gasstation;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.icu.text.IDNA;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import android.view.inputmethod.EditorInfo;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.geometry.Tm128;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.NaverMapOptions;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {
    private static final int ACCESS_LOCATION_PERMISSON_REQUEST_COME = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private InfoWindow infoWindow = new InfoWindow();

    private ArrayList<Oil> stations = new ArrayList<Oil>(); //주유소 현황
    private Map<Marker, Oil> map = new HashMap<Marker, Oil>();//마커, 주유소 매칭시키기위한 맵

    double la, lo;

    ArrayList<Marker> markers = new ArrayList<Marker>(); //마커 저장


    Location location;

    //버튼
    private ImageButton searchButton;
    private ImageButton resetButton;
    //검색창
    private EditText edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        onLocationChanged(location);

        //버튼 위치 설정
        searchButton = (ImageButton) findViewById(R.id.searchButton);
        resetButton = (ImageButton) findViewById(R.id.resetButton);
        edit = (EditText) findViewById(R.id.serachEdit);

        //버튼 동작 설정
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ActivitySearch 실행
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                String str = edit.getText().toString();
                intent.putExtra("str", str);
                startActivity(intent);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //리셋 버튼 동작
                onLocationChanged(location);
                onMapReady(naverMap);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
         switch (requestCode) {
             case ACCESS_LOCATION_PERMISSON_REQUEST_COME:
                 locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults);
                 return;
         }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;
        locationSource = new FusedLocationSource(this, ACCESS_LOCATION_PERMISSON_REQUEST_COME);
        naverMap.setLocationSource(locationSource);
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(true);

        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

          infoWindow = new InfoWindow();
        try{
            OilApi api = new OilApi(this);
            Thread searchThread = new Thread() {
                @Override
                public void run() {
                    stations = api.oilSearchRadius(la, lo); // 주유소 정보 리스트 받아오기 37.8253, 127.5165
                }
            };
            searchThread.start();
            searchThread.join();



            for(int i=0;i<stations.size();i++) {
                LatLng lat = new LatLng(stations.get(i).getX(), stations.get(i).getY());
                Marker marker = new Marker();
                marker.setWidth(50);//마커 가로길이
                marker.setHeight(50);//마커 세로길이
                marker.setCaptionText(stations.get(i).getTitle());
                marker.setSubCaptionText("휘발유:" + stations.get(i).getH1() + ", 경유:" + stations.get(i).getG());
                if(!stations.get(i).getRegion()){ //지역화폐가맹점이 아닌 경우 색깔변경
                    marker.setIcon(MarkerIcons.BLACK);//마커색 변경
                    marker.setIconTintColor(Color.RED);
                }
                marker.setPosition(lat);//마커의 위치설정
                marker.setMap(naverMap);//마커르 지도에 표시
                markers.add(marker);


                //마커 클릭시 발생하는 이벤트
                marker.setOnClickListener( overlay -> {

                    Oil oil = map.get(marker);
                    Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                    intent.putExtra("oil", oil);
                    startActivity(intent);

                    return true;
                });

                map.put(marker, stations.get(i)); //현재 마커에 주유소 정보 맵핑
            }

        } catch(NullPointerException e) { e.printStackTrace();} catch(InterruptedException e) { e.printStackTrace();}


        //마커 위에 텍스트를 표시하는 것
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(MainActivity.this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                // 정보 창이 열린 마커의 tag를 텍스트로 노출하도록 반환
                return (CharSequence)infoWindow.getMarker().getTag();
            }
        });



    }


    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        la = location.getLatitude();
        lo = location.getLongitude();

        //현재 모든 자료 초기화
        stations.clear();
        markers.clear();
        map.clear();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void markerSet() {

    }

}