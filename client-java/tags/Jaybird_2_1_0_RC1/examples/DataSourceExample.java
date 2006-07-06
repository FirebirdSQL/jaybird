// Original version of this file was part of InterClient 2.01 examples
//
// Copyright InterBase Software Corporation, 1998.
// Written by com.inprise.interbase.interclient.r&d.PaulOstler :-)
//
// Code was modified by Roman Rokytskyy to show that Firebird JCA-JDBC driver
// does not introduce additional complexity in normal driver usage scenario.
//
// An example of using a JDBC 2 Standard Extension DataSource.
// The DataSource facility provides an alternative to the JDBC DriverManager,
// essentially duplicating all of the driver manager’s useful functionality.
// Although, both mechanisms may be used by the same application if desired,
// JavaSoft encourages developers to regard the DriverManager as a legacy
// feature of the JDBC API.
// Applications should use the DataSource API whenever possible.
// A JDBC implementation that is accessed via the DataSource API is not
// automatically registered with the DriverManager.
// The DriverManager, Driver, and DriverPropertyInfo interfaces
// may be deprecated in the future.

public final class DataSourceExample
{
  static public void main (String args[]) throws Exception
  {
    // Create an Firebird data source manually;
    
	org.firebirdsql.pool.FBWrappingDataSource dataSource = 
        new org.firebirdsql.pool.FBWrappingDataSource();

    // Set the standard properties
    dataSource.setDatabase ("localhost/3050:c:/database/test_charset.fdb");
    dataSource.setDescription ("An example database of employees");

	/*
	 * Following properties were not deleted in order to show differences 
	 * between InterClient 2.01 data source implementation and Firebird one.
	 */
	
    //dataSource.setDataSourceName ("Employee");
    //dataSource.setPortNumber (3060);
    //dataSource.setNetworkProtocol ("jdbc:interbase:");
    //dataSource.setRoleName (null);
    
    // Set the non-standard properties
    //dataSource.setCharSet (interbase.interclient.CharacterEncodings.NONE);
    //dataSource.setSuggestedCachePages (0);
    //dataSource.setSweepOnConnect (false);
	
    // this some kind of equivalent to dataSource.setNetworkProtocol(String)
    // possible values are "type4", "type2" and "embedded".
	dataSource.setType("TYPE4");
    
    // SQL Role can be set like this:
    // 
    // dataSource.setRoleName("USER");
    
    // Character encoding for the connection is set to NONE
    dataSource.setEncoding("ISO8859_1");
    
    // other non-standard properties do not have setters
    // you can pass any DPB parameter
    //
    // dataSource.setNonStandardProperty("isc_dpb_sweep", null);
    // dataSource.setNonStandardProperty("isc_dpb_num_buffers", "75");
	
    // Connect to the Firebird DataSource
    try {
      dataSource.setLoginTimeout (10);
      java.sql.Connection c = dataSource.getConnection ("sysdba", "masterkey");
      
      java.sql.Statement stmt = c.createStatement();
      java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM test_charset");
      while(rs.next())
          System.out.println("a1 = " + rs.getString(1) + ", a2 = " + rs.getString(2));
          
      stmt.close();
      
      // At this point, there is no implicit driver instance
      // registered with the driver manager!
      System.out.println ("got connection");
      c.close ();
    }
    catch (java.sql.SQLException e) {
		e.printStackTrace();
      System.out.println ("sql exception: " + e.getMessage ());
    }
  }
}
			  
