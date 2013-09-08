package com.quantimodo.etl;

import java.sql.Timestamp;

import java.io.IOException;

public class QuantimodoRecord {
  private final String  applicationName;
  private final String  variableCategory;
  private final String  variableName;
  private final boolean userDefinedVariable;
  private final Boolean inputVariable;
  private final boolean summableVariable;
  private final double  value;
  private final String  unitName;
  private final long    startTime;
  private final int     duration;
  
  // Disable default constructor.
  private QuantimodoRecord() { throw new UnsupportedOperationException("The default constructor is not valid."); }
  
  public QuantimodoRecord(final CharSequence applicationName, final CharSequence variableCategory, final CharSequence variableName,
                          final boolean userDefinedVariable, final Boolean inputVariable, final boolean summableVariable,
                          final double value, final CharSequence unitName, final long startTime, final int duration) {
    this.applicationName     = applicationName.toString();
    this.variableCategory    = variableCategory.toString();
    this.variableName        = variableName.toString();
    this.userDefinedVariable = userDefinedVariable;
    this.inputVariable       = ((inputVariable == null) ? null : (inputVariable.booleanValue() ? Boolean.TRUE : Boolean.FALSE));
    this.summableVariable    = summableVariable;
    this.value               = value;
    this.unitName            = unitName.toString();
    this.startTime           = startTime;
    this.duration            = duration;
  }
  
  public String  getApplicationName()    { return applicationName;     }
  public String  getVariableCategory()   { return variableCategory;    }
  public String  getVariableName()       { return variableName;        }
  public boolean isUserDefinedVariable() { return userDefinedVariable; }
  public Boolean isInputVariable()       { return inputVariable;       }
  public boolean isSummableVariable()    { return summableVariable;    }
  public double  getValue()              { return value;               }
  public String  getUnitName()           { return unitName;            }
  public long    getStartTime()          { return startTime;           }
  public int     getDuration()           { return duration;            }
  
  public String toString() {
    return appendInfo(new StringBuilder(), this).toString();
  }
  
  public static final StringBuilder appendInfo(final StringBuilder stringBuilder, final QuantimodoRecord record) {
    try { appendInfo((Appendable) stringBuilder, record); } catch (final IOException e) {}
    return stringBuilder;
  }
  
  public static final StringBuffer appendInfo(final StringBuffer stringBuffer, final QuantimodoRecord record) {
    try { appendInfo((Appendable) stringBuffer, record); } catch (final IOException e) {}
    return stringBuffer;
  }
  
  public static final Appendable appendInfo(final Appendable appendable, final QuantimodoRecord record) throws IOException {
    return appendable.append('[').append((new Timestamp(record.startTime)).toString()).append(", ").append(Integer.toString(record.duration)).append("s] ").
        append(record.applicationName).append(" recorded ").append(record.variableName).append(" of ").
        append(Double.toString(record.value)).append(' ').append(record.unitName).
        append(" (").append(record.summableVariable ? "summable" : "nonsummable").append(' ').
        append((record.inputVariable == null) ? "input/output" : (record.inputVariable ? "input" : "output")).
        append(" variable)");
  }
  
  public static final StringBuilder appendInfoHTML(final StringBuilder stringBuilder, final QuantimodoRecord[] records) {
    try { appendInfoHTML((Appendable) stringBuilder, records); } catch (final IOException e) {}
    return stringBuilder;
  }
  
  public static final StringBuffer appendInfoHTML(final StringBuffer stringBuffer, final QuantimodoRecord[] records) {
    try { appendInfoHTML((Appendable) stringBuffer, records); } catch (final IOException e) {}
    return stringBuffer;
  }
  
  public static final Appendable appendInfoHTML(final Appendable appendable, final QuantimodoRecord[] records) throws IOException {
    appendable.append("<table border=\"1\"><thead><tr>" +
        "<th></th><th>Start Time</th><th>Duration</th><th>Application</th><th>Variable</th>" +
        "<th>Value</th><th>Unit Name</th><th>Input/Output Variable</th><th>Combination Method</th></tr></thead><tbody>");
    for (int recordNumber = 0; recordNumber < records.length; recordNumber++) {
      final QuantimodoRecord record = records[recordNumber];
      
      appendable.append("<tr><th>Record ").append(Integer.toString(recordNumber)).append("</th><td>").
          append(HTMLUtil.escapeHTML((new Timestamp(record.startTime)).toString())).append("</td><td>" ).
          append(Integer.toString   (record.duration                             )).append("s</td><td>").
          append(HTMLUtil.escapeHTML(record.applicationName                      )).append("</td><td>" ).
          append(HTMLUtil.escapeHTML(record.variableName                         )).append("</td><td>" ).
          append(Double.toString    (record.value                                )).append("</td><td>" ).
          append(HTMLUtil.escapeHTML(record.unitName                             )).append("</td><td>" ).
          append((record.inputVariable == null) ? "both" : (record.inputVariable.booleanValue() ? "input" : "output")).append("</td><td>").
          append(record.summableVariable ? "sum" : "mean"                         ).append("</td></tr>");
    }
    return appendable.append("</tbody></table>");
  }
}
