package com.zhelezny.weatherlv;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    private static class ViewHolder {
        ImageView imageView;
        TextView descriptionTextView;
        TextView dayTextView;
        TextView nightTextView;
        TextView humidityTextView;
    }

    private Map<String, Bitmap> bitmaps = new HashMap<>(); //Кэш загруженных изображений погоды

    public WeatherArrayAdapter (Context context, List<Weather> forecast) {
        super(context, -1, forecast);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Weather day = getItem(position); //Получение объекта Weather для заданной позиции ListView

        ViewHolder viewHolder; //Содержит ссылки на представления элемента списка

        //Проверяем возможность повторного использования VH для элемента вышедшего за границы экрана
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext()); //Наполнитель
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.imageView = convertView.findViewById(R.id.imageView);
            viewHolder.descriptionTextView = convertView.findViewById(R.id.descriptionTextView);
            viewHolder.dayTextView = convertView.findViewById(R.id.dayTextView);
            viewHolder.nightTextView = convertView.findViewById(R.id.nightTextView);
            viewHolder.humidityTextView = convertView.findViewById(R.id.humidityTextView);
            convertView.setTag(viewHolder); //Связываем VH с ListView
        } else { //Используем уже существующий VH заново
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //Если значок загружен-используем его, иначе загружаем в отдельном потоке
        if (bitmaps.containsKey(day.iconURI)) {
            viewHolder.imageView.setImageBitmap(bitmaps.get(day.iconURI));
        } else { //Загрузить и вывети значок погодных условий
            new LoadImageTask(viewHolder.imageView).execute(day.iconURI);
        }

        //Получаем данные из объекта Weather и заполняем представления
        Context context = getContext();
        viewHolder.descriptionTextView.setText(context.getString(R.string.day_description, day.dayOfWeek, day.description));
        viewHolder.dayTextView.setText(context.getString(R.string.day_temp, day.dayTemp));
        viewHolder.nightTextView.setText(context.getString(R.string.night_temp,day.nightTemp));
        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));

        return convertView;
    }

    //Загружаем изображение в отдельном потоке
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView image; //Для вывода миниатюры

        //Сохраняем image для загруженного объекта Bitmap
        public LoadImageTask(ImageView image) {
            this.image = image;
        }

        //Загружаем изображение.params[0] содержит содержит url-адрес изображения
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); //Создаем url для изображения
                connection = (HttpURLConnection) url.openConnection(); //Открываем соединение

                InputStream inputStream;
                // Получаем InputStream и загружаем изображение
                try {
                    inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream); //
                    bitmaps.put(params[0], bitmap); //Кэширование
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect(); //Закрываем соединение
            }
            return bitmap;
        }

        //Связываем значок погоды с элементом списка
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            image.setImageBitmap(bitmap);
        }
    }
}
