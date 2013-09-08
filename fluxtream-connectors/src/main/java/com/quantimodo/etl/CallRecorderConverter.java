package com.quantimodo.etl;

public class CallRecorderConverter implements Converter {
  public static final CallRecorderConverter instance = new CallRecorderConverter();
  
  private static final String[] REQUIRED_FIELD_NAMES = new String[] { "PhoneNumber", "Date", "Duration" };
  
  private CallRecorderConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if (databaseView == null) return null;
    
    final Table table; {
      final int tableNumber = databaseView.getTableNumber("recordings");
      table = databaseView.getTable((tableNumber == -1) ? 0 : tableNumber);
    }
    
    for (int requiredFieldNumber = 1; requiredFieldNumber < REQUIRED_FIELD_NAMES.length; requiredFieldNumber++) {
      if (!table.hasField(REQUIRED_FIELD_NAMES[requiredFieldNumber])) return null;
    }
    
    final int recordCount = table.getRecordCount();
    final QuantimodoRecord[] result = new QuantimodoRecord[recordCount];
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final String phoneNumber  =  (String) table.getData(recordNumber, "PhoneNumber");
      final long   startTime    = ((Number) table.getData(recordNumber, "Date" )).longValue();
      final int duration;
      try {
        final String durationText = (String) table.getData(recordNumber, "Duration");
        final int durationLength = durationText.length();
        final int hours   = Integer.parseInt(durationText.substring(0, durationLength - 6));
        final int minutes = Integer.parseInt(durationText.substring(durationLength - 5, durationLength - 3));
        final int seconds = Integer.parseInt(durationText.substring(durationLength - 2));
        duration = seconds + 60*(minutes + 60*hours);
      } catch (final NumberFormatException e) {
        return null;
      }
      
      result[recordNumber] = new QuantimodoRecord("Call Recorder", "telephone call", "telephone call with " + phoneNumber, true, true, true, duration, "s", startTime, duration);
    }
    
    return result;
  }
}
