package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class ChocolateDrawerItem implements DrawerItem {

    @Override
    public String getTitle() {
        return "Chocolate";
    }
}
