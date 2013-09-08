package com.quantimodo.etl;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import java.util.Map;
import java.util.HashMap;

public class ZombiesRunConverter implements Converter {
  public static final ZombiesRunConverter instance = new ZombiesRunConverter();
  
  private static final SimpleDateFormat   dateParser   = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
  
  private ZombiesRunConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("runrecord"))) return null;
    
    final Table runs = databaseView.getTable("runrecord");
    if ((!runs.hasField("distance")) || (!runs.hasField("started")) || (!runs.hasField("ended"))) return null;
      
    final int runCount = runs.getRecordCount();
    final QuantimodoRecord[] results = new QuantimodoRecord[runCount];
    
    for (int runNumber = 0; runNumber < runCount; runNumber++) {
      final int  distance  = (Integer) runs.getData(runNumber, "distance");
      final Long startTime = ParseUtil.parseNanoTime((String) runs.getData(runNumber, "started"));
      if (startTime == null) return null;
      final Long endTime   = ParseUtil.parseNanoTime((String) runs.getData(runNumber, "ended"  ));
      if (endTime == null) return null;
      
      results[runNumber] = new QuantimodoRecord("Zombies, Run!", "activity", "walk/run distance", false, true, true, distance, "m", startTime, (int) ((endTime - startTime + 500)/1000));
    }
    
    return results;
  }
}
