package com.quantimodo.etl;

import com.mysql.jdbc.Driver;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.sql.Timestamp;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.sql.SQLException;

public class QuantimodoDatabase {
  public static final QuantimodoDatabase instance;
  
  static {
    try { new Driver(); } catch (final SQLException e) {}
    QuantimodoDatabase db = null;
    try {
      db = new QuantimodoDatabase("jdbc:mysql://127.0.0.1:3306/", "quantimodo", "user", "quantimodo", "password", "PDNZCF7bv7CDX5D6", "rewriteBatchedStatements", "true");
    } catch (final SQLException e) {
      System.err.println("ERROR: COULD NOT CONNECT TO DATABASE!");
      e.printStackTrace();
    }
    instance = db;
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  }
  
  private static final class ShutdownHook extends Thread {
    public ShutdownHook() { super("QuantimodoDatabase shutdown hook"); }
    public void run() { QuantimodoDatabase.instance.close(); }
  }
  
  private final Connection        connection;
  private final CallableStatement writeStarter;
  private final CallableStatement writeInserter;
  private final CallableStatement writeAborter;
  private final CallableStatement writeFinisher;
  private final PreparedStatement reader;
  private final PreparedStatement deleter;
  
  private static final QuantimodoRecord[] EMPTY_READ = new QuantimodoRecord[0];
  
  // Disable default constructor.
  private QuantimodoDatabase() { throw new UnsupportedOperationException("The default constructor is not valid."); }
  
  private QuantimodoDatabase(final CharSequence url, final CharSequence databaseName, final CharSequence... parameters) throws SQLException {
    Connection        conn   = null;
    CallableStatement start  = null;
    CallableStatement insert = null;
    CallableStatement abort  = null;
    CallableStatement finish = null;    
    PreparedStatement read   = null;
    PreparedStatement delete = null;
    
    try {
      {
        if ((parameters.length & 1) != 0) throw new IllegalArgumentException("Unmatched parameter");
        String params = "";
        for (int i = 0; i < parameters.length; i += 2) {
          params += String.format("%c%s=%s", ((i == 0) ? '?' : '&'), parameters[i], parameters[i + 1]);
        }
        conn = DriverManager.getConnection(url.toString() + databaseName.toString() + params);
      }
      // Use transactions.
      conn.setAutoCommit(true);
      start  = conn.prepareCall("{ CALL qm_insert_initialize() }");
      insert = conn.prepareCall("{ CALL qm_insert(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) }");
      abort  = conn.prepareCall("{ CALL qm_insert_abort() }");
      finish = conn.prepareCall("{ CALL qm_insert_finalize() }");
      read   = conn.prepareStatement(
          "SELECT a.name, c.name, v.name, v.data_owner, v.input_variable, v.summable_variable, d.value, u.name, d.start_time_utc, d.duration_in_seconds FROM " +
            "qm_qs_data AS d " +
            "INNER JOIN qm_variables AS v ON d.variable = v.id " +
            "INNER JOIN qm_variable_categories AS c ON v.variable_category = c.id " +
            "INNER JOIN qm_applications AS a ON v.source_application = a.id " +
            "INNER JOIN qm_units AS u ON v.unit_used = u.id " +
          "WHERE d.data_owner = ? " +
          "ORDER BY d.start_time_utc ASC, d.duration_in_seconds ASC;"
      );
      delete = conn.prepareStatement(
          "DELETE FROM qm_qs_data WHERE data_owner = ?;"
      );
    } catch (final SQLException e) {
      if (delete != null) { try { delete.close(); } catch (final SQLException e1) {} }
      if (read   != null) { try { read.close();   } catch (final SQLException e1) {} }
      if (finish != null) { try { finish.close(); } catch (final SQLException e1) {} }
      if (abort  != null) { try { abort.close();  } catch (final SQLException e1) {} }
      if (insert != null) { try { insert.close(); } catch (final SQLException e1) {} }
      if (start  != null) { try { start.close();  } catch (final SQLException e1) {} }
      if (conn   != null) { try { conn.close();   } catch (final SQLException e1) {} }
      throw e;
    }
    connection    = conn;
    writeStarter  = start;
    writeInserter = insert;
    writeAborter  = abort;
    writeFinisher = finish;
    reader        = read;
    deleter       = delete;
  }
  
