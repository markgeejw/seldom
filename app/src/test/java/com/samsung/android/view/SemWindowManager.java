package com.samsung.android.view;

import android.content.ComponentName;

public class SemWindowManager {
    public static boolean failing;
    public static ComponentName lastComponent;
    public static boolean lastEnabled;

    public static SemWindowManager getInstance() {
        return new SemWindowManager();
    }

    public void requestMetaKeyEvent(ComponentName component, boolean enabled) {
        if (failing) throw new SecurityException("denied");
        lastComponent = component;
        lastEnabled = enabled;
    }
}
