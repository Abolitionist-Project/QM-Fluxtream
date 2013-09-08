package com.quantimodo.etl;

import java.util.Map;
import java.util.HashMap;

public class HowAreYouFeelingConverter implements Converter {
  public static final HowAreYouFeelingConverter instance = new HowAreYouFeelingConverter();
  
  private static final QuantimodoRecord[] EMPTY_RESULT = new QuantimodoRecord[0];
  
  private HowAreYouFeelingConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("buttonconfiguration")) || (!databaseView.hasTable("buttonconfigurationlevel")) || (!databaseView.hasTable("feelinghistory"))) return null;
    
    final Map<Integer, String>  buttonScaleName = new HashMap<Integer, String>();
    final Map<Integer, String>  buttonUnit      = new HashMap<Integer, String>();
    final Map<Integer, Integer> buttonValue     = new HashMap<Integer, Integer>();
    {
      final Table scales  = databaseView.getTable("buttonconfiguration");
      if ((!scales.hasField("id")) || (!scales.hasField("selectionlabel"))) return null;
      
      final Table buttons = databaseView.getTable("buttonconfigurationlevel");
      if ((!buttons.hasField("id")) || (!buttons.hasField("configurationid")) || (!buttons.hasField("value"))) return null;
      
      final int scaleCount  = scales.getRecordCount();
      final int buttonCount = buttons.getRecordCount();
      for (int scaleNumber = 0; scaleNumber < scaleCount; scaleNumber++) {
        final int    scaleID   = (Integer) scales.getData(scaleNumber, "id");
        final String scaleName = (String)  scales.getData(scaleNumber, "selectionlabel");
        
        int scaleLow  = Integer.MAX_VALUE;
        int scaleHigh = Integer.MIN_VALUE;
        for (int buttonNumber = 0; buttonNumber < buttonCount; buttonNumber++) {
          if (scaleID == ((Integer) buttons.getData(buttonNumber, "configurationid"))) {
            int value = (Integer) buttons.getData(buttonNumber, "value");
            buttonValue.put(buttonNumber, value);
            buttonScaleName.put(buttonNumber, scaleName);
            
            scaleLow  = Math.min(scaleLow,  value);
            scaleHigh = Math.max(scaleHigh, value);
          }
        }
        
        final String unit = scaleLow + " to " + scaleHigh;
        for (int buttonNumber = 0; buttonNumber < buttonCount; buttonNumber++) {
          if (scaleID == ((Integer) buttons.getData(buttonNumber, "configurationid"))) buttonUnit.put(buttonNumber, unit);
        }
      }
    }
    
    final Table feelings = databaseView.getTable("feelinghistory");
    if ((!feelings.hasField("time")) || (!feelings.hasField("levelid"))) return null;
      
    final int recordCount = feelings.getRecordCount();
    final QuantimodoRecord[] results = new QuantimodoRecord[recordCount];
    
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final int  buttonID = (Integer) feelings.getData(recordNumber, "levelid");
      final long time     = (Long)    feelings.getData(recordNumber, "time");
      
      results[recordNumber] = new QuantimodoRecord("How Are You Feeling?", "mood", buttonScaleName.get(buttonID), true, false, false, buttonValue.get(buttonID), buttonUnit.get(buttonID), time, 0);
    }
    
    return results;
  }
}
