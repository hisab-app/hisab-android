package io.github.zkhan93.hisab.model.callback;

import java.util.Map;

/**
 * Created by zeeshan on 2/26/2017.
 */

public interface NotificationCountLoadClbk {
    public void onNotificationCountLoaded(Map<String, Integer> notificationCountMap);
}
