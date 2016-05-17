package com.example.android.sunshine.app.wear;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.service.wallpaper.WallpaperService;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.sunshine.app.Utility;
import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class WearableIntentService extends IntentService implements GoogleApiClient.ConnectionCallbacks {
    private static final String LOG_TAG = WallpaperService.class.getSimpleName();

    public static final String WEATHER_ID = "weatherId";
    public static final String DESCRIPTION = "description";
    public static final String MAX_TEMP = "maxTemp";
    public static final String MIN_TEMP = "minTemp";

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
    };
    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_SHORT_DESC = 1;
    private static final int INDEX_MAX_TEMP = 2;
    private static final int INDEX_MIN_TEMP = 3;
    private static final String TODAY_PATH = "/today";

    private GoogleApiClient mGoogleApiClient;
    private boolean dataAwaitingConnection = false;
    private int mWeatherId;
    private String mDescription;
    private double mMaxTemp;
    private double mMinTemp;

    public WearableIntentService() {
        super("WearableIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .build();
        }
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (dataAwaitingConnection) {
            sendTodayDataToWearable();
            dataAwaitingConnection = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "onHandleIntent");

        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis());
        Cursor data = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null,
                null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        mWeatherId = data.getInt(INDEX_WEATHER_ID);
        mDescription = data.getString(INDEX_SHORT_DESC);
        mMaxTemp = data.getDouble(INDEX_MAX_TEMP);
        mMinTemp = data.getDouble(INDEX_MIN_TEMP);
        data.close();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            dataAwaitingConnection = true;
        } else {
            sendTodayDataToWearable();
            dataAwaitingConnection = false;
        }
    }

    private void sendTodayDataToWearable() {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(TODAY_PATH);
        DataMap dataMap = putDataMapRequest.getDataMap();
        dataMap.putInt(WEATHER_ID, mWeatherId);
        dataMap.putString(DESCRIPTION, mDescription);
        dataMap.putString(MAX_TEMP, Utility.formatTemperature(this, mMaxTemp));
        dataMap.putString(MIN_TEMP, Utility.formatTemperature(this, mMinTemp));

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();

        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
    }
}
