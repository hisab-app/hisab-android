package io.github.zkhan93.hisab.model.notification;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;
import org.greenrobot.greendao.DaoException;

/**
 * Created by zeeshan on 12/28/2016.
 */
@Entity
public class LocalGroup {
    @Id
    private String id;
    private String name;
    @ToMany(referencedJoinProperty = "groupId")
    private List<LocalExpense> expenses;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 695784082)
    private transient LocalGroupDao myDao;

    @Generated(hash = 1863710438)
    public LocalGroup(String id, String name) {
        this.id = id;
        this.name = name;
    }
    @Generated(hash = 648436687)
    public LocalGroup() {
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 383477976)
    public List<LocalExpense> getExpenses() {
        if (expenses == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            LocalExpenseDao targetDao = daoSession.getLocalExpenseDao();
            List<LocalExpense> expensesNew = targetDao
                    ._queryLocalGroup_Expenses(id);
            synchronized (this) {
                if (expenses == null) {
                    expenses = expensesNew;
                }
            }
        }
        return expenses;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1330691212)
    public synchronized void resetExpenses() {
        expenses = null;
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
    @Override
    public String toString() {
        return "LocalGroup{" +
                "name='" + name + '\'' +
                ", expenses=" + getExpenses() +
                '}';
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1125109660)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getLocalGroupDao() : null;
    }
}
