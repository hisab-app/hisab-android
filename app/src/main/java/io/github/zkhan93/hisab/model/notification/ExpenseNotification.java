package io.github.zkhan93.hisab.model.notification;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by zeeshan on 12/28/2016.
 */
@Entity
public class ExpenseNotification {
    @Id
    int expenseId;

    long createOn;
    String message;
    String groupId;
    int type;
//    public ExpenseNotification(){}
//    public ExpenseNotification(long createOn, String groupId, String message) {
//        this.createOn = createOn;
//        this.groupId = groupId;
//        this.message = message;
//
//    }
    @Generated(hash = 409747029)
    public ExpenseNotification(int expenseId, long createOn, String message,
            String groupId, int type) {
        this.expenseId = expenseId;
        this.createOn = createOn;
        this.message = message;
        this.groupId = groupId;
        this.type = type;
    }

    @Generated(hash = 1339352233)
    public ExpenseNotification() {
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
    public int getExpenseId() {
        return this.expenseId;
    }
    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
}
