package com.example.lfs.agvcontrol.Application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by lfs on 2018/6/28.
 */

public class MyApplication extends Application{
    public static String connectIP="192.168.0.1";

    public static void initIp(Context context){
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、取出数据
        String ip = dates.getString("ip","192.168.0.1");
        connectIP=ip;
    }
    public static String getIp(Context context){
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、取出数据
        String ip = dates.getString("ip",connectIP);
        return ip;
    }

    public static void saveIp(Context context, String ip){
        connectIP=ip;
        //1、打开Preferences，名称为setting，如果存在则打开它，否则创建新的Preferences
        SharedPreferences dates = context.getSharedPreferences("Dates", 0);
        //2、让setting处于编辑状态
        SharedPreferences.Editor editor = dates.edit();
        //3、存放数据
        editor.putString("ip",ip);
        //4、完成提交
        editor.commit();
    }
}
