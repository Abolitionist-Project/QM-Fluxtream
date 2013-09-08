package com.quantimodo.etl;

import com.fluxtream.dto.VariableDto;
import com.fluxtream.domain.Guest;
import com.fluxtream.auth.AuthHelper;
import com.fluxtream.services.GuestService;
import com.fluxtream.services.VariablesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class QuantimodoSpring {
  @Autowired
  VariablesService variablesService;
  
  @Autowired
  GuestService guestService;
  
  final Logger logger = Logger.getLogger(QuantimodoSpring.class);
  private static final QuantimodoRecord[] EMPTY_READ = new QuantimodoRecord[0];
  
  public synchronized void write(final long userID, final QuantimodoRecord[] records) {
    final Guest guest = guestService.getGuestById(AuthHelper.getGuestId());

    // Separate records by variable
    final Map<VariableInfo, List<QuantimodoRecord>> recordsByVariable = new HashMap<VariableInfo, List<QuantimodoRecord>>();
    for (final QuantimodoRecord record : records) {
      final VariableInfo variable = new VariableInfo(record.getApplicationName(), record.getVariableName());
      final List<QuantimodoRecord> recordsForThisVariable;
      if (!recordsByVariable.containsKey(variable)) {
        recordsForThisVariable = new ArrayList<QuantimodoRecord>();
        recordsByVariable.put(variable, recordsForThisVariable);
      }
      else {
        recordsForThisVariable = recordsByVariable.get(variable);
      }
      recordsForThisVariable.add(record);
    }
    
    // Handle each variable's records
    for (final VariableInfo variable : recordsByVariable.keySet()) {
      final List<QuantimodoRecord> recordsForThisVariable = recordsByVariable.get(variable);
      final VariableDto variableDto; {
        final QuantimodoRecord firstRecord = recordsForThisVariable.get(0);
        
        VariableDto tmpVariableDto = variablesService.getVariable(firstRecord.isUserDefinedVariable() ? guest : null, firstRecord.getApplicationName(), firstRecord.getVariableName());
        if (tmpVariableDto == null) {
          
        }
        variableDto = tmpVariableDto;
      }
      
      
      
      for (final QuantimodoRecord record : recordsForThisVariable) {
        
      }
    }
  }

  private static final class VariableInfo implements Comparable<VariableInfo> {
    public final String applicationName;
    public final String variableName;
    
    public VariableInfo(final String applicationName, final String variableName) {
      this.applicationName = applicationName;
      this.variableName = variableName;
    }
    
    public int compareTo(final VariableInfo o) {
      final int firstComparison = this.applicationName.compareTo(o.applicationName);
      return firstComparison == 0 ? this.variableName.compareTo(o.variableName) : firstComparison;
    }
    
    public boolean equals(final VariableInfo o) {
      return this.applicationName.equals(o.applicationName) && this.variableName.equals(o.variableName);
    }
    
    public int hashCode() {
      return applicationName.hashCode() % (19*variableName.hashCode());
    }
  }
}
