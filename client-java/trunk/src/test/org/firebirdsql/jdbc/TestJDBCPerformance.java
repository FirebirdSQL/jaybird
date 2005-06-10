package org.firebirdsql.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.textui.TestRunner;

import org.firebirdsql.common.FBTestBase;

public class TestJDBCPerformance extends FBTestBase {

    /* tpc bm b scaling rules */
    public static int tps = 1; /* the tps scaling factor: here it is 1 */

    public static int nbranches = 1; /* number of branches in 1 tps db */

    public static int ntellers = 10; /* number of tellers in 1 tps db */

    public static int naccounts = 100000; /*
                                             * number of accounts in 1 tps
                                             * db
                                             */

    public static int nhistory = 864000; /*
                                             * number of history recs in 1
                                             * tps db
                                             */

    public final static int TELLER = 0;

    public final static int BRANCH = 1;

    public final static int ACCOUNT = 2;

    int failed_transactions = 0;

    int transaction_count = 0;

    static int n_clients = 30;

    static int n_txn_per_client = 10;

    long start_time = 0;

    static boolean innodb = false;

    static boolean verbose = true;
    
    static boolean useSingleConnection = false;

    public TestJDBCPerformance(String name) {
        super(name);
    }

    private class JDBCBench {
        MemoryWatcherThread memoryWatcher;


        /*
         * main program, creates a 1-tps database: i.e. 1 branch, 10 tellers,...
         * runs one TPC BM B transaction
         */

        public void main(Connection connection, String[] Args) throws Exception {
            boolean initialize_dataset = false;

            for (int i = 0; i < Args.length; i++) {
                if (Args[i].equals("-clients")) {
                    if (i + 1 < Args.length) {
                        i++;
                        n_clients = Integer.parseInt(Args[i]);
                    }
                } else if (Args[i].equals("-tpc")) {
                    if (i + 1 < Args.length) {
                        i++;
                        n_txn_per_client = Integer.parseInt(Args[i]);
                    }
                } else if (Args[i].equals("-init")) {
                    initialize_dataset = true;
                } else if (Args[i].equals("-v")) {
                    verbose = true;
                } else if (Args[i].equals("-innodb")) {
                    innodb = true;
                }
            }

            System.out
                    .println("*********************************************************");
            System.out
                    .println("* JDBCBench v1.0                                        *");
            System.out
                    .println("*********************************************************");
            System.out.println();
            System.out.println();
            System.out.println("Number of clients: " + n_clients);
            System.out.println("Number of transactions per client: "
                    + n_txn_per_client);
            System.out.println();

            try {
                if (initialize_dataset) {
                    System.out.println("Start: "
                            + (new java.util.Date()).toString());
                    System.out.print("Initializing dataset...");
                    createDatabase(connection);
                    System.out.println("done.\n");
                    System.out.println("Complete: "
                            + (new java.util.Date()).toString());
                    return;
                }
                System.out.println("* Starting Benchmark Run *");
                memoryWatcher = new MemoryWatcherThread();
//                memoryWatcher.setDaemon(true);
                memoryWatcher.start();

                start_time = System.currentTimeMillis();

                Connection singleConnection = useSingleConnection ? getConnectionViaDriverManager() : null;
                
                ClientThread[] threads = new ClientThread[n_clients];
                for (int i = 0; i < n_clients; i++) {
                    threads[i] = new ClientThread(
                            useSingleConnection ? singleConnection
                                    : getConnectionViaDriverManager(),
                            n_txn_per_client, 1);
                    threads[i].start();
                }

                for (int i = 0; i < threads.length; i++) {
                    if (threads[i] != null)
                        threads[i].join();
                }
                
//                ClientThread testThread = new ClientThread(
//                        getConnectionViaDriverManager(), n_txn_per_client, 1000);
//                
//                testThread.run();
                
            } catch (Exception E) {
                System.out.println(E.getMessage());
                E.printStackTrace();
                
                throw E;
            }
        }

        public JDBCBench() {
        }

