package com.jakewharton.sdksearch.db;

import com.google.auto.value.AutoValue;
import com.squareup.sqldelight.EnumColumnAdapter;

@AutoValue
public abstract class Item implements ItemModel {
  public static final Factory<Item> FACTORY =
      new Factory<>(AutoValue_Item::new, EnumColumnAdapter.create(ItemType.class));
}
