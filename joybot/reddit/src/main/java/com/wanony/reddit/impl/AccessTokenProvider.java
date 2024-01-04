package com.wanony.reddit.impl;


import com.google.api.client.http.*;
import com.google.api.client.util.ObjectParser;
import com.wanony.reddit.impl.json.AccessToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.wanony.reddit.impl.DefaultRedditClient.OAUTH_ACCESS_TOKEN_ENDPOINT;

public class AccessTokenProvider {
  @Nullable
  private AccessToken currentAccessToken;
  private long expiresAfter = 0L;

  @NotNull
  private final HttpTransport httpTransport;

  @NotNull
  private final ObjectParser objectParser;

  @NotNull
  private final String userAgent;

  @NotNull
  private final String apiKey;

  @NotNull
  private final String apiSecret;

  public AccessTokenProvider(
      @NotNull HttpTransport httpTransport,
      @NotNull ObjectParser objectParser,
      @NotNull String userAgent,
      @NotNull String apiKey,
      @NotNull String apiSecret
      ) {
    this.httpTransport = httpTransport;
    this.objectParser = objectParser;
    this.userAgent = userAgent;
    this.apiKey = apiKey;
    this.apiSecret = apiSecret;
  }

  @NotNull
  public synchronized String get() throws IOException {
    if (currentAccessToken == null || System.currentTimeMillis() > expiresAfter) {
      currentAccessToken = getNewAccessToken();
      expiresAfter = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(currentAccessToken.expiresIn);
    }

    return currentAccessToken.accessToken;
  }

  @NotNull
  private AccessToken getNewAccessToken() throws IOException {
    BasicAuthentication auth = new BasicAuthentication(apiKey, apiSecret);

    HttpRequestFactory requestFactory =
        httpTransport.createRequestFactory(
            request -> {
              auth.initialize(request);
              request.getHeaders().setUserAgent(userAgent);
              request.setParser(objectParser);
            }
        );

    Map<String, String> params = new HashMap<>(2);
    params.put("grant_type", "https://oauth.reddit.com/grants/installed_client");
    params.put("device_id", "DO_NOT_TRACK_THIS_DEVICE");

    HttpResponse response = requestFactory
        .buildPostRequest(new GenericUrl(OAUTH_ACCESS_TOKEN_ENDPOINT), new UrlEncodedContent(params))
        .execute();

    AccessToken token;
    try {
      if (response.isSuccessStatusCode()) {
        token = response.parseAs(AccessToken.class);
      } else {
        throw new RuntimeException("Request for access token failed:\n " + response.parseAsString());
      }
    } finally {
      response.disconnect();
    }

    return token;
  }
}