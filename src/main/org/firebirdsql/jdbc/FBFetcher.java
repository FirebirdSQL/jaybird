package org.firebirdsql.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

interface FBFetcher {

     boolean next() throws SQLException;

     void close() throws SQLException;

	 static final int MAX_FETCH_ROWS = 400;
	 
     Statement getStatement();

	 public int getRowNum();
	 public boolean getIsEmpty();
	 public boolean getIsBeforeFirst();
	 public boolean getIsFirst();
	 public boolean getIsLast();
	 public boolean getIsAfterLast();
}