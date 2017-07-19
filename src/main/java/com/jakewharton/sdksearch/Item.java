package com.jakewharton.sdksearch;

final class Item {
  final long id;
  final String label;
  final String link;
  final String type;

  Item(long id, String label, String link, String type) {
    this.id = id;
    this.label = label;
    this.link = link;
    this.type = type;
  }

  @Override public String toString() {
    return "Item{"
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
