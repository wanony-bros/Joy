package com.wanony.reddit.api.json;

import org.jetbrains.annotations.Nullable;

public interface Link {
    @Nullable // Is actually nullable?
    String url();
}
