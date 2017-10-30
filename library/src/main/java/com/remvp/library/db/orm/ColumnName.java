package com.remvp.library.db.orm;

public class ColumnName {

   public String type;
   public String columnName;
   public boolean primaryKey;

   public ColumnName(String type, String columnName, boolean primaryKey) {
      this.type = type;
      this.columnName = columnName;
      this.primaryKey = primaryKey;
   }
}
