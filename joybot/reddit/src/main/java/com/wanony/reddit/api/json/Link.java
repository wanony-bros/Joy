package com.wanony.reddit.api.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Link {
    @NotNull
    String id();

    @Nullable // Is actually nullable?
    String url();

    @NotNull
    String name();

    @Nullable
    String author();

    @Nullable
    String selftext();

    @Nullable
    String thumbnail();

    @Nullable
    String subreddit();

    @Nullable
    String title();

    @NotNull
    String permalink();

}
