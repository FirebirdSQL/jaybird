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

import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * This class returns messages for the specified error code.
 * <p>
 * It loads all messages during the class initialization and keeps messages
 * in the static <code>java.util.Properties</code> variable.
 * </p>
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:rrokytskyy@users.sourceforge.net">Roman Rokytskyy</a>
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @version 1.0
 */
public class GDSExceptionHelper {

    private static final Logger log = LoggerFactory.getLogger(GDSExceptionHelper.class);

    private static final String MESSAGES = "isc_error_msg";
    private static final String JAYBIRD_MESSAGES = "org/firebirdsql/jaybird_error_msg";
    private static final String SQLSTATES = "isc_error_sqlstates";
    private static final String JAYBIRD_SQLSTATES = "org/firebirdsql/jaybird_error_sqlstates";
    private static final Map<Integer, String> messages;
    private static final Map<Integer, String> sqlstates;

    /**
     * Initializes the messages map.
     */
    static {
        try {
            messages = loadResource(false, MESSAGES, JAYBIRD_MESSAGES);
            sqlstates = loadResource(true, SQLSTATES, JAYBIRD_SQLSTATES);
        } catch (Exception ex) {
            log.error("Exception in init of GDSExceptionHelper, unable to load error information", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static Map<Integer, String> loadResource(boolean deduplicate, String... resources) throws Exception {
        Properties properties = new Properties();
        Exception firstException = null;
        // Load from property files
        for (String resource : resources) {
            String resourceFile = "/" + resource.replace('.', '/') + ".properties";
            try (InputStream in = getResourceAsStream(resourceFile)) {
                if (in != null) {
                    properties.load(in);
                } else {
                    log.warn("Unable to load resource; resource " + resource + " is not found");
                }
            } catch (IOException ioex) {
                log.error("Unable to load resource " + resource, ioex);
                if (firstException == null) {
                    firstException = ioex;
                }
            }
        }

        if (firstException != null) {
            throw firstException;
        }

        // Use of HashMap avoids unnecessary synchronization in Properties
        return asErrorCodeMapping(deduplicate, properties);
    }

    private static Map<Integer, String> asErrorCodeMapping(boolean deduplicate, Properties properties) {
        // Convert to hash map and deduplicate values if specified
        // We are not interning to avoid polluting the string constant pool
        final Map<String, String> deduplicationMap = deduplicate
                ? new HashMap<String, String>(128)
                : Collections.<String, String>emptyMap();
        final Map<Integer, String> propsAsMap = new HashMap<>(properties.size(), 1);

        for (Object key : properties.keySet()) {
            if (!(key instanceof String)) continue;
            try {
                final String keyString = (String) key;
                final Integer errorCode = Integer.valueOf(keyString);

                String value = properties.getProperty(keyString);
                if (deduplicate) {
                    if (deduplicationMap.containsKey(value)) {
                        value = deduplicationMap.get(value);
                    } else {
                        deduplicationMap.put(value, value);
                    }
                }

                propsAsMap.put(errorCode, value);
            } catch (NumberFormatException e) {
                log.warn("Key " + key + " is not a number; ignored", e);
            }
        }
        return propsAsMap;
    }

    private static InputStream getResourceAsStream(String res) {
        InputStream in = GDSExceptionHelper.class.getResourceAsStream(res);
        if (in == null) {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream(res);
        }
        return in;
    }

    /**
     * This method returns a message for the specified error code.
     *
     * @param code
     *         Firebird error code
     * @return instance of <code>GDSExceptionHelper.GDSMessage</code> class where you can set desired parameters.
     */
    public static GDSMessage getMessage(int code) {
        final String message = messages.get(code);
        return new GDSMessage(message != null ? message : "No message for code " + code + " found.");
    }

    /**
     * Get the SQL state for the specified error code.
     *
     * @param code
     *         Firebird error code
     * @return SQL state for the Firebird error code, "HY000" if nothing found.
     */
    public static String getSQLState(int code) {
        return getSQLState(code, SQLStateConstants.SQL_STATE_GENERAL_ERROR);
    }

    /**
     * Get the SQL state for the specified error code.
     *
     * @param code
     *         Firebird error code
     * @param defaultSQLState
     *         The default SQLState to return
     * @return SQL state for the Firebird error code, or <code>defaultSQLState</code> if nothing found.
     */
    public static String getSQLState(int code, String defaultSQLState) {
        final String sqlState = sqlstates.get(code);
        return sqlState != null ? sqlState : defaultSQLState;
    }

    /**
     * This class wraps message template obtained from isc_error_msg.properties
     * file and allows to set parameters to the message.
     */
    public static final class GDSMessage {
        private final String template;
        private final String[] params;
        private final List<String> extraParameters = new ArrayList<>();

        /**
         * Constructs an instance of GDSMessage for the specified template.
         */
        public GDSMessage(String template) {
            this.template = template;
            params = new String[getParamCountInternal()];
        }

        /**
         * Returns the number of parameters for the message template.
         *
         * @return number of parameters.
         */
        public int getParamCount() {
            return params.length;
        }

        private int getParamCountInternal() {
            int count = 0;
            for (int i = 0; i < template.length(); i++) {
                if (template.charAt(i) == '{') count++;
            }
            return count;
        }

        /**
         * Sets the parameter value
         *
         * @param position
         *         the parameter number, 0 - first parameter.
         * @param text
         *         value of parameter
         */
        public void setParameter(int position, String text) {
            if (position < params.length) {
                params[position] = text;
            }
        }

        /**
         * Sets the parameter values.
         * <p>
         * Parameter values with an index value higher than the number of message arguments are added as extra
         * parameters.
         * </p>
         *
         * @param messageParameters
         *         Message parameters
         */
        public void setParameters(List<String> messageParameters) {
            int position;
            for (position = 0; position < Math.min(params.length, messageParameters.size()); position++) {
                params[position] = messageParameters.get(position);
            }
            // If we have more messageParameters we need to store them separately
            if (params.length < messageParameters.size()) {
                for (; position < messageParameters.size(); position++) {
                    extraParameters.add(messageParameters.get(position));
                }
            }
        }

        /**
         * Puts parameters into the template and return the obtained string.
         *
         * @return string representation of the message.
         */
        public String toString() {
            String message = template;
            for (int i = 0; i < params.length; i++) {
                String param = "{" + i + "}";
                int pos = message.indexOf(param);
                if (pos > -1) {
                    message = message.substring(0, pos)
                            + (params[i] != null ? params[i] : "(null)")
                            + message.substring(pos + param.length());
                }
            }
            // Include extra parameters at the end of the message
            for (String extraParameter : extraParameters) {
                message = message + "; " + extraParameter;
            }
            return message;
        }
    }

}
