Khoji
======

Khoji finds all classes annotated with `@KhojiTarget` at compile time and generates a class for you to access them from.
The primary use case for this was to be able to selectively include DrawerItems in an Android drawer menu based on 
conditions and source-sets used.

* Create an interface you want to use, say `DrawerItem`
* Implement the interface on classes and annotated with `@KhojiTarget`
* Khoji generates a class called `DrawerItemCollection` takes in all dependencies and exposes the list of items through
a method `List<DrawerItemModel> getCollectedItems() {}`

In the drawer item example, we use some external data and BuildConfig information to determine which drawer items should
be visible.

Usage
-----------
Consider the following interface:
```java
public interface DrawerItem {
    boolean isVisible();
}
```

Create implementations like so:
```java
@KhojiTarget
public class DebugOnlyDrawerItem {
    @Override
    boolean isVisible() {
        return BuildConfig.DEBUG;
    }
}

@KhojiTarget
public class ParticularUserDrawerItem {
    private final User user;

    public ParticularUserDrawerItem(User user) {
        this.user = user;
    }

    @Override
    boolean isVisible() {
        return user.isParticularUser(); 
    }
}
```

The class generate is in the same package as the interface and is called `DrawerItemCollection`. It takes all 
implementation class dependencies through it's constructor so it might be used as.

```java
        User user = getUserFromSomeWhere();
        DrawerItemCollection drawerItemCollection = new DrawerItemCollection(user);
        List<DrawerItem> visibleItems = rx.Observable.from(drawerItemCollection.getCollectedItems()).filter(item::isVisible).toBlocking().single();
        drawerList.setAdapter(new DrawerListItemAdapter(visibleItems));
```

Now you can use, for example, a debug source set which holds a certain feature, the drawer item to access that feature 
will only be available in builds which have that source set enabled.

Installation
------------
```groovy
buildscript {
    repositories {
        jcenter() // Also available in maven central
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
    }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    apt 'com.github.saadfarooq:khoji-compiler:0.0.2'
    compile 'com.github.saadfarooq:khoji-annotations:0.0.2'
}
```

Snapshots of the development version are available in [Sonatype's snapshots repository][snapshots].


License
-------

    Copyright 2016 Saad Farooq

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

 [snapshots]: https://oss.sonatype.org/content/repositories/snapshots/

Dislaimer
---------
Most setup copied from Zac Sweers' Barber project (https://github.com/hzsweers/barber)