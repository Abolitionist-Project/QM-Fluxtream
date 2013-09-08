package com.quantimodo.etl;

import java.util.GregorianCalendar;

import java.util.List;
import java.util.ArrayList;

public class SportsTrackerConverter implements Converter {
  public static final SportsTrackerConverter instance = new SportsTrackerConverter();
  
  private static final String[] REQUIRED_FIELD_NAMES = new String[] { "distance", "date_time", "duration", "bpm", "breathing_rate", "skin_temp" };
  private static final QuantimodoRecord[] EMPTY_RESULT = new QuantimodoRecord[0];
  
  private SportsTrackerConverter() {}
  
  public QuantimodoRecord[] convert(final DatabaseView databaseView) {
    if ((databaseView == null) || (!databaseView.hasTable("TrackPoint"))) return null;
    
    final Table table = databaseView.getTable("TrackPoint");
    for (int requiredFieldNumber = 1; requiredFieldNumber < REQUIRED_FIELD_NAMES.length; requiredFieldNumber++) {
      if (!table.hasField(REQUIRED_FIELD_NAMES[requiredFieldNumber])) return null;
    }
    
    final int recordCount = table.getRecordCount();
    if (recordCount == 0) return EMPTY_RESULT;
    
    final List<QuantimodoRecord> results = new ArrayList<QuantimodoRecord>(4 * recordCount);
    
    int    lastTrackID         = Integer.MIN_VALUE;
    double lastDistance        = 0.0;
    int    lastHeartRate       = 0;
    double lastBreathingRate   = 0.0;
    double lastSkinTemperature = 0.0;
    long   lastTime            = Long.MIN_VALUE;
    int    lastDuration        = 0;
    for (int recordNumber = 0; recordNumber < recordCount; recordNumber++) {
      final int    trackID         = ((Number) table.getData(recordNumber, "track_id"      )).intValue();
      final double distance        = ((Number) table.getData(recordNumber, "distance"      )).doubleValue();
      final int    heartRate       = ((Number) table.getData(recordNumber, "bpm"           )).intValue();
      final double breathingRate   = ((Number) table.getData(recordNumber, "breathing_rate")).doubleValue();
      final double skinTemperature = ((Number) table.getData(recordNumber, "skin_temp"     )).doubleValue();
      final long   time            = ((Number) table.getData(recordNumber, "date_time"     )).longValue();
      final int    duration        = ((Number) table.getData(recordNumber, "duration"      )).intValue();
      
      final long   outputTime;
      final double outputDistance;
      final int    outputDuration;
      if (trackID == lastTrackID) {
        outputTime     = lastTime;
        outputDistance = distance - lastDistance;
        outputDuration = duration - lastDuration;
      }
      else {
        outputTime     = time - 1000*duration;
        outputDistance = distance;
        outputDuration = duration;
      }
      
      if (outputDistance  > 0.0) results.add(new QuantimodoRecord("SportsTracker", "activity",   "walk/run distance", false, true,  true,  outputDistance,     "m", outputTime, outputDuration));
      if (heartRate       > 0  ) results.add(new QuantimodoRecord("SportsTracker", "vital sign", "heart rate",        false, false, false, heartRate,          "bpm", outputTime, outputDuration));
      if (breathingRate   > 0.0) results.add(new QuantimodoRecord("SportsTracker", "vital sign", "breathing rate",    false, false, false, breathingRate/60.0, "Hz", outputTime, outputDuration));
      if (skinTemperature > 0.0) results.add(new QuantimodoRecord("SportsTracker", "vital sign", "temperature",       false, false, false, skinTemperature,    "\u00b0C", outputTime, outputDuration));
      
      lastTrackID         = trackID;
      lastDistance        = distance;
      lastHeartRate       = heartRate;
      lastBreathingRate   = breathingRate;
      lastSkinTemperature = skinTemperature;
      lastTime            = time;
      lastDuration        = duration;
    }
    
    return results.toArray(EMPTY_RESULT);
  }
}
