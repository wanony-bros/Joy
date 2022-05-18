package com.wanony.reddit.api.json;

import com.google.api.client.util.Key;

import java.util.Collection;

public class Listing {
  @Key public String before;
  @Key public String after;
  @Key public String modHash;
  @Key public Integer dist;
  @Key("geo_filter") public String geoFilter;
  @Key public Collection<Thing> children;
}