        public void reportDone() {
            n_clients--;

            if (n_clients <= 0) {
                if (memoryWatcher != null)
                    memoryWatcher.interrupt();

                long end_time = System.currentTimeMillis();
                double completion_time = ((double) end_time - (double) start_time) / 1000;
                System.out.println("* Benchmark finished *");
                System.out.println("\n* Benchmark Report *");
                System.out.println("--------------------\n");
                System.out.println("Time to execute " + transaction_count
                        + " transactions: " + completion_time + " seconds.");
                System.out.println("Max/Min memory usage: " + memoryWatcher.max
                        + " / " + memoryWatcher.min + " kb");
                System.out.println(failed_transactions + " / "
                        + transaction_count + " failed to complete.");
                System.out.println("Transaction rate: "
                        + (transaction_count - failed_transactions)
                        / completion_time + " txn/sec.");
            }

        }

        public synchronized void incrementTransactionCount() {
            transaction_count++;
        }

        public synchronized void incrementFailedTransactionCount() {
            failed_transactions++;
        }

        /*
         * createDatabase() - Creates and Initializes a scaled database.
         */

        void createDatabase(Connection connection) throws Exception {

            try {
                Statement Stmt = connection.createStatement();

                String s = connection.getMetaData().getDatabaseProductName();
                System.out.println("DBMS: " + s);

                String Query = "CREATE TABLE branches (";
                Query += "Bid         INT NOT NULL, PRIMARY KEY(Bid), ";
                Query += "Bbalance    INT,";
                Query += "filler      CHAR(88))"; /* pad to 100 bytes */
                if (innodb) Query += " TYPE = InnoDB";
                Stmt.execute(Query);
                Stmt.clearWarnings();

                Query = "CREATE TABLE tellers ( ";
                Query += "Tid         INT NOT NULL, PRIMARY KEY(Tid),";
                Query += "Bid         INT,";
                Query += "Tbalance    INT,";
                Query += "filler      CHAR(84))"; /* pad to 100 bytes */
                if (innodb) Query += " TYPE = InnoDB";

                Stmt.execute(Query);
                Stmt.clearWarnings();

                Query = "CREATE TABLE accounts ( ";
                Query += "Aid         INT NOT NULL, PRIMARY KEY(Aid), ";
                Query += "Bid         INT, ";
                Query += "Abalance    INT, ";
                Query += "filler      CHAR(84))"; /* pad to 100 bytes */
                if (innodb) Query += " TYPE = InnoDB";

                Stmt.execute(Query);
                Stmt.clearWarnings();

                Query = "CREATE TABLE history ( ";
                Query += "Tid         INT, ";
                Query += "Bid         INT, ";
                Query += "Aid         INT, ";
                Query += "delta       INT, ";
                Query += "time1        TIMESTAMP, ";
                Query += "filler      CHAR(22))"; /* pad to 50 bytes */
                if (innodb) Query += " TYPE = InnoDB";

                Stmt.execute(Query);
                Stmt.clearWarnings();

                /*
                 * prime database using TPC BM B scaling rules. Note that for
                 * each branch and teller: branch_id = teller_id / ntellers
                 * branch_id = account_id / naccounts
                 */

                for (int i = 0; i < nbranches * tps; i++) {
                    Query = "INSERT INTO branches(Bid,Bbalance) VALUES (" + i
                            + ",0)";
                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();
                }
                for (int i = 0; i < ntellers * tps; i++) {
                    Query = "INSERT INTO tellers(Tid,Bid,Tbalance) VALUES ("
                            + i + "," + i / ntellers + ",0)";
                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();
                }
                for (int i = 0; i < naccounts * tps; i++) {
                    Query = "INSERT INTO accounts(Aid,Bid,Abalance) VALUES ("
                            + i + "," + i / naccounts + ",0)";
                    Stmt.executeUpdate(Query);
                    Stmt.clearWarnings();
                }
            } catch (Exception E) {
                System.out.println(E.getMessage());
                E.printStackTrace();
            }

        } /* end of CreateDatabase */

        public int getRandomInt(int lo, int hi) {
            int ret = 0;

            ret = (int) (Math.random() * (hi - lo + 1));
            ret += lo;

            return ret;
        }

        public int getRandomID(int type) {
            int min, max, num;

            max = min = 0;
            num = naccounts;

            switch (type) {
                case TELLER:
                    min += nbranches;
                    num = ntellers;
                /* FALLTHROUGH */
                case BRANCH:
                    if (type == BRANCH) num = nbranches;
                    min += naccounts;
                /* FALLTHROUGH */
                case ACCOUNT:
                    max = min + num - 1;
            }
            return (getRandomInt(min, max));
        }

