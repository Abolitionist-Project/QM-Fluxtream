package com.quantimodo.etl;

public interface Converter {
  QuantimodoRecord[] convert(DatabaseView databaseView);
}

