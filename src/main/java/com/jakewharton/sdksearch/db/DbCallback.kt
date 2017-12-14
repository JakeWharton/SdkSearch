package com.jakewharton.sdksearch.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.db.SupportSQLiteOpenHelper

private const val VERSION = 1

class DbCallback : SupportSQLiteOpenHelper.Callback(VERSION) {
  override fun onCreate(db: SupportSQLiteDatabase) {
    db.execSQL(ItemModel.CREATE_TABLE)
  }

  override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
    throw UnsupportedOperationException()
  }
}
