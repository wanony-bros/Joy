package com.wanony.reddit.api.json;

import com.google.api.client.util.Key;

public abstract class Thing {
  @Key public String id;
  @Key public String name;
  @Key public String kind;
}
