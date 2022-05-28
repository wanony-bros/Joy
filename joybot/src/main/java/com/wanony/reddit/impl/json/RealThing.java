package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Listing;
import com.wanony.reddit.api.json.Thing;
import com.wanony.reddit.impl.Kind;
import org.jetbrains.annotations.NotNull;

import static com.wanony.reddit.impl.Kind.LISTING;

public class RealThing implements Thing {
  @Key public String kind;
  @Key public Object data;

  @NotNull
  public Listing forceListing() {
    Kind.ensure(kind, data, LISTING);
    return (Listing) data;
  }
}
