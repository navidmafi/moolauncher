package com.navidmafi.moolauncher.minecraft.service;

import com.navidmafi.moolauncher.minecraft.domain.LibraryInfo;
import com.navidmafi.moolauncher.util.OsUtils;

public class RuleEvaluator {
    public static boolean isAllowed(LibraryInfo lib) {
        if (lib.rules == null) {
            return true;
        }
        boolean explicitAllow = false;
        for (LibraryInfo.Rule r : lib.rules) {
            if (r.os == null) {
                if ("disallow".equals(r.action)) return false;
                if ("allow".equals(r.action)) explicitAllow = true;
            } else if (OsUtils.getMojangOsName().equals(r.os.name)) {
                if ("disallow".equals(r.action)) return false;
                if ("allow".equals(r.action)) explicitAllow = true;
            }
        }
        return explicitAllow || lib.rules.isEmpty();
    }
}
