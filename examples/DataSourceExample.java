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

import org.firebirdsql.jdbc.FBWrappingDataSource;
import org.firebirdsql.jca.FBConnectionRequestInfo;
import org.firebirdsql.gds.GDS;

public final class DataSourceExample
{
  static public void main (String args[]) throws Exception
  {
    // Create an Firebird data source manually;
    
	FBWrappingDataSource dataSource = new FBWrappingDataSource();

    // Set the standard properties
    dataSource.setDatabaseName ("localhost/3050:c:/database/employee.gdb");
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
	
	/*
	 * This is an example how to use FBConnectionRequestInfo to specify
	 * DPB that will be used for this data source.
	 */
	 
	FBConnectionRequestInfo cri = dataSource.getConnectionRequestInfo();
	cri.setProperty(GDS.isc_dpb_lc_ctype, "NONE");
    cri.setProperty(GDS.isc_dpb_sql_dialect, 1);
	
    /*
     * Note, next line works only with SuperServer architecture,
     * ClassicServer will hang if this line is uncommented.
     */
    
    //cri.setProperty(GDS.isc_dpb_num_buffers, 1);
	
	dataSource.setConnectionRequestInfo(cri);

    // Connect to the Firebird DataSource
    try {
      dataSource.setLoginTimeout (10);
      java.sql.Connection c = dataSource.getConnection ("sysdba", "masterkey");
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
			  
