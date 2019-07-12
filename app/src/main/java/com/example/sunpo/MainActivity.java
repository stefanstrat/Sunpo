package com.example.sunpo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.location.LocationAvailability;

import net.e175.klaus.solarpositioning.AzimuthZenithAngle;
import net.e175.klaus.solarpositioning.DeltaT;
import net.e175.klaus.solarpositioning.SPA;

import java.util.GregorianCalendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //We will use this instance to provide location information
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Creates a client for the fused location provider, gets passed an activity or context
        //(a context is a "bundle" of activities and other things
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Initialising the textView where the location will be displayed.
        //This location is unchangeable
        final TextView latitudeTextView = findViewById(R.id.textLocalLatitude);
        final TextView longitudeTextView = findViewById(R.id.textLocalLongitude);
        final TextView sunLatitudeTextView = findViewById(R.id.textSunLatitude);
        final TextView sunLongitudeTextView = findViewById(R.id.textSunLongitude);
        //START OF COPIED CODE FROM ANDROID DEVELOPERS
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            //The following is a try/catch because the user may not supply permissions for location
            try {
                //Building an onSuccessListener for the getLastLocation method
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                latitudeTextView.setText(getText(R.string.locationException));
                                if (location != null) {
                                    // Logic to handle location object
                                    //I really just want to print it (for now)
                                    latitudeTextView.setText(getText(R.string.localLatitude));
                                    latitudeTextView.append(Double.toString(location.getLatitude()));
                                    longitudeTextView.setText((getText(R.string.localLongitude)));
                                    longitudeTextView.append(Double.toString((location.getLongitude())));

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

                                    sunLatitudeTextView.append(String.format
                                            (Locale.ENGLISH,"%.2f", position.getAzimuth()));
                                    sunLongitudeTextView.append(String.format
                                            (Locale.ENGLISH, "%.2f", position.getZenithAngle()));


                                } else {
                                    //Print a message saying that the location was null
                                    latitudeTextView.setText(getText(R.string.locationNull));
                                }
                            }
                        });

            } catch (SecurityException exception) {
                Log.e("SecurityException", exception.toString());
            } catch (NullPointerException exception) {
                Log.e("NullPointerException", exception.toString());
            } catch (Exception exception) {
                Log.e("Exception", exception.toString());
            }
            //END OF COPIED CODE

        }

    }
}