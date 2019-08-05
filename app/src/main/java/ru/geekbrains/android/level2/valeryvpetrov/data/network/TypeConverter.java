package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TypeConverter {
    private static DateFormat dateFormat =
            new SimpleDateFormat(NASAMarsPhotosJsonParser.DATE_FORMAT);

    public static String dateToString(Date date) {
        return dateFormat.format(date);
    }

    @Nullable
    public static Date stringToDate(String dateString) throws ParseException {
        return dateFormat.parse(dateString);
    }
}
