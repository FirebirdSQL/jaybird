/*   The contents of this file are subject to the Mozilla Public
 *   License Version 1.1 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of
 *   the License at http://www.mozilla.org/MPL/
 *   Alternatively, the contents of this file may be used under the
 *   terms of the GNU Lesser General Public License Version 2 or later (the
 *   "LGPL"), in which case the provisions of the GPL are applicable
 *   instead of those above. You may obtain a copy of the Licence at
 *   http://www.gnu.org/copyleft/lgpl.html
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    relevant License for more details.
 *
 *    This file was created by members of the firebird development team.
 *    All individual contributions remain the Copyright (C) of those
 *    individuals.  Contributors to this file are either listed here or
 *    can be obtained from a CVS history command.
 *
 *    All rights reserved.
 */
package org.firebirdsql.logging;

public class LoggerFactory{
	
    private static boolean checked = false;
    private static boolean log4j = false;
	
    public static Logger getLogger(String name) {
        if (!checked){
            String sLog4j = System.getProperty("FBLog4j");
            if (sLog4j!=null && sLog4j.equals("false"))
                log4j = false;
            else{
                try {
                    Class verify = Class.forName("org.apache.log4j.Logger");
                    log4j = true;
                }
                catch (ClassNotFoundException cnfe){
                    log4j = false;
                }
            }
            checked = true;
        }
        if (log4j)
            return new Log4jLogger(name);
        else
            return null;
    }
	
    public static Logger getLogger(Class clazz) {
        return getLogger(clazz.getName());
    }
}
