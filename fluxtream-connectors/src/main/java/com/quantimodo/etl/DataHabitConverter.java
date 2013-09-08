package com.quantimodo.etl;

import java.util.Map;
import java.util.HashMap;

public class DataHabitConverter implements Converter {
  public static final DataHabitConverter instance = new DataHabitConverter();
  
  private static final String[]  unitTypes = new String[]  { "out of 5", "out of 100", "(raw number)", "yes/no", "occurrence", "s"  };
  private static final boolean[] summable  = new boolean[] { false,      false,        true,           false,    true,         true };
  
  private DataHabitConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("trackers")) || (!databaseView.hasTable("data"))) return null;
    
    final Map<Long, String>  trackerNames = new HashMap<Long, String>();
    final Map<Long, Integer> trackerUnits = new HashMap<Long, Integer>();
    {
      final Table trackerTable = databaseView.getTable("trackers");
      if ((!trackerTable.hasField("_id")) || (!trackerTable.hasField("TrackerName")) || (!trackerTable.hasField("TrackerType"))) return null;
      
      final int recordCount = trackerTable.getRecordCount();
      for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
        final long id = ((Number) trackerTable.getData(recordNumber, "_id")).longValue();
        trackerNames.put(id, (String) trackerTable.getData(recordNumber, "TrackerName"));
        trackerUnits.put(id, ((Number) trackerTable.getData(recordNumber, "TrackerType")).intValue());
      }
    }
    
    final Table table = databaseView.getTable("data");
    if ((!table.hasField("TrackerID")) || (!table.hasField("Timestamp")) || (!table.hasField("Value"))) return null;
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final long trackerID = ((Number) table.getData(recordNumber, "TrackerID")).longValue();
      
      final String  name       = trackerNames.get(trackerID);
      final double  value      = ((Number) table.getData(recordNumber, "Value")).doubleValue();
      final int     unitID     = trackerUnits.get(trackerID);
      final String  unitName   = unitTypes[unitID];
      final boolean isSummable = summable[unitID];
      final long    time       = Long.parseLong((String) table.getData(recordNumber, "Timestamp"));
      
      result[recordNumber] = new QuantimodoRecord("DataHabit", "(unknown)", name, true, null, isSummable, value, unitName, time, 0);
    }
    
    return result;
  }
}
