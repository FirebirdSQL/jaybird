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
package org.firebirdsql.logging;

/**
 * Describe class <code>LoggerFactory</code> here.
 *
 * @author <a href="mailto:brodsom@users.sourceforge.net">Blas Rodriguez Somoza</a>
 * @version 1.0
 */
public class LoggerFactory{
    
    private static final boolean forceConsoleLogger = false;
    
    private static boolean checked = false;
    private static boolean log4j = false;
    
    public static Logger getLogger(String name,boolean def) {
        if (!checked){
            String sLog4j = System.getProperty("FBLog4j");
            if (!def){
                if (sLog4j != null && sLog4j.equals("true"))
                    log4j = true;
                      else
                    log4j = false;
                 }
            else{
                if (sLog4j != null && sLog4j.equals("false"))
                    log4j = false;
                      else
                    log4j = true;
                 }

            if (log4j){
                 try {
                     Class.forName("org.apache.log4j.Category");
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
        if (forceConsoleLogger)
            return new ConsoleLogger(name);
        else
            return null;
    }
    
    public static Logger getLogger(Class clazz, boolean def) {
        return getLogger(clazz.getName(), def);
    }
}
