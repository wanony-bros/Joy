package com.wanony.reddit.api.json;


import com.google.api.client.util.Key;

import java.util.Collection;

public class Comment extends Votable {
  @Key("approved_by") public String approvedBy;
  @Key public String author;
  @Key("author_flair_css_class") public String authorFlairCSSClass;
  @Key("author_flair_text") public String authorFlairText;
  @Key("banned_by") public String bannedBy;
  @Key public String body;
  @Key("body_html") public String bodyHtml;
  @Key public String special;
  @Key public Integer gilded;
  @Key public Boolean likes;
  @Key("link_author") public String linkAuthor;
  @Key("link_id") public String linkId;
  @Key("link_title") public String linkTitle;
  @Key("link_url") public String linkUrl;
  @Key("num_reports") public String numReports;
  @Key("parent_id") public String parentId;
  @Key public Collection<Thing> replies;
  @Key public Boolean saved;
  @Key public Integer score;
  @Key("score_hidden") public Boolean scoreHidden;
  @Key public String subreddit;
  @Key("subreddit_id") public String subRedditId;
  @Key public String distinguished;
}
