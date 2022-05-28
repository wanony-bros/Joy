package com.wanony.reddit.samples;

import com.wanony.reddit.api.json.Listing;
import com.wanony.reddit.impl.DefaultRedditClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SubredditSample {
  public static void main(@NotNull String[] args) throws IOException {
    String token = "siXd0IUw6v_ZGws_5HKTjw";
    String secret = "FBP7pBE-l0iG-IPsdKgVaR02Qx4t9w";

    DefaultRedditClient reddit = new DefaultRedditClient(token, secret);
    Listing listing = reddit.subreddit("TwoXChromosomes");

    System.out.println(listing);
  }
}
