package com.example.android.sunshine.app;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class WeatherListenerService extends WearableListenerService {
    private static final String LOG_TAG = WeatherListenerService.class.getSimpleName();

    private static final String TODAY_PATH = "/today";
    public static final String TODAY_DATA = "today_data";
    public static final String WEATHER_ID = "weatherId";
    public static final String DESCRIPTION = "description";
    public static final String MAX_TEMP = "maxTemp";
    public static final String MIN_TEMP = "minTemp";

    public WeatherListenerService() {
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(LOG_TAG, "onDataChanged: " + dataEvents);
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                String path = event.getDataItem().getUri().getPath();
                if (TODAY_PATH.equals(path)) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    DataMap dataMap = dataMapItem.getDataMap();
                    SharedPreferences prefs = getSharedPreferences(TODAY_DATA, MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = prefs.edit();
                    prefsEditor.putInt(WEATHER_ID, dataMap.getInt(WEATHER_ID));
                    prefsEditor.putString(DESCRIPTION, dataMap.getString(DESCRIPTION));
                    prefsEditor.putString(MAX_TEMP, dataMap.getString(MAX_TEMP));
                    prefsEditor.putString(MIN_TEMP, dataMap.getString(MIN_TEMP));
                    prefsEditor.apply();
                }
            }
        }
    }
}
