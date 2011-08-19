/*
 * $Id$
 * 
 * Firebird Open Source J2EE Connector - JDBC Driver
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
package org.firebirdsql.ds;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Class containing static helper methods for reflective access.
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.2
 */
public final class ReflectionHelper {
    
    private ReflectionHelper() {}
    
    /**
     * Get all implemented interfaces by the class.
     * 
     * @param clazz class to inspect.
     * 
     * @return array of all implemented interfaces.
     */
    public static Class[] getAllInterfaces(Class clazz) {
        Set result = new HashSet();
        do {
            Class[] interfaces = clazz.getInterfaces();
            for (int i = 0; i < interfaces.length; i++) {
                result.add(interfaces[i]);
            }
            clazz = clazz.getSuperclass();
        } while(clazz.getSuperclass() != null);
        
        return (Class[])result.toArray(new Class[result.size()]);
    }
    
    /**
     * Helper function to find specified method in a specified class.
     * 
     * @param clazz
     *            class in which we look for a specified method.
     * @param name
     *            name of the method.
     * @param args
     *            types of method params.
     * 
     * @return instance of {@link Method} corresponding to specified name and
     *         param types.
     */
    public static Method findMethod(Class clazz, String name, Class[] args) {
        try {
            return clazz.getMethod(name, args);
        } catch (NoSuchMethodException nmex) {
            return null;
        }
    }

}
