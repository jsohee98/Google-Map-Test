package com.example.mapex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    //
    final static int ACTIVITY_MAPS2 = 0;

    /*
    /////////////////////////////////////////////
    Spinner spType;
    Button btFind;
    SupportMapFragment supportMapFragment;
    GoogleMap map;
    FusedLocationProviderClient fusedLocationProviderClient;
    double currentLat = 0, currentLong = 0;
    ///////////////////////////////////////////////
    */

    //위치 정보 얻기
    private FusedLocationProviderClient mFusedLocationClient, providerClient;

    //권한 요청 코드
    public static final int REQUEST_CODE_PERMISSONS = 1000;
    final static int REQUST_MARK = 101;

    //Location currentLocation, cLocation;
    Location currentLocation;

    ArrayList<CenterData> centerList;
    int zoom;
    boolean isMapLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SearchView searchView = findViewById(R.id.sv_location);
        centerList = GeoData.getAddressData();
        zoom=14;

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        providerClient=LocationServices.getFusedLocationProviderClient(this);

        // 일반 지도, 위성 지도
        ToggleButton b_mapmode = findViewById(R.id.b_mapmode);
        b_mapmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                else mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        ToggleButton b_showCenter = findViewById(R.id.b_showCenter);
        b_showCenter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){/*
                    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            int idsZoom=(int)(mMap.getCameraPosition().zoom);
                            Log.d("sh", "idsZoom:"+idsZoom);
                            if(zoom != idsZoom && idsZoom > 10){
                                zoom=idsZoom;
                                showMap(currentLocation);
                            }
                        }
                    });*/
                    showMap(currentLocation);
                }else{
                    mMap.clear();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     **/


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng DS = new LatLng(37.651428, 127.016268);
        //mMap.addMarker(new MarkerOptions().position(DS).title("DS"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(DS));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                marker.showInfoWindow();
            }
        });

        UiSettings ui = mMap.getUiSettings();

        ui.setZoomControlsEnabled(true);
        ui.setCompassEnabled(true);
        ui.setMyLocationButtonEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        providerClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                location.setLatitude(location.getLatitude());
                location.setLongitude(location.getLongitude());
                MapsActivity.this.currentLocation=location;
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void mCurrentLocation(View v) {
        // 권한 체크
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_PERMISSONS);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            //현재 위치
                            LatLng myLocation = new LatLng(location.getLatitude(), location.getLongitude());

                            mMap.addMarker(new MarkerOptions()
                                    .position(myLocation)
                                    .title("현재 위치"));

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));

                            mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));

                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_PERMISSONS:
                if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "권한 체크 거부 됨", Toast.LENGTH_SHORT).show();
                }
                return;
        }
    }


    private void toast(String msg){
        Toast toast=Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showMap(Location location){
        if(location != null){
            LatLng latLng=new LatLng(location.getLatitude(), location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(latLng).title("현재 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14.0f));

            //Circle circle = mMap.addCircle(new CircleOptions()
            //    .center(latLng)
            //    .radius(1000)
            //    .strokeColor(Color.RED)
            //    .fillColor(Color.BLUE));

            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    isMapLoaded=true;
                    drawCircle();
                }
            });

            if(isMapLoaded){
                drawCircle();
            }
        }


        if(centerList != null && centerList.size()>0){
            Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.ic_center);
            for(int i=0; i<centerList.size(); i++){
                //LatLng centerLatLng=new LatLng(centerList.get(i).centerLat, centerList.get(i).centerLng);

                //MarkerOptions mo = new MarkerOptions();
                //mo.title(centerList.get(i).centerName);
                //mo.snippet(centerList.get(i).centerAdd);
                //mo.snippet(centerList.get(i).centerNum);
                //mo.position(centerLatLng);
                //mMap.addMarker(mo);

                //mMap.addMarker(new MarkerOptions().position(centerLatLng).title(centerList.get(i).centerName).snippet(centerList.get(i).centerAdd).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));


                if(centerList.get(i).centerLat<location.getLatitude()+0.0085) {
                    if(centerList.get(i).centerLat>location.getLatitude()-0.0085) {
                        if(centerList.get(i).centerLng<location.getLongitude()+0.0085) {
                            if(centerList.get(i).centerLng>location.getLongitude()-0.0085) {
                                LatLng centerLatLng=new LatLng(centerList.get(i).centerLat, centerList.get(i).centerLng);

                                //MarkerOptions mo = new MarkerOptions();
                                //mo.title(centerList.get(i).centerName);
                                //mo.snippet(centerList.get(i).centerAdd);
                                //mo.snippet(centerList.get(i).centerNum);
                                //mo.position(centerLatLng);
                                //mMap.addMarker(mo);

                                mMap.addMarker(new MarkerOptions().position(centerLatLng).title(centerList.get(i).centerName).snippet(centerList.get(i).centerAdd).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                            }
                        }
                    }
                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    private static final double EARTH_RADIUS=6378100.0;
    private int offset;

    private int convertMetersToPixels(double lat, double lng, double radiusInMeters){
        double lat1=radiusInMeters / EARTH_RADIUS;
        double lng1=radiusInMeters / (EARTH_RADIUS*Math.cos((Math.PI*lat/180)));
        double lat2=lat+lat1*180/Math.PI;
        double lng2=lng+lng1*180/Math.PI;

        Point p1=mMap.getProjection().toScreenLocation(new LatLng(lat, lng));
        Point p2=mMap.getProjection().toScreenLocation(new LatLng(lat2, lng2));
        return Math.abs(p1.x - p2.x);
    }


    private LatLng getCoords(double lat, double lng){
        LatLng latLng=new LatLng(lat, lng);
        Projection projection=mMap.getProjection();
        Point p=projection.toScreenLocation(latLng);
        p.set(p.x, p.y+offset);
        return  projection.fromScreenLocation(p);
    }


    private Bitmap getCircleBitmap(LatLng latLng, int r){
        Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0x110000FF);
        paint.setStyle(Paint.Style.FILL);

        Paint paint1=new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setColor(0xFF0000FF);
        paint1.setStyle(Paint.Style.STROKE);

        int radius=offset=convertMetersToPixels(latLng.latitude, latLng.longitude, r);

        Bitmap bitmap=Bitmap.createBitmap(radius*2, radius*2, Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(bitmap);
        c.drawCircle(radius, radius, radius, paint);
        c.drawCircle(radius, radius, radius, paint1);

        return bitmap;
    }

    private void drawCircle() {
        LatLng circleLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        int radius = 1000;
        Bitmap bitmap = getCircleBitmap(circleLatLng, radius);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(getCoords(circleLatLng.latitude, circleLatLng.longitude));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        mMap.addMarker(markerOptions);
    }
}

