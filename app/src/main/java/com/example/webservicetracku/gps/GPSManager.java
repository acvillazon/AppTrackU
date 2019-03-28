package com.example.webservicetracku.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class GPSManager implements LocationListener {

    Context context;
    LocationManager locationManager;
    boolean gps_enabled, network_enable;
    GPSManagerInterface caller;

    public GPSManager(Context applicationContext,GPSManagerInterface caller) {
        context = applicationContext;
        this.caller=caller;
    }

    public void InitLocationManager() {
        try {
            locationManager = (LocationManager) context.
                    getSystemService(context.LOCATION_SERVICE);

            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network_enable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            if(gps_enabled) {
                locationManager.
                        getLastKnownLocation(LocationManager.GPS_PROVIDER);
                locationManager.
                        requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                0, 20, this,
                                Looper.getMainLooper());
            } else if(network_enable) { // Checking for GSM
                locationManager.
                        getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                locationManager.
                        requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                                0, 20, this,
                                Looper.getMainLooper());
            }
            String Provider = "GPS "+gps_enabled+" , "+"NETWORK "+network_enable;
//            Toast.makeText(context.getApplicationContext(),Provider+"",Toast.LENGTH_SHORT).show();
        }catch (Exception error){

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        //Toast.makeText(context.getApplicationContext(),"entre",Toast.LENGTH_SHORT).show();
        caller.LocationReceived(location.getLatitude(),location.getLongitude());
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
