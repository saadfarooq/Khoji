package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class MainDrawerItem implements DrawerItem {
    @Override
    public String getTitle() {
        return "I'm always here";
    }
}
