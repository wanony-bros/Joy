package com.wanony.reddit.api.json;

import com.google.api.client.util.Key;

public abstract class Votable extends Created {
  @Key public Integer ups;
  @Key public Integer downs;
  @Key public Integer likes;
}
