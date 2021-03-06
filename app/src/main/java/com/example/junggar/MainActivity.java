package com.example.junggar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.util.FusedLocationSource;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

public class  MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private Map<String, Object> result;
    GeoPoint position;
    String tmp;
    ArrayList<LatLng> latLngArrayList = new ArrayList<>();
    ArrayList<Marker> markerArrayList = new ArrayList<>();
    ArrayList<InfoWindow> infoWindowArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ImageView writeBtn = (ImageView) findViewById(R.id.Btn_write);
        ImageView refreshBtn = (ImageView) findViewById(R.id.Btn_refresh);
        ImageView searchBtn = (ImageView) findViewById(R.id.Btn_search);
        ImageView logoutBtn = (ImageView) findViewById(R.id.btn_logout);

        //????????????
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

       //?????? ????????? ????????????
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        //?????? ????????? ????????????
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostWriteActivity.class);
                startActivity(intent);
            }
        });

        //?????? refresh ??????
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        //????????? ?????? ?????? call
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        //????????? ?????? ????????????
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);


    };

    //gps ?????? ?????? ?????? ?????? ??? ?????? ?????? ?????? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,  @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // ?????? ?????????
                naverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d("StartCheck", "onMapReady starts here");

        //????????? ?????? ??????
        this.naverMap = naverMap;
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);

        //?????? ?????? ?????? on
        naverMap.setIndoorEnabled(true);

        //naver map ui setting
        UiSettings uiSettings = naverMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setLocationButtonEnabled(true);

        //?????? ?????? ?????? ?????? ??????
        Executor executor = command -> {
            Thread thread = new Thread(command);
            thread.start();
        };

        Handler handler = new Handler(Looper.getMainLooper());

        db.collection("posts").get().addOnSuccessListener(executor, queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                // Get data from DB
                result = document.getData();

                position = (GeoPoint) result.get("position");
                LatLng latlng = new LatLng(position.getLatitude(), position.getLongitude());
                int markerType = Integer.parseInt(String.valueOf(result.get("markertype")));
                tmp = (String) result.get("title");

                // Make marker from GetPoint
                Marker marker = new Marker();
                marker.setPosition(latlng);

                //?????? ?????? ??????
                switch (markerType){
                    case 1:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.hen_custom));
                        break;
                    case 2:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.westfood_custom));
                        break;
                    case 3:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.koreanfood_custom));
                        break;
                    case 4:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.japanfood_custom));
                        break;
                    case 5:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.fish_custom));
                        break;
                    case 6:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.noodle_custom));
                        break;
                    case 7:
                        marker.setIcon(OverlayImage.fromResource(R.drawable.snackfood_custom));
                        break;
                    default:
                        break;
                }

                marker.setWidth(100);
                marker.setHeight(100);
                marker.setTag(tmp);


                InfoWindow infoWindow = new InfoWindow();

                marker.setOnClickListener(overlay -> {
                    // ????????? ????????? ??? ???????????? ???
                    if (marker.getInfoWindow() == null) {
                        // ?????? ????????? ?????? ?????? ???????????? ?????? ?????? ???
                        infoWindow.open(marker);
                    } else {
                        // ?????? ?????? ????????? ?????? ?????? ???????????? ?????? ??????
                        infoWindow.close();
                    }

                    return true;
                });

                markerArrayList.add(marker);
                
                //InfoWindows ??? ?????? ??? ??????
                infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(getApplication()) {
                    @NonNull
                    @Override
                    public CharSequence getText(@NonNull InfoWindow infoWindow) {
                        // ?????? ?????? ?????? ????????? tag??? ???????????? ??????????????? ??????
                        return (CharSequence)infoWindow.getMarker().getTag();
                    }
                });
                
                //InfoWindows ??? ?????? ???

                infoWindow.setOnClickListener(overlay -> {
                    Intent intent = new Intent(MainActivity.this, PostActivity.class);
                    intent.putExtra("title",(String)infoWindow.getMarker().getTag());
                    startActivity(intent);
                    return true;
                });

                handler.post(()->{
                    for (Marker m : markerArrayList) m.setMap(naverMap);

                });
            }
        });


    }
}



