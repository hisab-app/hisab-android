package io.github.zkhan93.hisab.model.notification;

/**
 * Created by zeeshan on 12/28/2016.
 */

public class GroupNotification {
    private long createdOn;
    private String message;
    private String groupId;


    public GroupNotification(long createdOn, String message, String groupId) {
        this.createdOn = createdOn;
        this.message = message;
        this.groupId = groupId;

    }

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
