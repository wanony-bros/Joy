package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Subreddit;

import java.util.Collection;

public class RealSubreddit implements Subreddit {
  @Key("accountsActive") public Integer accountsActive;
  @Key("comment_score_hide_mins") public Integer commentScoreHideMins;
  @Key public String description;
  @Key("description_html") public String descriptionHtml;
  @Key("display_name") public String displayName;
  @Key("header_img") public String headerImg;
  @Key("header_size") public Collection<Integer> headerSize;
  @Key("header_title") public String headerTitle;
  @Key public Boolean over18;
  @Key("public_description") public String publicDescription;
  @Key("public_traffic") public Boolean publicTraffic;
  @Key public Long subscribers;
  @Key("submission_type") public String submissionType;
  @Key("submit_link_label") public String submitLinkLabel;
  @Key("submit_text_label") public String submitTextLabel;
  @Key("subreddit_type") public String subredditType;
  @Key public String title;
  @Key public String url;
  @Key("user_is_banned") public Boolean userIsBanned;
  @Key("user_is_contributor") public Boolean userIsContributor;
  @Key("user_is_moderator") public Boolean userIsModerator;
  @Key("user_is_subscriber") public Boolean userIsSubscriber;
}
