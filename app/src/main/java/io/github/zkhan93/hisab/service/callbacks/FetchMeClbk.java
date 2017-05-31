package io.github.zkhan93.hisab.service.callbacks;

import io.github.zkhan93.hisab.model.User;

/**
 * Created by zeeshan on 1/8/2017.
 */

public interface FetchMeClbk {
    public void onMeFetched(User user);
}
