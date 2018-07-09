package com.example.lfs.agvcontrol.Utils;

/**
 * Created by lfs on 2018/7/9.
 */

public class Utils {
    // 两次点击按钮之间的点击间隔不能少于1000毫秒
    private static final int MIN_CLICK_DELAY_TIME = 3000;
    private static long lastClickTime;

    public static boolean isFastClick() {
        boolean flag = false;
        long curClickTime = System.currentTimeMillis();
        if ((curClickTime - lastClickTime) >= MIN_CLICK_DELAY_TIME) {
            flag = true;
        }
        lastClickTime = curClickTime;
        return flag;
    }

    public static int getMinClickDelayTime() {
        return MIN_CLICK_DELAY_TIME;
    }
}
