package com.zhelezny.weatherlv;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Список объектов Weather, предоставляющих прогноз погоды
    private List<Weather> weatherList = new ArrayList<>();
    //ArrayAdapter связывает объекты Weather с элементами ListView
    private WeatherArrayAdapter weatherArrayAdapter;
    //Для вывода информации
    private ListView weatherListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Связываем WeatherList c WeatherListView при помощи Adapter'а
        weatherListView = (ListView) findViewById(R.id.weatherListView);
        weatherArrayAdapter = new WeatherArrayAdapter(this, weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        //FAB скрывает клавиатуру и делает запрос к веб-сервису
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Получаем текст из locationEditText и создаем url веб-сервиса
                EditText locationEditText = (EditText) findViewById(R.id.locationEditText);
                URL url = createURL(locationEditText.getText().toString()); //Передаем текст для URL

                //Скрываем клавиатуру и запускает поток для получения погоды от API
                if(url != null) {
                    dismissKeyboard(locationEditText);

                    new GetWeatherTask().execute(url);
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    //Прячем клавиатуру
    private void dismissKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private URL createURL(String city) {
        String apiKey = getString(R.string.api_key);
        String baseUrl = getString(R.string.web_service_url);

        try {
            //Формируем строку запроса
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8")
                    + "&units=metric&cnt=16&APPID=" + apiKey;
            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //Запрос погоды у веб-сервиса
    private class GetWeatherTask extends AsyncTask<URL, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection(); //Запрос к сервису
                int response = connection.getResponseCode(); //Получение кода ответа от сервиса

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    BufferedReader reader;
                    try {
                        //Упаковываем поток InputStream класса HttpURLConnection в BufferReader
                        reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                        String line;

                        //Читаем текст ответа и присоединяем в StringBuilder
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                    } catch (IOException e) {
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    //Преобразовываем строку JSON в JSONObject и отдаем onPostExecute
                    return new JSONObject(builder.toString());

                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                            R.string.connect_error, Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                        R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return null;
        }

        //Обрабатываем ответ JSON и обновляем ListView
        @Override
        protected void onPostExecute(JSONObject weather) {
            convertJSONtoArrayList(weather); //заполняем weatherList
            weatherArrayAdapter.notifyDataSetChanged(); //Связываем с ListView(обновление)
            weatherListView.smoothScrollToPosition(0); //Прокручиваем до верха
        }
    }

    //Создаем объект Weather на базе JSONObject с прогнозом
    private void convertJSONtoArrayList(JSONObject forecast) {
        weatherList.clear(); //Очищаем старые данные
        try {
            //Получаем свойство "list" JSONArray(массив объектов до 16 дней)
            JSONArray list = forecast.getJSONArray("list");

            //Преобразовываем каждый элемент списка в объект Weather
            for (int i = 0; i < list.length(); i++) {
                JSONObject day = list.getJSONObject(i); //Данные за день
                //Получаем JSONObject с температурами дня
                JSONObject temperatures = day.getJSONObject("temp");
                //Получаем JSONObject с описанием и значком
                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                //Добавляем новый объект Weather в ListView
                weatherList.add(new Weather(
                        temperatures.getDouble("day"), //Дневная температура
                        temperatures.getDouble("night"), //Ночная температура
                        day.getDouble("humidity"), //Процент влажности
                        day.getLong("dt"), //Временная метка даты/времени
                        weather.getString("description"), //Погодные условия
                        weather.getString("icon"))); //Имя значка

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

}