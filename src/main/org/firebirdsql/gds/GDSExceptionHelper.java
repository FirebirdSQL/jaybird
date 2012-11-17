/*
 * Public Firebird Java API.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *    1. Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution. 
 *    3. The name of the author may not be used to endorse or promote products 
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED 
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO 
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

   private static final String SQLSTATE_CLI_GENERIC_ERROR = "HY000";

   private static final Logger log = LoggerFactory.getLogger(GDSExceptionHelper.class,false);

    private static final String MESSAGES = "isc_error_msg";
    private static final String SQLSTATES = "isc_error_sqlstates";
    private static final Properties messages;
    private static final Properties sqlstates;

    /**
     * Initializes the messages map.
     */
    static {
        try {
            messages = loadResource(MESSAGES);
            sqlstates = loadResource(SQLSTATES);
        } catch (Exception ex) {
            if (log != null) log.error("Exception in init of GDSExceptionHelper, unable to load error information", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Properties loadResource(String resource) throws Exception {
        Properties properties = new Properties();
        String res = "/" + resource.replace('.','/') + ".properties";
		InputStream in = GDSException.class.getResourceAsStream(res);
		if (in == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream(res);
        }
		try {
            if (in != null) {
                properties.load(in);
            } else if (log != null) {
                log.warn("Unable to load resource; resource " + resource + " is not found");
            }
		} finally {
		    if (in != null) {
		        in.close();
		    }
		}
        return properties;
    }

    /**
     * This method returns a message for the specified error code.
     * @param code Firebird error code
     * @return instance of <code>GDSExceptionHelper.GDSMesssage</code> class
     * where you can set desired parameters.
     */
    public static GDSMessage getMessage(int code) {
        return new GDSMessage(messages.getProperty(
                Integer.toString(code), "No message for code " + code + " found."));
    }
    
    /**
     * Get the SQL state for the specified error code.
     * 
     * @param code Firebird error code
     *  
     * @return string with SQL state, "HY000" if nothing found. 
     */
    public static String getSQLState(int code) {
        return sqlstates.getProperty(Integer.toString(code), SQLSTATE_CLI_GENERIC_ERROR);
    }

    /**
     * This class wraps message template obtained from isc_error_msg.properties
     * file and allows to set parameters to the message.
     */
    public static final class GDSMessage {
        private final String template;
        private final String[] params;

        /**
         * Constructs an instance of GDSMessage for the specified template.
         */
        public GDSMessage(String template) {
            this.template = template;
            params = new String[getParamCountInternal()];
        }

        /**
         * Returns the number of parameters for the message template.
         * @return number of parameters.
         */
        public int getParamCount() {
            return params.length;
        }

        private int getParamCountInternal() {
            int count = 0;
            for(int i = 0; i < template.length(); i++) {
                if (template.charAt(i) == '{') count++;
            }
            return count;
        }

        /**
         * Sets the parameter value
         * @param position the parameter number, 0 - first parameter.
         * @param text value of parameter
         */
        public void setParameter(int position, String text) {
            if (position < params.length) {
                params[position] = text;
            }
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
