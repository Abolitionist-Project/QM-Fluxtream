package com.quantimodo.etl;

public class SleepAsAndroidConverter implements Converter {
  public static final SleepAsAndroidConverter instance = new SleepAsAndroidConverter();
  
  private static final String[] REQUIRED_FIELD_NAMES = new String[] { "startTime", "toTime", "quality" };
  
  private SleepAsAndroidConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if (databaseView == null) return null;
    if (!databaseView.hasTable("records")) return null;
    
    final Table table = databaseView.getTable("records");
    
    for (int requiredFieldNumber = 1; requiredFieldNumber < REQUIRED_FIELD_NAMES.length; requiredFieldNumber++) {
      if (!table.hasField(REQUIRED_FIELD_NAMES[requiredFieldNumber])) return null;
    }
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final long   startTime = ((Number) table.getData(recordNumber, "startTime")).longValue();
      final long   toTime    = ((Number) table.getData(recordNumber, "toTime"   )).longValue();
      final long   duration  = toTime - startTime;
      final double quality   = ((Number) table.getData(recordNumber, "quality"  )).doubleValue();
      
      result[recordNumber] = new QuantimodoRecord("Sleep as Android", "sleep", "sleep quality", false, true, false, quality, "out of 1", startTime, (int) (((toTime - startTime)/500 + 1)/2));
    }
    
    return result;
  }
}
