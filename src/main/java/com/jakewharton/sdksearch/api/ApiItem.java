package com.jakewharton.sdksearch.api;

public final class ApiItem {
  public final long id;
  public final String label;
  public final String link;
  public final String type;

  ApiItem(long id, String label, String link, String type) {
    this.id = id;
    this.label = label;
    this.link = link;
    this.type = type;
  }

  @Override public String toString() {
    return "ApiItem{"
        + "id="
        + id
        + ", label='"
        + label
        + '\''
        + ", link='"
        + link
        + '\''
        + ", type='"
        + type
        + '\''
        + '}';
  }
}
