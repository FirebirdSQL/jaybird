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
// essentially duplicating all of the driver manager's useful functionality.
// Although, both mechanisms may be used by the same application if desired,
// JavaSoft encourages developers to regard the DriverManager as a legacy
// feature of the JDBC API.
// Applications should use the DataSource API whenever possible.
// A JDBC implementation that is accessed via the DataSource API is not
// automatically registered with the DriverManager.
// The DriverManager, Driver, and DriverPropertyInfo interfaces
// may be deprecated in the future.

public final class DataSourceExample {
    static public void main(String args[]) throws Exception {
        // Create a Firebird data source manually;
        org.firebirdsql.ds.FBSimpleDataSource dataSource = new org.firebirdsql.ds.FBSimpleDataSource();

        // Set connect through the employee alias
        // The 'database' property is the JDBC url without the jdbc:firebirdsql: prefix
        dataSource.setDatabase("//localhost:3050/employee");

        // PURE_JAVE is the default, other options include NATIVE, EMBEDDED and LOCAL
        dataSource.setType("PURE_JAVA");

        dataSource.setUserName("sysdba");
        dataSource.setPassword("masterkey");

        // SQL Role can be set like this:
        //
        // dataSource.setRoleName("USER");

        // Default character encoding for the connection is set to NONE
        dataSource.setEncoding("UTF8");

        // Alternatively, you can use the Java encoding
        //dataSource.setCharSet("utf-8");

        // other non-standard properties do not have setters; you can pass any DPB parameter
        //
        // dataSource.setNonStandardProperty("isc_dpb_sweep", null);
        // dataSource.setNonStandardProperty("isc_dpb_num_buffers", "75");

        dataSource.setLoginTimeout(10);

        // Connect to the Firebird DataSource
        try (java.sql.Connection c = dataSource.getConnection()) {
            System.out.println("got connection");

            try (java.sql.Statement stmt = c.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery("select cust_no, customer from customer")) {
                while (rs.next()) {
                    System.out.println("cust_no = " + rs.getString(1) + ", customer = " + rs.getString(2));
                }
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }
}
			  