        class ClientThread extends Thread {

            int ntrans = 0;
            int loopCount;
            Connection connection;

            public ClientThread(Connection connection, int number_of_txns, int loopCount) throws SQLException {
                ntrans = number_of_txns;
                this.loopCount = loopCount;
                this.connection = connection;
                if (!useSingleConnection)
                    connection.setAutoCommit(false);
            }

            public void run() {
                try {
                    while (ntrans-- > 0) {
    
                        int account = JDBCBench.this.getRandomID(ACCOUNT);
                        int branch = JDBCBench.this.getRandomID(BRANCH);
                        int teller = JDBCBench.this.getRandomID(TELLER);
                        int delta = JDBCBench.this.getRandomInt(0, 1000);
    
                        doOne(connection, account, branch, teller, delta, loopCount);
                        incrementTransactionCount();
                    }
                    reportDone();
                } finally {
                    try {
                        if (!useSingleConnection)
                            connection.close();
                    } catch(SQLException ex) {
                        // empty
                    }
                }
            }

            /*
             * doOne() - Executes a single TPC BM B transaction.
             */

            int doOne(Connection connection, int bid, int tid, int aid, int delta, int loopCount) {
                
                try {
                    Statement stmt = connection.createStatement(
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);

                    int aBalance = 0;
                    
                            
                        String query = "UPDATE accounts ";
                        query += "SET     Abalance = Abalance + " + delta + " ";
                        query += "WHERE   Aid = " + aid;
    
                        stmt.executeUpdate(query);
                        stmt.clearWarnings();
    
                        query = "SELECT Abalance ";
                        query += "FROM   accounts ";
                        query += "WHERE  Aid = " + aid;
    
                        synchronized(connection) {
                        PreparedStatement ps = connection.prepareStatement(query);
                        try {
                        for(int i = 0; i < loopCount; i++) {
                            ResultSet rs = ps.executeQuery();
        
                            while (rs.next()) {
                                aBalance = rs.getInt(1);
                            }
                        }
                        } finally {
                            ps.close();
                        }
                        }
    
                        query = "UPDATE tellers ";
                        query += "SET    Tbalance = Tbalance + " + delta + " ";
                        query += "WHERE  Tid = " + tid;
    
                        stmt.executeUpdate(query);
                        stmt.clearWarnings();
    
                        query = "UPDATE branches ";
                        query += "SET    Bbalance = Bbalance + " + delta + " ";
                        query += "WHERE  Bid = " + bid;
    
                        stmt.executeUpdate(query);
                        stmt.clearWarnings();
    
                        query = "INSERT INTO history(Tid, Bid, Aid, delta) ";
                        query += "VALUES (";
                        query += tid + ",";
                        query += bid + ",";
                        query += aid + ",";
                        query += delta + ")";
    
                        stmt.executeUpdate(query);
                        stmt.clearWarnings();

                    if (!useSingleConnection)
                        connection.commit();
                    
                    return aBalance;
                } catch (SQLException E) {
                    if (verbose) {
                        System.out.println("Transaction failed: "
                                + E.getMessage());
                        E.printStackTrace();
                    }
                    incrementFailedTransactionCount();
                }
                
                return 0;

            } /* end of DoOne */

        }

        class MemoryWatcherThread extends Thread {

            long min = 0;

            long max = 0;

            public void run() {
                min = Runtime.getRuntime().freeMemory();

                for (;;) {
                    long currentFree = Runtime.getRuntime().freeMemory();
                    long currentAlloc = Runtime.getRuntime().totalMemory();
                    long used = currentAlloc - currentFree;

                    if (used < min) min = used;
                    if (used > max) max = used;

                    try {
                        sleep(100);
                    } catch (InterruptedException E) {
                    }
                }
            }
        }

    }

    public void testPerformance() throws Exception {
        new JDBCBench().main(getConnectionViaDriverManager(), new String[0]);
    }
    
    public void testDummy() {
        // empty
    }
    
    public static void main(String[] args) {
        TestRunner.run(TestJDBCPerformance.class);
    }

}
