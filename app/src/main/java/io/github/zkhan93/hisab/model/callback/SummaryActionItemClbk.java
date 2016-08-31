package io.github.zkhan93.hisab.model.callback;

import java.util.Map;

/**
 * Created by Zeeshan Khan on 7/10/2016.
 */
public interface SummaryActionItemClbk {
    void archiveGrp(String groupId, Map<String,Object> expenses);
    void moreInfo();
}
