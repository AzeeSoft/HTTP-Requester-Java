package com.azeesoft.libs.httprequester.core.tools;

import java.util.prefs.Preferences;

/**
 * Created by azizt on 2/27/2017.
 */
public class QT {
    public static void PL(String s) {
        System.out.println(s);
    }


    public static Preferences getPreferences() {
        return Preferences.userRoot().node("AZDDNSUpdater");
    }
}

