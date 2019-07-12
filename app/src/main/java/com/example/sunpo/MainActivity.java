package com.example.sunpo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Creates a client for the fused location provider, gets passed an activity or context
        //(a context is a "bundle" of activities and other things
        //We will use this instance to provide location information
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        //Initialising the textViews where the locations will be displayed.
        //This location is unchangeable
        final TextView latitudeTextView = findViewById(R.id.textLocalLatitude);
        final TextView longitudeTextView = findViewById(R.id.textLocalLongitude);
        final TextView sunAzimuthTextView = findViewById(R.id.textSunAzimuth);
        final TextView sunZenithTextView = findViewById(R.id.textSunZenith);


        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, tell the user
            latitudeTextView.setText(R.string.locationPermissions);

            // No explanation needed; request the permission
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                /*
                Show an explanation to the user *asynchronously* -- don't block
                this thread waiting for the user's response! After the user
                sees the explanation, try again to request the permission.
                */

            } else ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        } else {
            //The following is a try/catch because the user may not supply permissions for location
            try {
                //Building an onSuccessListener for the getLastLocation method
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            latitudeTextView.setText
                                    (getText(R.string.locationException));
                            if (location != null) {
                                // Logic to handle location object
                                //I really just want to print it (for now)
                                latitudeTextView.setText(getText(R.string.localLatitude));
                                latitudeTextView.append(String.format
                                        (Locale.ENGLISH, "%.2f",location.getLatitude()));
                                longitudeTextView.setText(getText(R.string.localLongitude));
                                longitudeTextView.append(String.format
                                        (Locale.ENGLISH, "%.2f",location.getLongitude()));

                                //And save it
                                double localLatitude = location.getLatitude();
                                double localLongitude = location.getLongitude();

                                //Now I want to use the KlausBrunner solarposition API to get
                                // the Sun's position
                                final GregorianCalendar dateTime = new GregorianCalendar();
                                AzimuthZenithAngle position = SPA.calculateSolarPosition(
                                        dateTime,
                                        localLatitude, // latitude (degrees)
                                        localLongitude, // longitude (degrees)
                                        190, // elevation (m)
                                        DeltaT.estimate(dateTime), // delta T (s)
                                        1010, // avg. air pressure (hPa)
                                        11); // avg. air temperature (°C)

                                sunAzimuthTextView.append(String.format
                                        (Locale.ENGLISH,"%.2f", position.getAzimuth()));
                                sunZenithTextView.append(String.format
                                        (Locale.ENGLISH, "%.2f", position.getZenithAngle()));

                            } else {
                                //Print a message saying that the location was null
                                latitudeTextView.setText(getText(R.string.locationNull));
                                longitudeTextView.setText(R.string.locationNull);
                                sunAzimuthTextView.setText(R.string.locationNull);
                                sunZenithTextView.setText(R.string.locationNull);
                            }
                        }
                    });

                //Callback to update location
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        if (locationResult == null) {
                            return;
                        }
                        for (Location location : locationResult.getLocations()) {
                            // Update UI with location data
                            longitudeTextView.setText(String.format
                                    (Locale.ENGLISH, "%.5f", location.getLongitude()));
                            latitudeTextView.setText(String.format
                                    (Locale.ENGLISH, "%.5f", location.getLatitude()));
                        }
                    }
                };
            } catch (SecurityException exception) {
                Log.e("SecurityException", exception.toString());
            } catch (NullPointerException exception) {
                Log.e("NullPointerException", exception.toString());
            } catch (Exception exception) {
                Log.e("Exception", exception.toString());
            }

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        createLocationRequest();
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
        }
        catch(SecurityException exception){
            Log.d("SecurityException", exception.toString());
        }
    }

    private LocationRequest locationRequest;
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(100);
        locationRequest.setFastestInterval(50);
        locationRequest.setMaxWaitTime(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

}