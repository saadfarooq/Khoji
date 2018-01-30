package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class WhiteChocolateDrawerItem implements DrawerItem {
    private final String name;

    public WhiteChocolateDrawerItem(String name) {
        this.name = name;
    }

    @Override
    public String getTitle() {
        return name;
    }
}
