package org.firebirdsql.jdbc;

interface FBFetcher {

    boolean next() throws java.sql.SQLException;

    void close() throws java.sql.SQLException;

	 static final int MAX_FETCH_ROWS = 400;
	 
    java.sql.Statement getStatement();

	 public int getRowNum();
	 public boolean getIsEmpty();
	 public boolean getIsBeforeFirst();
	 public boolean getIsFirst();
	 public boolean getIsLast();
	 public boolean getIsAfterLast();
}