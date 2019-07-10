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

public class MainActivity extends AppCompatActivity {

    //We will use this instance to provide location information
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Creates a client for the fused location provider, gets passed an activity or context
        //(a context is a "bundle" of activities
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Initialising the textView where the location will be displayed.
        //This location is unchangeable
        final TextView textView = findViewById(R.id.LocationDisplay);
        final TextView upperText = findViewById(R.id.upperText);
        upperText.setText(R.string.title);
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
                upperText.setText(R.string.locationPermissions);
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
            //THE COMMENTED OUT CODE CRASHES!!! (NullPointerException I think)
            //The following is a try/catch because the user may not supply permissions for location
            try {
                /*//Trying to get the location availability
                //Need to use an onSuccess listener I think
                fusedLocationClient.getLocationAvailability()
                        .addOnSuccessListener(new OnSuccessListener<LocationAvailability>() {
                            @Override
                            public void onSuccess(LocationAvailability locationAvailability) {
                                textView.setText(getText(R.string.locationUnavailable));
                                if(!fusedLocationClient.getLocationAvailability().
                                        getResult().isLocationAvailable())
                                    textView.setText(getText(R.string.locationUnavailable));

                            }
                        });
*/
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                textView.setText(getText(R.string.locationException));
                                if (location != null) {
                                    // Logic to handle location object
                                    //I really just want to print it (for now)
                                    textView.setText(location.toString());
                                } else {
                                    //Print a message saying that the location was null
                                    textView.setText(getText(R.string.locationNull));
                                }
                            }
                        });
            } catch (SecurityException exception) {
                Log.e("SecurityException", exception.toString());
                textView.setText(getText(R.string.locationException));
            } catch (NullPointerException exception) {
                Log.e("NullPointerException", exception.toString());
            } catch (Exception exception) {
                Log.e("Exception", exception.toString());
            }
            //END OF COPIED CODE

        }

    }
}