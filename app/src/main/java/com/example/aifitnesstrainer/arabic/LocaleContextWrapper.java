package com.example.aifitnesstrainer.arabic;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.os.Build;

import java.util.Locale;

public class LocaleContextWrapper extends ContextWrapper {

    public LocaleContextWrapper(Context base) {
        super(base);
    }

    public static ContextWrapper wrap(Context context, String language) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(new Locale(language));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(new Locale(language));
        }
        return new LocaleContextWrapper(context.createConfigurationContext(configuration));
    }
}
