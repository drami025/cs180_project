package com.cs180.ucrtinder.ucrtinder.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.cs180.ucrtinder.ucrtinder.Parse.ParseConstants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class GeoLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
                                                           GoogleApiClient.OnConnectionFailedListener,
                                                           LocationListener {

    GoogleApiClient mGoogleApiClient = null;
    Location mLastLocation = null;

    Context mContext;
    Boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;

    public GeoLocationService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(getClass().getSimpleName(), "Started geolocation in OnStartCommand");

        // Get context
        mContext = getApplicationContext();

        // Start GoogleApiClient
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // Connect googleApiClient
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            createLocationRequest();
            startLocationUpdates();
        }

        return START_STICKY;
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        mRequestingLocationUpdates = true;
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {

            if (mRequestingLocationUpdates) {
                createLocationRequest();
                startLocationUpdates();
            }

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // Start Parse put request
                    ParseUser currentUser = ParseUser.getCurrentUser();
                    if (currentUser != null) {

                        Log.d(getClass().getSimpleName(), mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
                        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        currentUser.put(ParseConstants.KEY_LOCATION, parseGeoPoint);

                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Log.d(getClass().getSimpleName(), "Done saving geopoint");
                            }
                        });
                    }
                }
            });
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();

        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        stopLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Start parse put request
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {

                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    currentUser.put(ParseConstants.KEY_LOCATION, parseGeoPoint);

                    currentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            Log.d(getClass().getSimpleName(), "Done saving geopoint");
                        }
                    });

                    Log.d(getClass().getSimpleName(), mLastLocation.getLatitude() + ", " + mLastLocation.getLongitude());
                }
            }
        });

        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    protected void startLocationUpdates() {
        Log.d(getClass().getSimpleName(), "Started location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);

    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1 * 20 * 1000);         // 20 seconds for test
        //mLocationRequest.setInterval(3 * 60 * 1000);       // 3 minute for real
        mLocationRequest.setFastestInterval(1 * 20 * 1000);  // 20 seconds for test
        //mLocationRequest.setFastestInterval(3 * 60 * 1000);// 3 minute for real
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }
}
