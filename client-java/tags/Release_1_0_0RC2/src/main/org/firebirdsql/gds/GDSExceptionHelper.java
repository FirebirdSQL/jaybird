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

/*
 * CVS modification log:
 * $Log$
 * Revision 1.9  2002/06/10 18:47:40  brodsom
 * logging change, logging depends on the first class used, default to true for FBManagedConnectionFactory, FBManager and tests and false for other classes.
 *
 * Revision 1.8  2002/06/06 11:24:07  brodsom
 * Performance patch. Log if log4j is in the classpath, don't log if the enviroment variable FBLog4j is false.
 *
 * Revision 1.7  2002/06/04 01:17:49  brodsom
 * performance patches
 *
 * Revision 1.6  2002/02/02 18:58:24  d_jencks
 * converted to log4j logging and cleaned up some test problems.  If you do not wish to use log4j, you may leave out the log4j-core.jar and get no logging
 *
 * Revision 1.5  2002/02/01 03:58:06  d_jencks
 * applied fix from William Suroweic in case we messed up the params in error messages
 *
 * Revision 1.4  2002/01/07 06:59:54  d_jencks
 * Revised FBManager to create dialect 3 databases, and the tests to use a newly created database. Simplified and unified test constants. Test targets are now all-tests for all tests and one-test for one test: specify the test as -Dtest=Gds one-test for the TestGds.class test.  Made a few other small changes to improve error messages
 *
 * Revision 1.3  2001/10/24 16:59:43  alberola
 * Fixed bug in ClassLoader
 *
 * Revision 1.2  2001/07/18 20:07:31  d_jencks
 * Added better GDSExceptions, new NativeSQL, and CallableStatement test from Roman Rokytskyy
 *
 * Revision 1.1  2001/07/16 03:57:43  d_jencks
 * added text error messages to GDSExceptions, thanks Roman Rokytskyy
 *
 */
package org.firebirdsql.gds;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

/**
 * This class is supposed to return messages for the specified error code.
 * It loads all messages during the class initialization and keeps messages
 * in the static <code>java.util.Properties</code> variable.
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public class GDSExceptionHelper {

   private static final Logger log = LoggerFactory.getLogger(GDSExceptionHelper.class,false);

    private static final String MESSAGES = "isc_error_msg";
    private static java.util.Properties messages = new java.util.Properties();

    private static boolean initialized = false;

    /**
     * This method initializes the messages map.
     * @todo think about better exception handling.
     */
    private static void init() {
        try {
            ClassLoader cl = GDSException.class.getClassLoader();
            String res = MESSAGES.replace('.','/') + ".properties";
            java.io.InputStream in = cl.getResourceAsStream(res);
            messages.load(in);
        } catch (Exception ex) {
            if (log!=null) log.info("Exception in init of GDSExceptionHelper", ex);
        } finally {
            initialized = true;
        }
    }

    /**
     * This method returns a message for the specified error code.
     * @param code Firebird error code
     * @return instance of <code>GDSExceptionHelper.GDSMesssage</code> class
     * where you can set desired parameters.
     */
    public static GDSMessage getMessage(int code) {
        if (!initialized) init();
        return new GDSMessage(messages.getProperty(
            "" + code, "No message for code " + code + " found."));
    }

    /**
     * This class wraps message template obtained from isc_error_msg.properties
     * file and allows to set parameters to the message.
     */
    public static class GDSMessage {
        private String template;
        private String[] params;

        /**
         * Constructs an instance of GDSMessage for the specified template.
         */
        public GDSMessage(String template) {
            this.template = template;
            params = new String[getParamCount()];
        }

        /**
         * Returns the number of parameters for the message template.
         * @return number of parameters.
         */
        public int getParamCount() {
            int count = 0;
            for(int i = 0; i < template.length(); i++)
                if (template.charAt(i) == '{') count++;
            return count;
        }

        /**
         * Sets the parameter value
         * @param position the parameter number, 0 - first parameter.
         * @param text value of parameter
         */
        public void setParameter(int position, String text) {
            if (position < params.length)
                params[position] = text;
        }

        /**
         * Puts parameters into the template and return the obtained string.
         * @return string representation of the message.
         */
        public String toString() {
            String message = template;
            for(int i = 0; i < params.length; i++) {
                String param = "{" + i + "}";
                int pos = message.indexOf(param);
                if (pos > -1) 
                {
                   String temp = message.substring(0, pos);
                   temp += (params[i] == null) ? "" : params[i];
                   temp += message.substring(pos + param.length());
                   message = temp;
                } // end of if ()
            }
            return message;
        }
    }

}
