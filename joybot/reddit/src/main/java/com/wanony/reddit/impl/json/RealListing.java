package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;
import com.wanony.reddit.api.json.Link;
import com.wanony.reddit.api.json.Listing;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RealListing implements Listing {
  @Key public String before;
  @Key public String after;
  @Key public String modHash;
  @Key public Integer dist;
  @Key("geo_filter") public String geoFilter;
  @Key public Collection<RealThing> children;


  @Override
  public @NotNull List<Link> getLinks() {
    return children.stream().map(RealThing::asLink).filter(Objects::nonNull).collect(Collectors.toList());
  }
}
