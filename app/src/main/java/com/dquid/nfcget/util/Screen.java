package com.dquid.nfcget.util;

import android.view.Window;
import android.view.WindowManager;

/**
 * Screen.java
 *
 * Purpose: static class to handle screen timeout
 *
 * @author Lorenzo @ RSR.srl
 * @version 1.0
 */
public class Screen
{
    /**
     * Avoid the screen to turn off till a releaseScreenOn call
     */
    public static void keepScreenOn(Window window)
    {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    /**
     * Release the screen on blocking let the screen to turn off after this call
     * @param window window view
     */
    public static void releaseScreenOn(Window window)
    {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
