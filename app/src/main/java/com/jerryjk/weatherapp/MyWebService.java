package com.jerryjk.weatherapp;


import com.jerryjk.weatherapp.Model.WeatherData;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by jarek on 2016-11-30.
 */


    public interface MyWebService {



        @GET("/data/2.5/forecast/daily") // deklarujemy endpoint oraz metodę
        void getData(@Query("q") String q,
                     @Query("mode") String mode,
                     @Query("units") String units,
                     @Query("lang") String lang,
                     @Query("cnt") String cnt,
                     @Query("appid") String appid,
                Callback<WeatherData> pResponse);



      // @POST("/wsexample/") // deklarujemy endpoint, metodę oraz dane do wysłania
      //  void postData(@Body Weather pBody, Callback<Weather> pResponse);
    }

