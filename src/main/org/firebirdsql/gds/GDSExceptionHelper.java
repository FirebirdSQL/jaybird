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

package org.firebirdsql.gds;

import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.Properties;
import java.io.InputStream;

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
    private static Properties messages = new Properties();

    private static boolean initialized = false;

    /**
     * This method initializes the messages map.
     * @todo think about better exception handling.
     */
    private static void init() {
        try {
            String res = "/" + MESSAGES.replace('.','/') + ".properties";
			InputStream in = GDSException.class.getResourceAsStream(res);
            
            if (in == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                in = cl.getResourceAsStream(res);
            }
            
            if (in != null)
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
