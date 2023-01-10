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
package org.firebirdsql.jdbc.cts;

import java.lang.reflect.*;
import java.util.Vector;

import junit.framework.*;

import com.sun.cts.harness.EETest;

/**
 * CTS test suite. This class is analogue of {@link junit.framework.TestSuite}
 * class for CTS test cases. Portions of code were copied from TestSuite impl.
 * 
 * @author Roman Rokytskyy
 */
public class CTSTestSuite extends TestSuite {

    /**
     * Create test suite for the specified CTS test class. This method creates
     * separate test cases for each test method.
     * 
     * @param theClass class with CTS test cases.
     */
    public CTSTestSuite(Class theClass) {
        setName(theClass.getName());
        
        try {
            getTestConstructor(theClass); // Avoid generating multiple error messages
        } catch (NoSuchMethodException e) {
            addTest(warning("Class " + theClass.getName() +
                            " has no public constructor TestCase(String name) " +
                            "or TestCase()"));
            return;
        }

        if (!Modifier.isPublic(theClass.getModifiers())) {
            addTest(warning("Class "+theClass.getName()+" is not public"));
            return;
        }

        Vector names= new Vector();
        if (EETest.class.isAssignableFrom(theClass)) {
            Method[] methods= theClass.getDeclaredMethods();
            for (int i= 0; i < methods.length; i++) {
                if (CTSTestConfig.isTestEnabled(theClass, methods[i]))
                    addTestMethod(methods[i], names, theClass);
            }
        } else 
            addTest(warning("Class " + theClass.getName() + " is not CTS test."));
    }

    /**
     * Gets a constructor which takes a single String as
     * its argument or a no arg constructor.
     */
    public static Constructor getTestConstructor(Class theClass) 
        throws NoSuchMethodException 
    {
        try {
            return theClass.getConstructor(null);   
        } catch (NoSuchMethodException e) {
            // fall through
        }
        return theClass.getConstructor(new Class[0]);
    }
    
    private void addTestMethod(Method m, Vector names, Class theClass) {
        String name= m.getName();
        if (names.contains(name))
            return;
        if (! isPublicTestMethod(m)) {
            if (isTestMethod(m))
                addTest(warning("Test method isn't public: "+m.getName()));
            return;
        }
        names.addElement(name);
        addTest(new CTSTestCase(theClass, m.getName()));
    }
    
    /**
     * Check if method is a public test method.
     * 
     * @param m method to check.
     * 
     * @return <code>true</code> if <code>m</code> is test method and is public.
     */
    private boolean isPublicTestMethod(Method m) {
        return isTestMethod(m) && Modifier.isPublic(m.getModifiers());
    }
    
    /**
     * Check if method is test method. Test method is a method name of which 
     * starts with "test", it has no parameters and does not return any results.
     * 
     * @param m method to check.
     * 
     * @return <code>true</code> if <code>m</code> is test method.
     */
    private boolean isTestMethod(Method m) {
        String name= m.getName();
        Class[] parameters= m.getParameterTypes();
        Class returnType= m.getReturnType();
        return parameters.length == 0 && name.startsWith("test") && 
            returnType.equals(Void.TYPE);
    }

    /**
     * Returns a test which will fail and log a warning message.
     */
    public static Test warning(final String message) {
        return new TestCase("warning") {
            protected void runTest() {
                fail(message);
            }
        };
    }
    
}
