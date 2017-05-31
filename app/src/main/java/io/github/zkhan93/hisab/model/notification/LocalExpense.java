package io.github.zkhan93.hisab.model.notification;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by zeeshan on 12/28/2016.
 */
@Entity
public class LocalExpense {
    @Id private String id;
    private String groupId;
    private long timestamp;
    private float amount;
    private String ownerName;
    private String ownerId;
    private String desc;
    private int type;
    @ToOne(joinProperty = "groupId")
    private LocalGroup group;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 1542029528)
    private transient LocalExpenseDao myDao;
    @Generated(hash = 2105533490)
    public LocalExpense(String id, String groupId, long timestamp, float amount,
            String ownerName, String ownerId, String desc, int type) {
        this.id = id;
        this.groupId = groupId;
        this.timestamp = timestamp;
        this.amount = amount;
        this.ownerName = ownerName;
        this.ownerId = ownerId;
        this.desc = desc;
        this.type = type;
    }
    @Generated(hash = 2097825908)
    public LocalExpense() {
    }
    public long getTimestamp() {
        return this.timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public float getAmount() {
        return this.amount;
    }
    public void setAmount(float amount) {
        this.amount = amount;
    }
    public String getOwnerName() {
        return this.ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
    public String getOwnerId() {
        return this.ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public String getDesc() {
        return this.desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public int getType() {
        return this.type;
    }
    public void setType(int type) {
        this.type = type;
    }
    @Generated(hash = 1288353610)
    private transient String group__resolvedKey;
    /** To-one relationship, resolved on first access. */
    @Generated(hash = 2110045930)
    public LocalGroup getGroup() {
        String __key = this.groupId;
        if (group__resolvedKey == null || group__resolvedKey != __key) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocalGroupDao targetDao = daoSession.getLocalGroupDao();
            LocalGroup groupNew = targetDao.load(__key);
            synchronized (this) {
                group = groupNew;
                group__resolvedKey = __key;
            }
        }
        return group;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 427512699)
    public void setGroup(LocalGroup group) {
        synchronized (this) {
            this.group = group;
            groupId = group == null ? null : group.getId();
            group__resolvedKey = groupId;
        }
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    public String getGroupId() {
        return this.groupId;
    }
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    @Override
    public String toString() {
        return "LocalExpense{" +
                "id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", amount=" + amount +
                ", ownerName='" + ownerName + '\'' +
                ", desc='" + desc + '\'' +
                ", type=" + type +
                '}';
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 932338305)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLocalExpenseDao() : null;
    }
}
