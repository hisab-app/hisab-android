package io.github.zkhan93.hisab.model.notification;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by zeeshan on 12/28/2016.
 */
@Entity
public class GroupNotification {
    private long createdOn;
    private String message;
    @Id
    private String groupId;

    @Generated(hash = 2129744615)
    public GroupNotification(long createdOn, String message, String groupId) {
        this.createdOn = createdOn;
        this.message = message;
        this.groupId = groupId;
    }

    @Generated(hash = 1612993830)
    public GroupNotification() {
    }

//    public GroupNotification(){}
//    public GroupNotification(long createdOn, String message, String groupId) {
//        this.createdOn = createdOn;
//        this.message = message;
//        this.groupId = groupId;
//
//    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    @Override
    public String toString() {
        return "GroupNotification{" +
                "createdOn=" + createdOn +
                ", message='" + message + '\'' +
                ", groupId='" + groupId + '\'' +
                
                '}';
    }
}
