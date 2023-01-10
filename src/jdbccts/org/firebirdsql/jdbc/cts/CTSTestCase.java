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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Arrays;
import java.util.Properties;

import com.sun.cts.harness.EETest;
import com.sun.cts.util.TestUtil;

import junit.framework.*;

/**
 * CTS test case. This is {@link junit.framework.TestCase} extension that allows
 * running CTS test cases using "appclient" vehicle as normal JUnit test cases. 
 * 
 * @author Roman Rokytskyy
 */
public class CTSTestCase extends TestCase {
    
    private static final Class[] SETUP_PARAMS = new Class[] {
        new String[0].getClass(),
        Properties.class
    };

    private Properties props = new Properties();
    
    private Class testCase;
    private String testName;
    
    private Method setupMethod;
    private Method cleanupMethod;
    
    /**
     * Create instance of this class for the specified test class and test name.
     * 
     * @param testCase class from CTS that has to be executed.
     * @param testName name of the test case.
     */
    public CTSTestCase(Class testCase, String testName) {
        super("testMethod");
        
        this.props.putAll(CTSTestConfig.getProperties());
        this.testCase = testCase;
        this.testName = testName;
        
        Method[] methods = testCase.getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals("setup") && 
                Arrays.equals(methods[i].getParameterTypes(), SETUP_PARAMS))
                    setupMethod = methods[i];
            else
            if (methods[i].getName().equals("cleanup") &&
                methods[i].getParameterTypes().length == 0)
                    cleanupMethod = methods[i];
        }
    }

    private Object test;
    
    /**
     * Set up this test case.
     */
    protected void setUp() throws Exception {
        super.setUp();
        
        TestUtil.NEW_LINE = props.getProperty("line.separator");
        TestUtil.traceflag = CTSTestConfig.isTraceFlag();
        
        TestUtil.iWhereAreWe = 1;
        
        TestUtil.setCurrentTest(testName, 
                new PrintWriter(System.out), new PrintWriter(System.err));

        TestUtil.separator2();

        test = testCase.newInstance();
        
        TestUtil.logMsg("Beginning Test: " + testName);
        TestUtil.separator2();
        
        if (setupMethod != null) {
            Properties tempProps = new Properties();
            tempProps.putAll(props);
            setupMethod.invoke(test, 
                    new Object[] { new String[0], tempProps});
        }
        
    }

    /**
     * Tear this test case down.
     */
    protected void tearDown() throws Exception {
        if (cleanupMethod != null)
            cleanupMethod.invoke(test, null);

        TestUtil.separator2();
        
        super.tearDown();
    }

    /**
     * Test method that will be executed.
     * 
     * @throws Throwable if something went wrong.
     */
    public void testMethod() throws Throwable {
        try {
            Method testMethod = testCase.getMethod(testName, null);
            
            try {
                testMethod.invoke(test, null);
            } catch(InvocationTargetException ex) {
                if (ex.getTargetException() instanceof EETest.Fault)
                    throw new AssertionFailedError(
                            exceptionToString(ex.getTargetException()));
                else
                    throw ex.getTargetException();
            }
            
        } catch(NoSuchElementException ex) {
            throw new AssertionFailedError(
                "Method " + testName + " not found in class " + 
                testCase.getName());
        }
    }
    
    /**
     * Get number of test cases.
     */
    public int countTestCases() {
        return 1;
    }

    /**
     * Get name of the test case.
     */
    public String getName() {
        return testName;
    }

    /**
     * Set name of the test case.
     * 
     * @param name name of the test case.
     */
    public void setName(String name) {
        this.testName = name;
    }

    /**
     * Get name of the test case.
     * 
     * @deprecated use {@link #getName()} instead.
     */
    public String name() {
        return getName();
    }

    /**
     * Converts the stack trace into a string
     */
    private static String exceptionToString(Throwable t) {
        StringWriter stringWriter= new StringWriter();
        PrintWriter writer= new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();

    }
    
}
