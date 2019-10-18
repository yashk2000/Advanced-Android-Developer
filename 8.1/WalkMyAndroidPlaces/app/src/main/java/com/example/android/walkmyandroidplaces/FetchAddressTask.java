package com.example.android.walkmyandroidplaces;


import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class FetchAddressTask extends AsyncTask<Location, Void, String> {

    private Context mContext;
    private OnTaskCompleted mListener;


    FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
        mContext = applicationContext;
        mListener = listener;
    }

    private final String TAG = FetchAddressTask.class.getSimpleName();

    @Override
    protected String doInBackground(Location... params) {
        Geocoder geocoder = new Geocoder(mContext,
                Locale.getDefault());

        Location location = params[0];
        List<Address> addresses = null;
        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            resultMessage = mContext.getString(R.string.service_not_available);
            Log.e(TAG, resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            resultMessage = mContext.getString(R.string.invalid_lat_long_used);
            Log.e(TAG, resultMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        if (addresses == null || addresses.size() == 0) {
            if (resultMessage.isEmpty()) {
                resultMessage = mContext.getString(R.string.no_address_found);
                Log.e(TAG, resultMessage);
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressParts = new ArrayList<>();

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressParts.add(address.getAddressLine(i));
            }

            resultMessage = TextUtils.join(
                    "\n",
                    addressParts);

        }

        return resultMessage;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onTaskCompleted(address);
        super.onPostExecute(address);
    }

    interface OnTaskCompleted {
        void onTaskCompleted(String result);
    }
}