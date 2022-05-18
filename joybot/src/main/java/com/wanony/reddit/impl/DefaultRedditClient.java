package com.wanony.reddit.impl;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ObjectParser;
import com.google.gson.GsonBuilder;
import com.wanony.reddit.api.json.Thing;
import com.wanony.reddit.impl.json.AccessToken;
import com.wanony.reddit.impl.json.ThingTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DefaultRedditClient {
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @NotNull
    public static final String OAUTH_ACCESS_TOKEN_ENDPOINT = "https://www.reddit.com/api/v1/access_token";

    // Currently, unused - we don't make use of user details
    @NotNull
    private static final String OAUTH_AUTHORIZE_ENDPOINT = "https://www.reddit.com/api/v1/authorize";

    private static final ObjectParser JSON = new JsonObjectParser(new GsonFactory());

    @NotNull
    private static final String USER_AGENT = "java:com.wanony:joy:dev (by /u/Slvinz)";

    @NotNull
    private final String apiKey;
    @NotNull
    private final String apiSecret;

    @NotNull
    private final AccessTokenProvider accessTokenProvider;

    public DefaultRedditClient(
        @NotNull String redditApiKey,
        @NotNull String redditApiSecret
    ) {
        this.apiKey = redditApiKey;
        this.apiSecret = redditApiSecret;
        this.accessTokenProvider = new AccessTokenProvider(HTTP_TRANSPORT, JSON, USER_AGENT, apiKey, apiSecret);
    }


    public Collection<String> subreddit() throws IOException {
        Thing t = request("https://oauth.reddit.com/r/cars/new", Thing.class);
        return Collections.emptyList();
    }

    @Nullable
    private <T> Thing request(@NotNull String endPoint, @NotNull Class<T> expectedData) throws IOException {
        GenericUrl url = new GenericUrl(endPoint);

        HttpRequestFactory requestFactory =
            HTTP_TRANSPORT.createRequestFactory(
                request -> {
                    String accessToken = accessTokenProvider.get();
                    request.getHeaders().setAuthorization("bearer " + accessToken);
                    request.setParser(JSON);
                }
            );

        HttpRequest request = requestFactory.buildGetRequest(url);

        HttpResponse response = request.execute();
        try {
            if (response.isSuccessStatusCode()) {
                System.out.println(response.parseAsString());
                return new GsonBuilder().registerTypeAdapter(Thing.class, new ThingTypeAdapter()).create().fromJson(response.parseAsString(), Thing.class);
            } else {
                throw new RuntimeException("Request for access token failed: " + response.parseAsString());
            }
        } finally {
            response.disconnect();
        }
    }
}
