package ru.geekbrains.android.level2.valeryvpetrov;

import android.app.Application;

public class NasaRoversApplication extends Application {

    public static final String SHARED_PREFERENCES_NAME = "NasaRoversSP";
    public static final String SHARED_PREFERENCES_KEY_ROVER_ID = "id";
    public static final String SHARED_PREFERENCES_KEY_ROVER_NAME = "name";
    public static final String SHARED_PREFERENCES_KEY_ROVER_LANDING_DATE = "landingDate";
    public static final String SHARED_PREFERENCES_KEY_ROVER_LAUNCH_DATE = "launchDate";
    public static final String SHARED_PREFERENCES_KEY_ROVER_STATUS = "status";
    public static final String SHARED_PREFERENCES_KEY_ROVER_MAX_SOL = "maxSol";
    public static final String SHARED_PREFERENCES_KEY_ROVER_MAX_DATE = "maxDate";
    public static final String SHARED_PREFERENCES_KEY_ROVER_TOTAL_PHOTOS = "totalPhotos";

    public static final String SHARED_PREFERENCES_KEY_ROVER_LAST_REGISTERED_LAUNCH_FORMAT = "%sLastRegisteredLaunch";

    public static final String SHARED_PREFERENCES_KEY_ROVER_LANDING_LAT = "%sLandingLat";
    public static final String SHARED_PREFERENCES_KEY_ROVER_LANDING_LNG = "%sLandingLng";

}
