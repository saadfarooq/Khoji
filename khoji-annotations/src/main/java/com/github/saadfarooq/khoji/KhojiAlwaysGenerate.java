package com.github.saadfarooq.khoji;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.SOURCE)
public @interface KhojiAlwaysGenerate {
    Class<?>[] parameters() default {};
}
