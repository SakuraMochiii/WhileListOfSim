package com.cloudpos.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.cloudpos.whilelistofsim.Logger;


public class SystemUtil {
    public static int getAPPVersionCodeFromAPP(Context ctx, String packageName) {
        int currentVersionCode = -1;
        PackageManager manager = ctx.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(packageName, 0);
            currentVersionCode = info.versionCode; // version code
        } catch (PackageManager.NameNotFoundException e) {
            Logger.debug(e.getMessage());
            currentVersionCode = -1;
        }
        return currentVersionCode;
    }
}
