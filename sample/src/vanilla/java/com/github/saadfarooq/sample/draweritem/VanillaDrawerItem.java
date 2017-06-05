package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

import java.util.List;

public class VanillaDrawerItem implements DrawerItem {
    private final String title;

    public VanillaDrawerItem(String title, List<String> list) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
