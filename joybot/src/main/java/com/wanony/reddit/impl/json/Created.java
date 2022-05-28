package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;

public abstract class Created {
  @Key public Long created;
  @Key("created_utc") public Long createdUtc;
}
