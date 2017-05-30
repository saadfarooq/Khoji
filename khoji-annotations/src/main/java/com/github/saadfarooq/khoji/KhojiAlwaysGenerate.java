package com.github.saadfarooq.khoji;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Using this annotation will ensure that the interface is generated regardless
 * even if the underlying collection is empty.
 *
 * This will be useful for when certain interfaces are not specified for specifc
 * buildTypes
 */


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface KhojiAlwaysGenerate {
}
