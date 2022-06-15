package com.wanony.reddit.api;

import com.wanony.reddit.api.json.Listing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URISyntaxException;

public interface RedditClient {
    @Nullable Listing subreddit(@NotNull String subreddit) throws IOException, URISyntaxException;

    @Nullable Listing subreddit(@NotNull String sub, @Nullable String lastSent) throws IOException, URISyntaxException;
}
