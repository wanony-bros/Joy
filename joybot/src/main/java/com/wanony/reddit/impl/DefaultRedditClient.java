package com.wanony.reddit.impl;

import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.ObjectParser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wanony.reddit.api.json.Listing;
import com.wanony.reddit.impl.json.RealThing;
import com.wanony.reddit.impl.json.ThingTypeAdapter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class DefaultRedditClient {
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    @NotNull
    public static final String OAUTH_ACCESS_TOKEN_ENDPOINT = "https://www.reddit.com/api/v1/access_token";

    // Currently, unused - we don't make use of user details
    @NotNull
    private static final String OAUTH_AUTHORIZE_ENDPOINT = "https://www.reddit.com/api/v1/authorize";

    private static final ObjectParser JSON = new JsonObjectParser(new GsonFactory());

    private static final Gson GSON = new GsonBuilder().registerTypeAdapterFactory(ThingTypeAdapter.FACTORY).create();

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

    @Nullable
    public Listing subreddit(@NotNull String subreddit) throws IOException {
        RealThing thing = request("https://oauth.reddit.com/r/" + subreddit +"/new", RealThing.class);
        return (thing != null) ? thing.forceListing() : null;
    }

    @Nullable
    private <T> T request(@NotNull String endPoint, @NotNull Class<T> expectedData) throws IOException {
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
                return GSON.fromJson(response.parseAsString(), expectedData);
            } else {
                throw new RuntimeException(
                        "Request at endpoint (" + endPoint + " failed with:  " + response.parseAsString()
                );
            }
        } finally {
            response.disconnect();
        }
    }
}
