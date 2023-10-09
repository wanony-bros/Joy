package com.wanony.reddit.samples;

import com.wanony.joy.discord.JoyBotKt;
import com.wanony.reddit.api.json.Link;
import com.wanony.reddit.api.json.Listing;
import com.wanony.reddit.impl.DefaultRedditClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class SubredditSample {
  public static void main(@NotNull String[] args) throws IOException, URISyntaxException {
    String token = JoyBotKt.getProperty("redditToken");
    String secret = JoyBotKt.getProperty("redditSecret");

    DefaultRedditClient reddit = new DefaultRedditClient(token, secret);
    Listing listing = reddit.subreddit("TwoXChromosomes");

    assert listing != null;

    for (Link l : listing.getLinks()) {
      System.out.println(l.url());
    }

    String newestId = listing.getLinks().get(0).name();
    System.out.println(newestId);

    listing = reddit.subreddit("TwoXChromosomes", newestId);

    assert listing != null;

    List<Link> list = listing.getLinks();
    System.out.println(list);
  }
}
