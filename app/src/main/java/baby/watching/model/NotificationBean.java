package baby.watching.model;

import java.io.Serializable;

/**
 * Created by mohit.soni on 29-Dec-17.
 */

public class NotificationBean implements Serializable {

    String packageName;
    String Id;
    String Tag;
    String postTime;
    String appName;
    int icon;
    int count;

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getTag() {
        return Tag;
    }

    public void setTag(String tag) {
        Tag = tag;
    }

    public String getPostTime() {
        return postTime;
    }

    public void setPostTime(String postTime) {
        this.postTime = postTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "NotificationBean{" +
                "packageName='" + packageName + '\'' +
                ", Id='" + Id + '\'' +
                ", Tag='" + Tag + '\'' +
                ", postTime='" + postTime + '\'' +
                ", appName='" + appName + '\'' +
                ", count=" + count +
                '}';
    }
}
