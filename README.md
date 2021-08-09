Khoji (Archived)
======

Primary use case was Android which has progressively moved to a modular structure and annotation processors can't proccess modular dependencies.
-----

Khoji finds all classes annotated with `@KhojiTarget` at compile time and generates a class for you to access them from.

The primary use case for this is to be able to selectively include UI elements, such as DrawerItems
 or settings tiles, in an Android drawer menu based on conditions (e.g. debug or release) and source-sets used.
This avoids checking for these conditions everywhere in your source code.

* Create an interface you want to use, say `DrawerItem`
* Implement the interface on classes and annotated with `@KhojiTarget`
* Khoji generates a class called `DrawerItemCollection` takes in all dependencies and exposes the list of items through
a method `List<DrawerItemModel> getCollectedItems() {}`

In the drawer item example, we use some external data and BuildConfig information to determine which drawer items should
be visible.

Usage
------
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

The class generated is in the same package as the interface and is called `DrawerItemCollection`. It takes all 
implementation class dependencies through it's constructor so it might be used as.

```java
        User user = getUserFromSomeWhere();
        DrawerItemCollection drawerItemCollection = new DrawerItemCollection(user);
        List<DrawerItem> visibleItems = rx.Observable.from(drawerItemCollection.getCollectedItems()).filter(item::isVisible).toBlocking().single();
        drawerList.setAdapter(new DrawerListItemAdapter(visibleItems));
```

Now you can use, for example, a debug source set which holds a certain feature, the drawer item to access that feature 
will only be available in builds which have that source set enabled.

#### Special Cases
What if you only have implementations of the interface in some source sets and not in others? Or what 
if the generated class's dependencies are different between two source sets?

This could occur, for example, if you have some services that you start in debug but none of who have
 made it to release yet. The `DiagnosisServiceCollection(dep1, dep2).getCollectedItems()` call would 
 give a compile error because `DiagnosisServiceCollection` is not generated for release since there 
 are `@KhojiTarget` annotated `DiagnosisService` implementations in release yet.
  
  In that case, you can use the `@KhojiAlwaysGenerate` annotation on the interface itself and define
 it's dependencies on the annotation. With `@KhojiAlwaysGenerate` annotated interface, if an
 annotated implementation of the interface exists in a source path, everything proceeds as normal.
 But in the case where there's no implementation, the collection class will be generated from the 
 signature of the `@KhojiAlwaysGenerate` annotation with `getColledtedItems()` returning an empty list.
 
 ```java
    @KhojiAlwaysGenerate(
            parameters = { Dep1.class, Dep2.class }
    )
    public interface DiagnosisService {
        void doSomething(Application application);
    }
     
 ```

The above will generate the same signature as the source path with implementations i.e.
`DiagnosisServiceCollection(dep1, dep2).getCollectedItems()` so the usage code is exactly the same
regardless of build type.

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

apply plugin: 'com.neenbedankt.android-apt' // not required for newer version of Android gradle

dependencies {
    apt 'com.github.saadfarooq:khoji-compiler:0.0.5'
    compile 'com.github.saadfarooq:khoji-annotations:0.0.5'
}
```



License
-------

    Copyright 2017 Saad Farooq

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
