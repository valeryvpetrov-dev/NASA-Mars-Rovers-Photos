package ru.geekbrains.android.level2.valeryvpetrov.data.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TypeConverter {
    @NonNull
    private static DateFormat dateFormat =
            new SimpleDateFormat(NASAMarsRoversGenerator.DATE_FORMAT, Locale.US);

    @Nullable
    public static String dateToString(@Nullable Date date) {
        if (date != null)
            return dateFormat.format(date);
        else
            return null;
    }

    @Nullable
    public static Date stringToDate(@Nullable String dateString) throws ParseException {
        if (dateString != null)
            return dateFormat.parse(dateString);
        else
            return null;
    }
}
