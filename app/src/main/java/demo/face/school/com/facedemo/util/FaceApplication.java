package demo.face.school.com.facedemo.util;

import android.app.Application;

import org.litepal.LitePal;

public class FaceApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LitePal.initialize(this);//数据库初始化
    }


}