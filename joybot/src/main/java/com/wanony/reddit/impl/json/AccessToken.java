package com.wanony.reddit.impl.json;

import com.google.api.client.util.Key;

public class AccessToken {
  @Key("access_token") public String accessToken;
  @Key("token_type") public String tokenType;
  @Key("device_id") public String deviceId;
  @Key("expires_in") public Integer expiresIn;
  @Key public String scope;
}
