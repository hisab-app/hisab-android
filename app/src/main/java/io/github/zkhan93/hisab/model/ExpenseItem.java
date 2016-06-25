package io.github.zkhan93.hisab.model;

/**
 * Created by Zeeshan Khan on 6/25/2016.
 */
public class ExpenseItem {
    Group group;
    User author;
    String description;
    float amount;
    long createdOn;

    public ExpenseItem() {
    }

    public ExpenseItem(Group group, User author, String description, float amount, long createdOn) {
        this.group = group;
        this.author = author;
        this.description = description;
        this.amount = amount;
        this.createdOn = createdOn;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(long createdOn) {
        this.createdOn = createdOn;
    }
}
