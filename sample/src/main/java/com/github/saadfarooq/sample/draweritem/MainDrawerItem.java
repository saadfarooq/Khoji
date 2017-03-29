package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiTarget;

import java.util.List;

@KhojiTarget
public class MainDrawerItem implements DrawerItem {
    private final String title;

    public MainDrawerItem(String title, List<String> listString) {
        this.title = title;
    }

    @Override
    public String getTitle() {
        return title;
    }
}
