package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class LibDrawerItem implements DrawerItem {
    @Override
    public String getTitle() {
        return "Library drawer item";
    }
}
