package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class DarkChocolateDrawerItem implements DrawerItem {
    private final String name;

    public DarkChocolateDrawerItem(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return name;
    }
}
