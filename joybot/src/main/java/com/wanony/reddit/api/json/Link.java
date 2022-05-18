package com.wanony.reddit.api.json;

import com.google.api.client.util.Key;

public class Link extends Votable {
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
  @Key public Long edited;
  @Key public String distinguished;
  @Key public Boolean stickied;
}
