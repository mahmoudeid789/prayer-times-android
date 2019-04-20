package com.metinkale.prayer;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.utils.LocaleUtils;

import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.os.LocaleListCompat;

public class AliasManager extends InternalBroadcastReceiver implements InternalBroadcastReceiver.OnPrefsChangedListener {
    @Override
    public void onPrefsChanged(@NonNull String key) {
        if (key.equals(Preferences.LANGUAGE.getKey())) {
            PackageManager pm = getContext().getPackageManager();
            
            PackageInfo info;
            try {
                info = pm.getPackageInfo(getContext().getApplicationContext().getPackageName(),
                        PackageManager.GET_ACTIVITIES | PackageManager.GET_DISABLED_COMPONENTS);
            } catch (PackageManager.NameNotFoundException e) {
                Crashlytics.logException(e);
                throw new RuntimeException(e);
            }
            String prefix = "com.metinkale.prayer.alias";
            ArrayMap<String, String> aliases = new ArrayMap<>();
            for (ActivityInfo ai : info.activities) {
                if (ai.name.startsWith(prefix)) {
                    aliases.put(new Locale(ai.name.substring(prefix.length())).getLanguage(), ai.name);
                }
            }
            
            String bestAlias = "Default";
            LocaleListCompat locales = LocaleUtils.getLocalesCompat();
            for (int i = 0; i < locales.size(); i++) {
                String lang = locales.get(i).getLanguage();
                if (aliases.containsKey(lang)) {
                    bestAlias = lang;
                    break;
                }
            }
            
            for (Map.Entry<String, String> entry : aliases.entrySet()) {
                if (bestAlias.equals(entry.getKey())) {
                    //enable
                    pm.setComponentEnabledSetting(new ComponentName(getContext(), entry.getValue()), PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                            PackageManager.DONT_KILL_APP);
                } else if (!bestAlias.equals(entry.getKey())) {
                    //disable
                    pm.setComponentEnabledSetting(new ComponentName(getContext(), entry.getValue()), PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
                }
            }
            
            
        }
    }
}
