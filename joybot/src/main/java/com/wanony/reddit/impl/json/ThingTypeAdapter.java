package com.wanony.reddit.impl.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wanony.reddit.api.json.Thing;

import java.io.IOException;

public class ThingTypeAdapter extends TypeAdapter<Thing> {
  @Override
  public void write(JsonWriter out, Thing value) throws IOException {

  }

  @Override
  public Thing read(JsonReader in) throws IOException {
    if (in.peek() == JsonToken.NULL) {
      in.nextNull();
      return null;
    }

    in.beginObject();
    String name = in.nextName();
    if (!name.equals("kind")) {
      throw new RuntimeException("Kind is not first!");
    }

    String kind = in.nextString();

    String dataName = in.nextName();
    if (!dataName.equals("data")) {
      throw new RuntimeException("Expected data found " + dataName);
    }

    in.skipValue();

    in.endObject();
    return null;
  }
}
