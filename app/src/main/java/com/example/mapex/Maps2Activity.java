package com.example.mapex;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class Maps2Activity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener
        , OnMapReadyCallback {

    GoogleApiClient apiClient;
    FusedLocationProviderClient providerClient;
    Location currentLocation;

    GoogleMap map;

    ArrayList<CenterData> centerList;
    int zoom;
    boolean isMapLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Intent intent = getIntent();

        centerList = GeoData.getAddressData();
        zoom=14;
        apiClient=new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        providerClient=LocationServices.getFusedLocationProviderClient(this);


        ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mission_map)).getMapAsync(this);
    }


    private void toast(String msg){
        Toast toast=Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        toast.show();
    }


    private void showMap(Location location){
        if(location != null){
            LatLng latLng=new LatLng(location.getLatitude(), location.getLongitude());
            CameraPosition position=new CameraPosition.Builder().target(latLng).zoom(zoom).build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(position));

            map.clear();
            map.addMarker(new MarkerOptions().position(latLng).title("현재 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));

            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
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
                LatLng centerLatLng=new LatLng(centerList.get(i).centerLat, centerList.get(i).centerLng);

                map.addMarker(new MarkerOptions().position(centerLatLng).title(centerList.get(i).centerName).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        apiClient.connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(apiClient.isConnected()){
            apiClient.disconnect();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        providerClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // 현재 위치 서울역이라 가정
                location.setLatitude(37.5559235);
                location.setLongitude(126.9697074);
                Maps2Activity.this.currentLocation=location;
                showMap(location);
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        UiSettings settings=map.getUiSettings();
        settings.setZoomControlsEnabled(true);
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                int idsZoom=(int)(map.getCameraPosition().zoom);
                Log.d("sh", "idsZoom:"+idsZoom);
                if(zoom != idsZoom && idsZoom > 10){
                    zoom=idsZoom;
                    showMap(currentLocation);
                }
            }
        });
    }


    private static final double EARTH_RADIUS=6378100.0;
    private int offset;


    private int convertMetersToPixels(double lat, double lng, double radiusInMeters){
        double lat1=radiusInMeters / EARTH_RADIUS;
        double lng1=radiusInMeters / (EARTH_RADIUS*Math.cos((Math.PI*lat/180)));
        double lat2=lat+lat1*180/Math.PI;
        double lng2=lng+lng1*180/Math.PI;


        Point p1=map.getProjection().toScreenLocation(new LatLng(lat, lng));
        Point p2=map.getProjection().toScreenLocation(new LatLng(lat2, lng2));
        return Math.abs(p1.x - p2.x);
    }


    private LatLng getCoords(double lat, double lng){
        LatLng latLng=new LatLng(lat, lng);
        Projection projection=map.getProjection();
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


    private void drawCircle(){
        LatLng circleLatLng=new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        int radius=1000;
        Bitmap bitmap=getCircleBitmap(circleLatLng, radius);
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(getCoords(circleLatLng.latitude, circleLatLng.longitude));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap));
        map.addMarker(markerOptions);
    }

}