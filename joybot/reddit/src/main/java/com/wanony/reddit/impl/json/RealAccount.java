package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Account;

public class RealAccount extends Created implements Account {
  @Key("comment_karma") public Integer commentKarma;
  @Key("has_mail") public Boolean hasMail;
  @Key("has_mod_mail") public Boolean hasModMail;
  @Key("has_verified_email") public Boolean hasVerifiedEmail;
  @Key public String id;
  @Key("is_friend") public Boolean isFriend;
  @Key("is_gold") public Boolean isGold;
  @Key("is_mod") public Boolean isMod;
  @Key("link_karma") public Integer linkKarma;
  @Key public String modHash;
  @Key public String name;
  @Key("over_18") public Boolean over18;
}
