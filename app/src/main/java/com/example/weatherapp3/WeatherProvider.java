package com.example.weatherapp3;

import android.os.Handler;
import android.util.Log;

import com.example.weatherapp3.weatherApi.WeatherApi;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WeatherProvider {
    private static final String BASE_URL = "https://api.openweathermap.org/";
    private static final String KEY = "e83d0265c9865659af525e50e89b8edd";
    private Timer timer;
    private Handler handler = new Handler();

    public static WeatherProvider instance = null;

    List<WeatherProviderListener> listeners;

    public static WeatherProvider getInstance() {
        instance = instance == null ? new WeatherProvider() : instance;
        return instance;
    }

    public WeatherProvider() {
        listeners = new ArrayList<>();
        startLoadData();
    }

    interface Openweather {
        @GET("data/2.5/forecast")
        Call<WeatherApi> getWeather(@Query("q") String q, @Query("appid") String key);

    }

    private WeatherApi getWeatherByRetrofit(String city) {
//        WeatherApi weatherApi = null; //fixme i need init weathet api??
// https://api.openweathermap.org/data/2.5/forecast?q=Moscow,ru&appid=e83d0265c9865659af525e50e89b8edd
//            URL url = new URL(BASE_URL + "data/2.5/forecast?q=" +city+ ",ru&appid=" + KEY);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Call<WeatherApi> call = retrofit.create(Openweather.class)
                .getWeather(city + ",ru", KEY);


        try {
            Response<WeatherApi> response = call.execute();
//            if (response.isSuccessful())
            Log.i("getdata", response.body().getCity().getName());
            return response.body();

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void addListener(WeatherProviderListener listener) {
        //todo add listener
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WeatherProviderListener listener) {
        //todo add listener
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    private void startLoadData() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {


                final WeatherApi weatherApi = getWeatherByRetrofit("Moscow");
//                final WeatherApi weatherApi = getWeatherByOkHttp("Moscow");
//                getWeatherByRetrofit("Omsk");// load data to log
                Log.i("loadData", weatherApi.getCity().getName());
//                Log.i("loadData", weatherApi.getCod());

                if (weatherApi == null) {
                    return;
                }
                handler.post(new Runnable() {//new treads
                    @Override
                    public void run() {
                        //todo load data

                        for (WeatherProviderListener listener : listeners) {
                            listener.upDateWeather(weatherApi);
                        }

                    }
                });


            }
        }, 2000, 100000);
    }

    void stopLoadData() {
        if (timer == null) {
            timer.cancel();
        }
        listeners.clear();
    }
}