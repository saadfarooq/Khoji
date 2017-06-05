package com.github.saadfarooq.sample.draweritem;

import com.github.saadfarooq.khoji.KhojiAlwaysGenerate;

@KhojiAlwaysGenerate(
        parameters = {
                String.class
        }
)
public interface DrawerItem {
    String getTitle();
}
