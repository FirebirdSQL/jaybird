 /*
 * Firebird Open Source J2ee connector - jdbc driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a CVS history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jdbc;

import java.util.Properties;

import junit.framework.TestCase;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;
import org.firebirdsql.management.FBManager;


/**
 * Class containing test-related constants. It should be changed depending
 * on the particular environment.
 *
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 */
public class BaseFBTest extends TestCase
{


    /**
     * Default URL for the test
     */
   private static final String DB_PATH = System.getProperty("test.db.dir", "");
   private static final String DB_LC_CTYPE = System.getProperty("test.db.lc_ctype", "NONE");
   public static final String DB_SERVER_URL = System.getProperty("test.db.host", "localhost");
   public static final int DB_SERVER_PORT = Integer.parseInt(System.getProperty("test.db.port", "3050"));

   public static final String DB_NAME = "fbtest.gdb";

   public static final String getdbpath(String name)
   {
      return DB_SERVER_URL + "/" + DB_SERVER_PORT + ":" + DB_PATH + "/" + name;
   }

   public static final String DB_DATASOURCE_URL = getdbpath(DB_NAME);
   public static final String DB_DRIVER_URL = FBDriver.FIREBIRD_PROTOCOL + DB_DATASOURCE_URL;



    /**
     * Default user name for database connection
     */
    public static final String DB_USER = "sysdba";

    /**
     * Password for the default user for database connection
     */
    public static final String DB_PASSWORD = "masterkey";
    
//    public static final String DB_LC_CTYPE = "NONE";

    /**
     * Default properties for database connection
     */
    public static final Properties DB_INFO =
        new Properties();

    // set up info properties
    static{
        DB_INFO.setProperty(FBDriver.USER, DB_USER);
        DB_INFO.setProperty(FBDriver.PASSWORD, DB_PASSWORD);
        DB_INFO.setProperty("lc_ctype", DB_LC_CTYPE);
    }

   private final FBManager fbManager = new FBManager();

   protected final Logger log = LoggerFactory.getLogger(getClass(),true);

    public BaseFBTest(String testName) {
        super(testName);
    }

   protected void setUp() throws Exception
   {
      try 
      {
         fbManager.setServer(DB_SERVER_URL);
         fbManager.setPort(DB_SERVER_PORT);
         fbManager.start();
         fbManager.createDatabase(DB_PATH + "/" + DB_NAME, DB_USER, DB_PASSWORD);
      }
      catch (Exception e)
      {
         if (log!=null) log.warn("exception in setup of " + getName() + ": ", e);
      } // end of try-catch
   }

   protected void tearDown() throws Exception
   {
      try 
      {
         fbManager.dropDatabase(DB_DATASOURCE_URL, DB_USER, DB_PASSWORD);
         fbManager.stop();
      }
      catch (Exception e)
      {
         if (log!=null) log.warn("exception in teardown of " + getName() + ": ", e);
      } // end of try-catch
      
   }
}
