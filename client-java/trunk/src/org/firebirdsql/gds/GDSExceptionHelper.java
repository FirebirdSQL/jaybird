/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Original developer David Jencks
 *
 * Contributor(s):
 *  Roman Rokytskyy
 *
 * Alternatively, the contents of this file may be used under the
 * terms of the GNU Lesser General Public License Version 2.1 or later
 * (the "LGPL"), in which case the provisions of the LGPL are applicable
 * instead of those above.  If you wish to allow use of your
 * version of this file only under the terms of the LGPL and not to
 * allow others to use your version of this file under the MPL,
 * indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by
 * the LGPL.  If you do not delete the provisions above, a recipient
 * may use your version of this file under either the MPL or the
 * LGPL.
 */

/*
 * CVS modification log:
 * $Log$
 * Revision 1.1  2001/07/16 03:57:43  d_jencks
 * added text error messages to GDSExceptions, thanks Roman Rokytskyy
 *
 */
package org.firebirdsql.gds;

/**
 * This class is supposed to return messages for the specified error code.
 * It loads all messages during the class initialization and keeps messages
 * in the static <code>java.util.Properties</code> variable.
 */
public class GDSExceptionHelper {
    private static final String MESSAGES = "org.firebirdsql.gds.isc_error_msg";
    private static java.util.Properties messages = new java.util.Properties();

    static {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            String res = MESSAGES.replace('.','/') + ".properties";
            java.io.InputStream in = cl.getResourceAsStream(res);
            messages.load(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This method returns a message for the specified error code.
     * @param code Firebird error code
     * @return instance of <code>GDSExceptionHelper.GDSMesssage</code> class
     * where you can set desired parameters.
     */
    public static GDSMessage getMessage(int code) {
        return new GDSMessage(messages.getProperty(
            "" + code, "No message for code " + code + "found."));
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
                String temp = message.substring(0, pos);
                temp += (params[i] == null) ? "" : params[i];
                temp += message.substring(pos + param.length());
                message = temp;
            }
            return message;
        }
    }

}