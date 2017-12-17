package com.jakewharton.sdksearch.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Item implements ItemModel {
  public static final Factory<Item> FACTORY = new Factory<>(AutoValue_Item::new);

  public static Item createForInsert(String listing, String label, String link) {
    return new AutoValue_Item(-1, listing, label, false, link);
  }
}
