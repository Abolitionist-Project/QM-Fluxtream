package com.quantimodo.etl;

public class CardiographConverter implements Converter {
  public static final CardiographConverter instance = new CardiographConverter();
  
  private CardiographConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("history"))) return null;
    
    final Table table = databaseView.getTable("history");
    if ((!table.hasField("history_date")) || (!table.hasField("history_bpm"))) return null;
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final long  date = ((Number) table.getData(recordNumber, "history_date")).longValue();
      final int   bpm  = ((Number) table.getData(recordNumber, "history_bpm" )).intValue();
      
      result[recordNumber] = new QuantimodoRecord("Cardiograph", "vital sign", "heart rate", false, false, false, bpm, "bpm", date, 0);
    }
    
    return result;
  }
}
