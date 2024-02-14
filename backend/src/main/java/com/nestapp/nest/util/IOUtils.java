package com.nestapp.nest.util;

import com.nestapp.nest.config.Config;

public class IOUtils {

    public static void log(Object... o) {
        if (o != null) {
            for (Object element : o) {
                if (element instanceof Exception) {
                    ((Exception) element).printStackTrace();
                } else {
                    System.out.println(element);
                }
            }
        }
    }

    public static void debug(Object... o) {
        if (o != null && Config.IS_DEBUG) {
            for (Object element : o) {
                if (element instanceof Exception) {
                    ((Exception) element).printStackTrace();
                } else {
                    System.out.println(element);
                }
            }
        }
    }
}
