package com.example.android.walkmyandroidplaces;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final int REQUEST_PICK_PLACE = 2;
    private static final String TRACKING_LOCATION_KEY = "tracking_location";

    private Button mLocationButton;
    private Button mPlacePickerButton;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;

    private boolean mTrackingLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    private PlaceDetectionClient mPlaceDetectionClient;
    private LocationCallback mLocationCallback;

    private AnimatorSet mRotateAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLocationButton = findViewById(R.id.button_location);
        mPlacePickerButton = findViewById(R.id.button_place_picker);
        mLocationTextView = findViewById(R.id.textview_location);
        mAndroidImageView = findViewById(R.id.imageview_android);


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(
                this);

        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator
                (this, R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(
                    TRACKING_LOCATION_KEY);
        }

        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mTrackingLocation) {
                    startTrackingLocation();
                } else {
                    stopTrackingLocation();
                }
            }
        });

        mPlacePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder =
                        new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this)
                            , REQUEST_PICK_PLACE);
                } catch (GooglePlayServicesRepairableException
                        | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation) {
                    new FetchAddressTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
                }
            }
        };
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            mTrackingLocation = true;
            mFusedLocationClient.requestLocationUpdates
                    (getLocationRequest(),
                            mLocationCallback,
                            null /* Looper */);

            mLocationTextView.setText(getString(R.string.address_text,
                    getString(R.string.loading),
                    getString(R.string.loading),
                    new Date()));
            mLocationButton.setText(R.string.stop_tracking_location);
            mRotateAnim.start();
        }
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start_tracking_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:

                if (grantResults.length > 0
                        && grantResults[0]
                        == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onTaskCompleted(final String result) throws SecurityException {
        if (mTrackingLocation) {
            Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull
                                                       Task<PlaceLikelihoodBufferResponse> task) {

                            if (task.isSuccessful()) {
                                PlaceLikelihoodBufferResponse likelyPlaces =
                                        task.getResult();
                                float maxLikelihood = 0;
                                Place currentPlace = null;
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    if (maxLikelihood < placeLikelihood.getLikelihood())
                                    {
                                        maxLikelihood = placeLikelihood.getLikelihood();
                                        currentPlace = placeLikelihood.getPlace();
                                    }
                                }

                                if (currentPlace != null) {
                                    mLocationTextView.setText(
                                            getString(R.string.address_text,
                                                    currentPlace.getName(), result,
                                                    System.currentTimeMillis()));
                                    setAndroidType(currentPlace);
                                }

                                likelyPlaces.release();
                            } else {
                                mLocationTextView.setText(
                                        getString(R.string.address_text,
                                                "No place found",
                                                result, System.currentTimeMillis()));
                            }

                        }
                    });
        }
    }

    private void setAndroidType(Place currentPlace) {
        int drawableID = -1;
        for (Integer placeType : currentPlace.getPlaceTypes()) {
            switch (placeType) {
                case Place.TYPE_SCHOOL:
                    drawableID = R.drawable.android_school;
                    break;
                case Place.TYPE_GYM:
                    drawableID = R.drawable.android_gym;
                    break;
                case Place.TYPE_RESTAURANT:
                    drawableID = R.drawable.android_restaurant;
                    break;
                case Place.TYPE_LIBRARY:
                    drawableID = R.drawable.android_library;
                    break;
            }
        }

        if (drawableID < 0) {
            drawableID = R.drawable.android_plain;
        }
        mAndroidImageView.setImageResource(drawableID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            setAndroidType(place);
            mLocationTextView.setText(
                    getString(R.string.address_text, place.getName(),
                            place.getAddress(), System.currentTimeMillis()));

        } else {
            mLocationTextView.setText("No place found");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onPause() {
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mTrackingLocation) {
            startTrackingLocation();
        }
        super.onResume();
    }

}