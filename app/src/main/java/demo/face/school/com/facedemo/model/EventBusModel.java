package demo.face.school.com.facedemo.model;

/**
 * Created by Administrator on 2017/6/21 0021.
 */

public class EventBusModel {
    private String type;
    private String str;

    public EventBusModel(String type, String str) {//事件传递参数
        this.type = type;
        this.str = str;
    }

    public String getType() {//取出事件参数
        return type;
    }

    public void setType(String msg) {
        this.type = type;
    }

    public String getStr() {//取出事件参数
        return str;
    }

    public void setStr(String msg) {
        this.str = str;
    }
}