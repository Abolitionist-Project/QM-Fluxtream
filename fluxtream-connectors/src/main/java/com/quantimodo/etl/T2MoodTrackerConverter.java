package com.quantimodo.etl;

import java.util.Map;
import java.util.HashMap;

public class T2MoodTrackerConverter implements Converter {
  public static final T2MoodTrackerConverter instance = new T2MoodTrackerConverter();
  
  private T2MoodTrackerConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("scale")) || (!databaseView.hasTable("result"))) return null;
    
    final Map<Long, String> scaleNames = new HashMap<Long, String>(); {
      final Table scaleTable = databaseView.getTable("scale");
      if ((!scaleTable.hasField("_id")) || (!scaleTable.hasField("min_label")) || (!scaleTable.hasField("max_label"))) return null;
      
      final int recordCount = scaleTable.getRecordCount();
      for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
        final long id = ((Number) scaleTable.getData(recordNumber, "_id")).longValue();
        scaleNames.put(id, String.format("%s/%s", (String) scaleTable.getData(recordNumber, "min_label"), (String) scaleTable.getData(recordNumber, "max_label")));
      }
    }
    
    final Table table = databaseView.getTable("result");
    if ((!table.hasField("scale_id")) || (!table.hasField("timestamp")) || (!table.hasField("value"))) return null;
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final long scaleID = ((Number) table.getData(recordNumber, "scale_id")).longValue();
      
      final String  name       = scaleNames.get(scaleID);
      final byte    value      = ((Number) table.getData(recordNumber, "value")).byteValue();
      final long    time       = ((Number) table.getData(recordNumber, "timestamp")).longValue();
      
      result[recordNumber] = new QuantimodoRecord("T2 Mood Tracker", "mood", name, false, false, false, value, "out of 100", time, 0);
    }
    
    return result;
  }
}
