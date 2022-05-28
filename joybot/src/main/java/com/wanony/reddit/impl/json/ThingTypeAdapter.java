package com.wanony.reddit.impl.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.wanony.reddit.impl.Kind;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

public class ThingTypeAdapter extends TypeAdapter<RealThing> {
  public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
      if (!(type.getType().equals(RealThing.class))) {
        return null;
      }

      //noinspection unchecked
      return (TypeAdapter<T>) new ThingTypeAdapter(gson);
    }
  };

  public Gson context;

  public ThingTypeAdapter(@NotNull Gson gson) {
    context = gson;
  }

  @Override
  public void write(JsonWriter out, RealThing value) throws IOException {
    context.getAdapter(Object.class).write(out, value);
  }

  @Override
  public RealThing read(JsonReader in) throws IOException {
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

    RealThing thing = new RealThing();
    thing.kind = kind;
    Kind k = Arrays.stream(Kind.values()).filter((o) -> Kind.is(kind, o)).findFirst().orElse(null);
    if (k != null) {
      thing.data = context.getAdapter(k.getType()).read(in);
    }
    if (k == null) {
      throw new RuntimeException("Unexpected data found! Kind: " + kind);
    }

    in.endObject();
    return thing;
  }
}
