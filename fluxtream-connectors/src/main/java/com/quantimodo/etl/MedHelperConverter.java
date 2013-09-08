package com.quantimodo.etl;

import java.util.Map;
import java.util.HashMap;

public class MedHelperConverter implements Converter {
  public static final MedHelperConverter instance = new MedHelperConverter();
  
  private static final String[] unitTypes = new String[] { "tablets", "mg", "mL", "drops", "applications", "mcg", "units", "puffs", "sprays", "capsules", "g" };
  
  private MedHelperConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("prescription")) || (!databaseView.hasTable("doselog"))) return null;
    
    final Map<Long, String> prescriptionNames = new HashMap<Long, String>();
    final Map<Long, String> prescriptionUnits = new HashMap<Long, String>();
    {
      final Table prescriptionTable = databaseView.getTable("prescription");
      if ((!prescriptionTable.hasField("_id")) || (!prescriptionTable.hasField("name")) || (!prescriptionTable.hasField("inventorytype"))) return null;
      
      final int recordCount = prescriptionTable.getRecordCount();
      for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
        final long id = ((Number) prescriptionTable.getData(recordNumber, "_id")).longValue();
        prescriptionNames.put(id, (String) prescriptionTable.getData(recordNumber, "name"));
        prescriptionUnits.put(id, unitTypes[((Number) prescriptionTable.getData(recordNumber, "inventorytype")).intValue()]);
      }
    }
    
    final Table table = databaseView.getTable("doselog");
    if ((!table.hasField("prescriptionid")) || (!table.hasField("actualtime")) || (!table.hasField("actualdosage"))) return null;
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final long prescriptionID = ((Number) table.getData(recordNumber, "prescriptionid")).longValue();
      
      final String name = prescriptionNames.get(prescriptionID);
      final double dose = ((Number) table.getData(recordNumber, "actualdosage")).doubleValue();
      final String unit = prescriptionUnits.get(prescriptionID);
      final long   time = ((Number) table.getData(recordNumber, "actualtime"  )).longValue();
      
      result[recordNumber] = new QuantimodoRecord("Med Helper", "medicine", name, true, true, true, dose, unit, time, 0);
    }
    
    return result;
  }
}
