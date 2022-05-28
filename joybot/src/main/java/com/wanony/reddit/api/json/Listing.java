package com.wanony.reddit.api.json;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Listing {
    @NotNull
    List<Link> getLinks();
}
