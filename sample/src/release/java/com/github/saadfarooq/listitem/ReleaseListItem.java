package com.github.saadfarooq.listitem;


import com.github.saadfarooq.khoji.KhojiTarget;
import com.github.saadfarooq.sample.draweritem.ListItem;

@KhojiTarget
public class ReleaseListItem implements ListItem {
    @Override
    public String getListItemName() {
        return "Release List Item";
    }
}
