package com.jerryjk.weatherapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jerryjk.weatherapp.Model.WeatherData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivity extends AppCompatActivity {

    public static final String CITY_NAME = "cityName";


    private ViewPager mViewPager;
    private RecyclerView mRVFishPrice;
    private WeatherAdapter mAdapter;

    private SharedPreference sharedPreference;
    Activity context = this;

    private String prefsCity;
    private static final String CLASS_TAG = "MainActivity";

    RestAdapter retrofit;
    MyWebService myWebService;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.container);
        setupViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        Intent intent = getIntent();
        String cityName = intent.getStringExtra(CITY_NAME);

        sharedPreference = new SharedPreference();
        prefsCity = sharedPreference.getCityPrefs(context);


        // ustawiamy wybrane parametry adaptera
        retrofit = new RestAdapter.Builder()
                // adres API
                .setEndpoint("http://api.openweathermap.org")
                // niech Retrofit loguje wszystko co robi
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        // tworzymy klienta
        myWebService = retrofit.create(MyWebService.class);


        //check internet connection
        if (internetConnectionCheck(MainActivity.this) == false) {
            Toast.makeText(getApplicationContext(), "Upss..brak połączenia z internetem", Toast.LENGTH_SHORT).show();
        }


        if (prefsCity == null & cityName == null) {

            Intent intent2 = new Intent(MainActivity.this, SearchCityActivity.class);
            startActivity(intent2);

        }else if (prefsCity != null & cityName == null) {
            startWeatherTask(prefsCity);

        }else  if (cityName != null) {
            startWeatherTask(cityName);
        }

    }



        /**
         *  Retrofit Getting Data From Web Service and Setting Views with Data
         */



        public void startWeatherTask(String cityNam) {


            try {
                myWebService.getData(cityNam, "json", "metric", "pl", "10", getString(R.string.APPID), new Callback<WeatherData>() {

                    final ProgressDialog loading = ProgressDialog.show(MainActivity.this, getString(R.string.loading_title), getString(R.string.loading_please_wait), false, false);
                    List<Weather> data = new ArrayList<>();

                    @Override
                    public void success(WeatherData myWebServiceResponse, Response response) {


                        for (int i = 0; i < myWebServiceResponse.getList().size(); i++) {

                            data.add(new Weather(
                                    myWebServiceResponse.getCity().getName(),
                                    myWebServiceResponse.getList().get(i).getDt(),
                                    myWebServiceResponse.getList().get(i).getTemp().getDay(),
                                    myWebServiceResponse.getList().get(i).getWeather().get(0).getDescription(),
                                    myWebServiceResponse.getList().get(i).getWeather().get(0).getIcon(),
                                    myWebServiceResponse.getList().get(i).getTemp().getMin(),
                                    myWebServiceResponse.getList().get(i).getTemp().getMax(),
                                    myWebServiceResponse.getList().get(i).getTemp().getNight(),
                                    myWebServiceResponse.getList().get(i).getPressure()));

                        }

                        Weather weather1 = data.get(0);

                        // set Views with Weather data
                        ImageView bitViev1 = (ImageView) findViewById(R.id.weather_image);
                        bitViev1.setImageResource(weather1.getImgResource());


                        TextView cName = (TextView) findViewById(R.id.city_name);
                        TextView tDay = (TextView) findViewById(R.id.tempDay);
                        TextView wDescription = (TextView) findViewById(R.id.weather_description);
                        TextView wDt = (TextView) findViewById(R.id.weather_dt);
                        TextView tMin = (TextView) findViewById(R.id.temp_min);
                        TextView tMax = (TextView) findViewById(R.id.temp_max);
                        TextView tNight = (TextView) findViewById(R.id.temp_night);
                        TextView dPressure = (TextView) findViewById(R.id.pressure);

                        if (weather1.getCityName().equals("Śródmieście")) {
                            cName.setText("Kraków");
                        } else
                            cName.setText(weather1.getCityName());

                        tDay.setText(weather1.getWeatherTemp());
                        wDescription.setText(weather1.getWeatherDescription());
                        wDt.setText(weather1.getWeatherDt());
                        tMin.setText("Dzień Min  " + weather1.getTempMin());
                        tMax.setText("Dzień Max  " + weather1.getTempMax());
                        tNight.setText("Noc  " + weather1.getTempNight());
                        dPressure.setText("Ciśnienie  " + weather1.getPressure());

                        // Setup and Handover data to recyclerview
                        mRVFishPrice = (RecyclerView) findViewById(R.id.weatherList);
                        mAdapter = new WeatherAdapter(MainActivity.this, data);
                        mRVFishPrice.setAdapter(mAdapter);
                        mRVFishPrice.setLayoutManager(new LinearLayoutManager(MainActivity.this));


                        loading.dismiss();

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d(CLASS_TAG, error.getLocalizedMessage());

                        loading.dismiss();
                    }

                });


            } catch (Exception e) {
                Log.d(CLASS_TAG, e.toString());
            }


        }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_search) {
            Intent intent = new Intent(this, SearchCityActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment1DayWeather(), "DZISIAJ");
        adapter.addFragment(new Fragment10DaysWeather(), "10 DNI");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }




    public static boolean internetConnectionCheck(Activity CurrentActivity) {
        Boolean Connected = false;
        ConnectivityManager connectivity = (ConnectivityManager) CurrentActivity.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) for (int i = 0; i < info.length; i++)
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    Log.e("My Network is: ", "Connected ");
                    Connected = true;
                } else {}

        } else {
            Log.e("My Network is: ", "Not Connected");

            Toast.makeText(CurrentActivity.getApplicationContext(),
                    "Please Check Your internet connection",
                    Toast.LENGTH_LONG).show();
            Connected = false;

        }
        return Connected;

    }



}







