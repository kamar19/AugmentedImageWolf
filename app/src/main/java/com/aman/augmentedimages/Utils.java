package com.aman.augmentedimages;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class
Utils {
    public SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy hh:mm:ss", Locale.ENGLISH);

    public String getCurrentDateTimeString() {
        return sdf.format(new Date().toString());
    }
}
