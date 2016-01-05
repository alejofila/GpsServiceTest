package com.example.alejandro.gpsservicetest;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import javax.xml.xpath.XPathFunctionException;

/**
 * Created by Alejandro on 5/01/2016.
 */
public class GpsService extends Service implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = GpsService.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private CustomReceiver mCustomReceiver;
    private Location mLastLocation;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Creating service");
        mCustomReceiver = new CustomReceiver();
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(CustomReceiver.STOP_SERVICE_ACTION);

        registerReceiver(mCustomReceiver,mIntentFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            Log.d(TAG, "Intent is not null");
        else
            Log.d(TAG, "Intent is null");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(3000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setMaxWaitTime(5000);
        Log.d(TAG, "Starting service");
        mGoogleApiClient.connect();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Destoying the service");
        super.onDestroy();
        unregisterReceiver(mCustomReceiver);
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }
    //LocationListener Callback

    /**
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "New Location arrived Latitude: " + location.getLatitude() + " Longitude : " + location.getLongitude());
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor= sp.edit();
        spEditor.putString("LAT",""+location.getLatitude());
        spEditor.putString("LON",""+location.getLongitude());
        spEditor.apply();


    }


    //GoogleApi.ConnectionCallbacks

    /**
     * @param bundle
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


    }

    /**
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspend");
        mGoogleApiClient.connect();

    }

    //GoogleApi.onConnectionFailedListner

    /**
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed " + connectionResult.getErrorMessage());

    }
    public  class  CustomReceiver extends BroadcastReceiver{

        public static final String STOP_SERVICE_ACTION ="stop_service";


        @Override
        public void onReceive(Context context, Intent intent) {
            GpsService.this.stopSelf();




        }
    }


}
