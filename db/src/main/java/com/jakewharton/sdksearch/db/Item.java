package com.jakewharton.sdksearch.db;

import com.google.auto.value.AutoValue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AutoValue
public abstract class Item implements ItemModel {
  public static final Factory<Item> FACTORY = new Factory<>(AutoValue_Item::new);
  private static final Pattern PACKAGE = Pattern.compile("^([a-z0-9]+.)+");

  public static Item createForInsert(String listing, String fqcn, String link, boolean deprecated) {
    Matcher matcher = PACKAGE.matcher(fqcn);
    if (!matcher.lookingAt()) {
      throw new IllegalArgumentException("FQCN '" + fqcn + "' doesn't appear to be valid.");
    }
    String packageName = fqcn.substring(0, matcher.end() - 1);
    String className = fqcn.substring(matcher.end());
    return new AutoValue_Item(-1, listing, packageName, className, deprecated, link);
  }
}
