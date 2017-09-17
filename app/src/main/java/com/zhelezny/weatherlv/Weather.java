package com.zhelezny.weatherlv;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

//Информация о погоде за однин день
public class Weather {

    public final String dayOfWeek;
    public final String dayTemp;
    public final String nightTemp;
    public final String humidity;
    public final String description;
    public final String iconURI;

    public Weather (double dayTemp, double nightTemp, double humidity,
                    long timeStamp, String description, String iconName) {

        //Настройка на форматирование(температур) в целые числа
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        this.dayTemp = numberFormat.format(dayTemp) + "\u00B0C";
        this.nightTemp = numberFormat.format(nightTemp) + "\u00B0C";
        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.0);

        this.dayOfWeek = convertTimeStampToDay(timeStamp);
        this.description = description;
        this.iconURI = "http://openweathermap.org/img/w/" + iconName + ".png";

    }

    //Преобразовываем временную метку в день недели
    private static String convertTimeStampToDay(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000); //Получаем время
        TimeZone tz = TimeZone.getDefault(); //Часовой пояс устройства

        //Поправка на часовой пояс устройства
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        //Возвращаем день недели
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        return dateFormatter.format(calendar.getTime());

    }

}