  public QuantimodoRecord[] read(final long userID) throws SQLException {
    reader.setLong(1, userID);
    ResultSet resultSet = reader.executeQuery();
    
    List<QuantimodoRecord> results = new ArrayList<QuantimodoRecord>();
    while (resultSet.next()) {
      final int isInput = resultSet.getByte(5);
      results.add(new QuantimodoRecord(resultSet.getString(1), resultSet.getString(2), resultSet.getString(3),
                  resultSet.getBoolean(4), (isInput == 2) ? null : ((isInput == 1) ? true : false), resultSet.getBoolean(6),
                  resultSet.getDouble(7), resultSet.getString(8), resultSet.getTimestamp(9).getTime(), resultSet.getInt(10)));
    }
    
    return (results.size() == 0) ? EMPTY_READ : results.toArray(EMPTY_READ);
  }
  
  public void write(final long userID, final QuantimodoRecord[] records) throws SQLException {
    write(userID, records, false);
  }
  
  public synchronized void write(final long userID, final QuantimodoRecord[] records, final boolean debugPrinting) throws SQLException {
    writeAborter.executeUpdate();
    writeStarter.executeUpdate();
    
    try {
      writeInserter.setLong(1, userID);
      for (int recordNumber = 0; recordNumber < records.length; recordNumber++) {
        final QuantimodoRecord record = records[recordNumber];
        
        writeInserter.setString (2,  record.getApplicationName());
        writeInserter.setString (3,  record.getVariableCategory());
        writeInserter.setString (4,  record.getVariableName());
        writeInserter.setBoolean(5,  record.isUserDefinedVariable());
        {
          final Boolean isInput = record.isInputVariable();
          writeInserter.setByte (6,  (byte) ((isInput == null) ? 2 : (isInput.booleanValue() ? 1 : 0)));
        }
        writeInserter.setBoolean(7,  record.isSummableVariable());
        writeInserter.setDouble (8,  record.getValue());
        writeInserter.setString (9,  record.getUnitName());
        writeInserter.setLong   (10, record.getStartTime());
        writeInserter.setInt    (11, record.getDuration());
        
        if (debugPrinting) System.out.printf("Batching %s.\n\n", writeInserter);
        writeInserter.addBatch();
        if (recordNumber % 1000 == 999) {
          if (debugPrinting) System.out.println("Executing batch.");
          writeInserter.executeBatch();
        }
      }
      
      if (debugPrinting) System.out.print("Executing batch.");
      writeInserter.executeBatch();
      
      if (debugPrinting) System.out.println("Finalizing write on database.");
      writeFinisher.executeUpdate();
    } catch (final SQLException e) {
      try { writeAborter.executeUpdate(); } catch (final SQLException e1) {}
      // Transactions are on server's stored procedures now
      // try { connection.rollback();        } catch (final SQLException e1) { System.err.println("After error, could not roll back!"); e1.printStackTrace(); }
      throw e;
    }
  }
  
  // Returns number of rows deleted.
  public int deleteAll(final long userID) throws SQLException {
    try {
      deleter.setLong(1, userID);
      final int result = deleter.executeUpdate();
      return result;
    } catch (final SQLException e) {
      try { connection.rollback(); } catch (final SQLException e1) { System.err.println("After error, could not roll back!"); e1.printStackTrace(); }
      throw e;
    }
  }
  
  // Closes database connection for the rest of the time the program runs.
  // Do not use until you are completely done with QuantimodoDatabase.
  public void close() {
    if (writeStarter  != null) try { writeStarter.close();  } catch (final SQLException e) {}
    if (writeInserter != null) try { writeInserter.close(); } catch (final SQLException e) {}
    if (writeAborter  != null) {
      try { writeAborter.executeUpdate(); } catch (final SQLException e) {}
      try { writeAborter.close();         } catch (final SQLException e) {}
    }
    if (writeFinisher != null) try { writeFinisher.close(); } catch (final SQLException e) {}
    if (reader        != null) try { reader.close();        } catch (final SQLException e) {}
    if (deleter       != null) try { deleter.close();       } catch (final SQLException e) {}
    if (connection    != null) {
      try { connection.rollback(); } catch (final SQLException e) {}
      try { connection.close();    } catch (final SQLException e) {}
    }
  }
}
