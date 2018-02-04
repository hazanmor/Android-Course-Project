package com.demonized.androidproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback {
    MapView mapView;
    GoogleMap googleMap;
    BroadcastReceiver br;
    IntentFilter filter;
    public MapFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        br= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(intent.getDoubleExtra("LAT",0),intent.getDoubleExtra("LON",0))));
            }
        };
        filter= new IntentFilter();
        filter.addAction(GalleryActivity.ACTION);
        getActivity().registerReceiver(br,filter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstance) {
        mapView = (MapView) v.findViewById(R.id.map);
        mapView.onCreate(savedInstance);
        mapView.onResume();
        mapView.getMapAsync(this);

    }

    @Override
    public void onResume() {
        mapView.onResume();
        getActivity().registerReceiver(br,filter);
        super.onResume();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onPause(){
        mapView.onPause();
        getActivity().unregisterReceiver(br);
        super.onPause();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap=googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

    }


}
