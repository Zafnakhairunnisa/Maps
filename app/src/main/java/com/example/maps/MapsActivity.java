package com.example.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.maps.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private SensorManager mSensorManager;

    private Sensor mSensorLight;
    private boolean isFragmentDisplayed = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mSensorLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.mapstyle));

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can't find style. Error: ", e);
        }

        setMapOnClick(mMap);
        setPoiClicked(mMap);
        enableMyLocation();

    }

    private void setMapOnClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                String text = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);

                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                String address = "";
                try {
                    List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address returnedAddress = addresses.get(0);
                        address = returnedAddress.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .snippet(text)
                        .title("Dropped pin")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.zIndex(1f);

                mMap.clear();
                Marker marker = mMap.addMarker(markerOptions);
                mMap.addMarker(markerOptions);
                marker.showInfoWindow();

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16));


                Bundle bundle = new Bundle();
                bundle.putString("name", "Dropped pin");
                bundle.putDouble("latitude", latLng.latitude);
                bundle.putDouble("longitude", latLng.longitude);
                bundle.putString("address", address);

                SimpleFragment simpleFragment = SimpleFragment.newInstance();
                simpleFragment.setArguments(bundle);

                displayFragment();
                //untuk tambah marker
//                mMap.clear();
//                map.addMarker(new MarkerOptions()
//                        .position(latLng)
//                        .snippet(text)
//                        .title("Dropped pin")
//                );
//                displayFragment();
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.language) {
            Intent languageIntent = new Intent(Settings.ACTION_LOCALE_SETTINGS);
            startActivity(languageIntent);
            return true;
        }
        else {
            switch (item.getItemId()) {
                case R.id.normal_map:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    return true;
                case R.id.satellite_map:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    return true;
                case R.id.hybrid_map:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    return true;
                case R.id.terrain_map:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }

    }


    private void setPoiClicked(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(@NonNull PointOfInterest pointOfInterest) {

                Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                String address = "";

                try {
                    List<Address> addresses = geocoder.getFromLocation(pointOfInterest.latLng.latitude, pointOfInterest.latLng.longitude, 1);
                    if (addresses != null && addresses.size() > 0) {
                        Address returnedAddress = addresses.get(0);
                        address = returnedAddress.getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(pointOfInterest.latLng)
                        .title(pointOfInterest.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                markerOptions.zIndex(1f);

                mMap.clear();
                Marker poiMarker = mMap.addMarker(markerOptions);
                poiMarker.showInfoWindow();

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(poiMarker.getPosition(), 16));

                Bundle bundle = new Bundle();
                bundle.putString("name", pointOfInterest.name);
                bundle.putString("address", address);
                bundle.putDouble("latitude", pointOfInterest.latLng.latitude);
                bundle.putDouble("longitude", pointOfInterest.latLng.longitude);
                SimpleFragment simpleFragment = new SimpleFragment();
                simpleFragment.setArguments(bundle);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, simpleFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
//                mMap.clear();
//                Marker poiMarker = mMap.addMarker(new MarkerOptions()
//                        .position(pointOfInterest.latLng)
//                        .title(pointOfInterest.name)
//                );
//                poiMarker.showInfoWindow();
//                displayFragment();

            }


        });
    }

    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
            if (location != null) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
            }

        }
        else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }

    private void displayFragment() {
        SimpleFragment simpleFragment = SimpleFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, simpleFragment).addToBackStack(null).commit();
        isFragmentDisplayed = true;
    }

    private void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        SimpleFragment simpleFragment = (SimpleFragment) fragmentManager.findFragmentById(R.id.fragment_container);
        if (simpleFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(simpleFragment).commit();
            isFragmentDisplayed = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1 :
                if (grantResults.length>0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    enableMyLocation();
                    break;
                }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSensorLight != null) {
            mSensorManager.registerListener(this, mSensorLight,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

       float currentValue = event.values[0];


        if (currentValue >= 0 && currentValue < 10){

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.mapstyledark));
                }
            });

            findViewById(R.id.title).setBackgroundColor(getResources().getColor(R.color.blue3));
            findViewById(R.id.fragment_container).setBackgroundColor(getResources().getColor(R.color.blue));
            TextView textView = findViewById(R.id.title);
            textView.setTextColor(Color.WHITE);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment instanceof SimpleFragment) {
                TextView name = fragment.getView().findViewById(R.id.nameMap);
                TextView lat = fragment.getView().findViewById(R.id.latitudeMap);
                TextView lon = fragment.getView().findViewById(R.id.longitudeMap);
                TextView address = fragment.getView().findViewById(R.id.addressMap);

                name.setTextColor(Color.WHITE);
                lat.setTextColor(Color.WHITE);
                lon.setTextColor(Color.WHITE);
                address.setTextColor(Color.WHITE);

            }
        }
        else if(currentValue >= 10 && currentValue <= 40000){

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this, R.raw.mapstyle));
                }
            });

            findViewById(R.id.fragment_container).setBackgroundColor(getResources().getColor(R.color.white));
            findViewById(R.id.title).setBackgroundColor(getResources().getColor(R.color.cream));
            TextView textView = findViewById(R.id.title);
            textView.setTextColor(Color.BLACK);

            FragmentManager fragmentManager = getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
            if (fragment instanceof SimpleFragment) {
                TextView name = fragment.getView().findViewById(R.id.nameMap);
                TextView lat = fragment.getView().findViewById(R.id.latitudeMap);
                TextView lon = fragment.getView().findViewById(R.id.longitudeMap);
                TextView address = fragment.getView().findViewById(R.id.addressMap);

                name.setTextColor(Color.BLACK);
                lat.setTextColor(Color.BLACK);
                lon.setTextColor(Color.BLACK);
                address.setTextColor(Color.BLACK);

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}