package io.github.zkhan93.hisab.model.notification;

/**
 * Created by zeeshan on 12/28/2016.
 */

public class ExpenseNotification {
    long createOn;
    String message;
    String groupId;
    int type;

    public ExpenseNotification(long createOn, String groupId, String message) {
        this.createOn = createOn;
        this.groupId = groupId;
        this.message = message;

    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getCreateOn() {
        return createOn;
    }

    public void setCreateOn(long createOn) {
        this.createOn = createOn;
    }

    @Override
    public String toString() {
        return "ExpenseNotification{" +
                "createOn=" + createOn +
                ", message='" + message + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
