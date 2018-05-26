package com.londonappbrewery.climapm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.londonappbrewery.model.WeatherDom;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class WeatherController extends AppCompatActivity {

    final int NEW_CITY_CODE = 456;
    // Constants:
    final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather";
    // App ID to use OpenWeather data
    final String APP_ID = "eb190ce2b2f2ff67623a6a2a00688de2";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;

    final String LOGCAT_TAG = "Clima";

    final int REQUEST_CODE = 123;

    final String LOCATION_PROVIDER = LocationManager.NETWORK_PROVIDER;


    // Member Variables:
    Boolean mUseLocation = true;
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    LocationManager mLocationManager;
    LocationListener mLocationListener;


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(LOGCAT_TAG, "onResume() called");

        if (mUseLocation) {
            getWeatherForCoarseLocation();
            getWeatherForFineLocation();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_controller_layout);

        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
        ImageButton changeCityButton = (ImageButton) findViewById(R.id.changeCityButton);

        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d(LOGCAT_TAG, "onLocationChanged() callback received");

                Log.d(LOGCAT_TAG, "long is: " + location.getLongitude());
                Log.d(LOGCAT_TAG, "lat is: " + location.getLatitude());

                RequestParams params = new RequestParams();
                params.put("lat", location.getLatitude());
                params.put("lon", location.getLongitude());
                params.put("appid", APP_ID);

                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d(LOGCAT_TAG, "onProviderDisabled() callback received");
            }
        };

        // TODO: Add an OnClickListener to the changeCityButton here:

    }


    // TODO: Add onResume() here:

    @Override
    protected void onPause() {
        super.onPause();

        if (mLocationManager != null) mLocationManager.removeUpdates(mLocationListener);
    }




    // TODO: Add getWeatherForNewCity(String city) here:

    private void getWeatherForNewCity(String city){
        Log.d(LOGCAT_TAG, "Getting weather for new city");
        RequestParams params = new RequestParams();
        params.put("q", city);
        params.put("appid", APP_ID);

        letsDoSomeNetworking(params);
    }


    private void getWeatherForCoarseLocation() {
        Log.d(LOGCAT_TAG, "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE);

            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    private void getWeatherForFineLocation() {
        Log.d(LOGCAT_TAG, "Getting weather for current location");
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }


    private void letsDoSomeNetworking(RequestParams params){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(WEATHER_URL, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.d(LOGCAT_TAG, "Success! JSON: " + response.toString());

                Gson gson = new Gson(); // Or use new GsonBuilder().create();
                WeatherDom weather = gson.fromJson(response.toString(), WeatherDom.class);

                updateUI(weather);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);

                Log.d(LOGCAT_TAG, "FAIL! " + throwable.toString());
                Toast.makeText(WeatherController.this, "Request failed", Toast.LENGTH_SHORT).show();

                Log.d(LOGCAT_TAG, "Status code " + statusCode);
                Log.d(LOGCAT_TAG, "response was " + errorResponse.toString());
            }
        });
    }


    // TODO: Add updateUI() here:
    private void updateUI(WeatherDom dom)
    {
        mTemperatureLabel.setText(dom.getMain().getTempAsString());
        mCityLabel.setText(dom.getName());

        int resourceID = getResources().getIdentifier(dom.getIconName(), "drawable", getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(LOGCAT_TAG, "Location permission granted!");
                getWeatherForCoarseLocation();
                getWeatherForFineLocation();
            }
            else {
                Log.d(LOGCAT_TAG, "Permission denied - stupid GDPA");
            }
        }
    }

    public void changeCityClicked(View view) {
        Intent myIntent = new Intent(WeatherController.this, ChangeCityController.class);
        startActivityForResult(myIntent, NEW_CITY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LOGCAT_TAG, "onActivityResult() called");
        if (requestCode == NEW_CITY_CODE && resultCode == RESULT_OK){
            String city = data.getStringExtra("City");
            Log.d(LOGCAT_TAG, "New City is " + city);
            mUseLocation = false;
            getWeatherForNewCity(city);
        }
    }
}
