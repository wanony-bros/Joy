package com.wanony.reddit.samples;

import com.wanony.JoyBotKt;
import com.wanony.reddit.api.json.Link;
import com.wanony.reddit.api.json.Listing;
import com.wanony.reddit.impl.DefaultRedditClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SubredditSample {
  public static void main(@NotNull String[] args) throws IOException {
    String token = JoyBotKt.getProperty("redditToken");
    String secret = JoyBotKt.getProperty("redditSecret");

    DefaultRedditClient reddit = new DefaultRedditClient(token, secret);
    Listing listing = reddit.subreddit("TwoXChromosomes");

    assert listing != null;

    for (Link l : listing.getLinks()) {
      System.out.println(l.url());
    }
  }
}
