package com.wanony.reddit.api.json;

import com.google.api.client.util.Key;

public class Message extends Created {
  @Key public String author;
  @Key public String body;
  @Key("body_html") public String bodyHtml;
  @Key public String context;
  @Key("first_message") public Message firstMessage;
  @Key("first_message_name") public String firstMessageName;
  @Key public Boolean likes;
  @Key("link_title") public String linkTitle;
  @Key public String name;
  @Key("new") public Boolean isNew;
  @Key("parent_id") public String parentID;
  @Key public String replies;
  @Key public String subject;
  @Key public String subreddit;
  @Key("was_comment") public Boolean wasComment;
}
