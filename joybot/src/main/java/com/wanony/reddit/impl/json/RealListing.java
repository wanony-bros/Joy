package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Listing;

import java.util.Collection;

public class RealListing implements Listing {
  @Key public String before;
  @Key public String after;
  @Key public String modHash;
  @Key public Integer dist;
  @Key("geo_filter") public String geoFilter;
  @Key public Collection<RealThing> children;


}
