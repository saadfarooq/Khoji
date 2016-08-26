package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

@KhojiTarget
public class VanillaDrawerItem implements DrawerItem {

    @Override
    public String getTitle() {
        return "Vanilla";
    }
}
