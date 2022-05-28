package com.wanony.reddit.impl;

import com.wanony.reddit.impl.json.*;
import org.jetbrains.annotations.NotNull;

public enum Kind {
    LISTING("Listing", RealListing.class),
    COMMENT("t1", RealComment.class),
    ACCOUNT("t2", RealAccount.class),
    LINK("t3", RealLink.class),
    SUBREDDIT("t4", RealSubreddit.class),
    AWARD("t5", Object.class),
    ;

    @NotNull
    private final String kind;
    @NotNull
    private final Class<?> type;

    Kind(@NotNull String kind, @NotNull Class<?> listingClass) {
        this.kind = kind;
        type = listingClass;
    }

    @NotNull
    public Class<?> getType() {
        return type;
    }

    public static boolean is(@NotNull String string, @NotNull Kind kind) {
        return string.equals(kind.kind);
    }

    public static void ensure(@NotNull String string, @NotNull Object obj, @NotNull Kind kind) {
        if (kind.kind.equals(string) && kind.type.equals(obj.getClass())) {
            // All good boss
            return;
        }

        throw new RuntimeException("Object is not of type " + kind.kind + ". Object: " + obj);
    }
}
