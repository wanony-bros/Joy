package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Link;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RealLink extends Votable implements Link {
  @Key public String id;
  @Key public String name;
  @Key public String author;
  @Key("author_flair_css_class") public String authorFlairCSSClass;
  @Key("author_flair_text") public String authorFlairText;
  @Key public Boolean clicked;
  @Key public String domain;
  @Key public Boolean hidden;
  @Key("is_self") public Boolean isSelf;
  @Key public Boolean likes;
  @Key("link_flair_css_class") public String linkFlairCSSClass;
  @Key("link_flair_text") public String linkFlairText;
  @Key public Boolean locked;
  @Key public Object media;
  @Key("media_embed") public Object mediaEmbed;
  @Key("num_comments") public Integer numComments;
  @Key("over_18") public Boolean over18;
  @Key public String permalink;
  @Key public Boolean saved;
  @Key public Integer score;
  @Key public String selftext;
  @Key("selftext_html") public String selftextHtml;
  @Key public String subreddit;
  @Key("subredddit_id") public String subredditId;
  @Key public String thumbnail;
  @Key public String url;
  // This can be a number or boolean! How fun! Work out how to deal with this (read as string maybe?)
  //@Key public Boolean edited;
  @Key public String distinguished;
  @Key public Boolean stickied;

  @Override
  @Nullable // Is actually nullable?
  public String url() {
    return url;
  }

  @Override
  @NotNull
  public String name() {
    return name;
  }
}
