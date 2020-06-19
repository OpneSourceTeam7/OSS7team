package com.example.gasstation;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

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
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener{
    private static final int ACCESS_LOCATION_PERMISSON_REQUEST_COME = 100;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private InfoWindow infoWindow = new InfoWindow();

    private ArrayList<Oil> stations=new ArrayList<Oil>(); //주유소 현황

    double la,lo;

    //버튼
    private ImageButton searchButton;
    //검색창
    private EditText edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


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
        Location location = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
        la = location.getLatitude();
        lo = location.getLongitude();

        //onLocationChanged(location);




        //버튼 위치 설정
        searchButton = (ImageButton)findViewById(R.id.searchButton);
        edit = (EditText)findViewById(R.id.serachEdit);

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


        try{
            OilApi api = new OilApi(this);
            Thread searchThread = new Thread() {
                @Override
                public void run() {

                    stations = api.searchGas(0, la, lo, ""); // 주유소 정보 리스트 받아오기 37.8253, 127.5165
                }
            };
            searchThread.start();
            searchThread.join();

            ArrayList<Marker> markers = new ArrayList<Marker>(); //검색한 주유소들의 좌표값을 이용하여 지도에 모든 주유소 마커 표시

            for(int i=0;i<stations.size();i++) {
                //Tm128 tm = new Tm128(Double.parseDouble(oilStation.get(i).getX()), Double.parseDouble(oilStation.get(i).getY()));
                //LatLng lat = tm.toLatLng();
                LatLng lat = new LatLng(stations.get(i).getX(), stations.get(i).getY());
                Marker marker = new Marker();
                marker.setWidth(50);//마커 가로길이
                marker.setHeight(50);//마커 세로길이
                marker.setCaptionText(stations.get(i).getTitle());
                if(stations.get(i).getRegion()){ //지역화폐가맹점인경우 색깔변경
                    marker.setIcon(MarkerIcons.BLACK);//마커색 변경
                    marker.setIconTintColor(Color.RED);
                }
                marker.setTag(stations.get(i).getAdress()); // 마커의 tag를 클릭하면 뜨는 창
                marker.setPosition(lat);//마커의 위치설정
                marker.setMap(naverMap);//마커르 지도에 표시
                //marker.setIcon(OverlayImage.fromResource(R.drawable.icon);//icon 이미지 변경
                markers.add(marker);

                //마커 클릭시 발생하는 이벤트
                marker.setOnClickListener( overlay -> {
                    //마커 클릭 시 주유소의 이름을 표시하고 토스트로 화면에 주소 표시
                    //Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
                    infoWindow.open(marker);//마커 클릭시 정보창 표시
                    // 이벤트 소비, OnMapClick 이벤트는 발생하지 않음
                    return true;
                });


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
        double la = location.getLatitude();
        double lo = location.getLongitude();

        Log.d("map", "좌표값 변경됨");
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
}