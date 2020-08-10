package com.delfi.xmobile.app.lecreusetcommunication;

import android.app.Application;

import com.delfi.xmobile.app.lecreusetcommunication.utils.FontChanger;
import com.delfi.xmobile.lib.xcore.logger.ExceptionHandler;

public class ModuleApp extends Application {

    public static boolean IsRunBackground;
    private static ModuleApp mInstance;

    public static ModuleApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        overrideDefaultTypefaces();
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

    /**
     * Method used to override the default typefaces with the custom fonts
     * for the application.
     */
    private void overrideDefaultTypefaces() {
        //for sans typeface
        FontChanger.overrideDefaultFont(this, "SANS_SERIF", "montserrat_regular.otf");
    }
}
