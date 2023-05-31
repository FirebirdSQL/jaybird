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

import java.io.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * Configuration for CTS test suite.
 * 
 * @author Roman Rokytskyy
 */
public class CTSTestConfig {
    public static final String CTS_CONFIG_FILE = "cts.config.file";
    public static final String CTS_CONFIG_RESOURCE = "cts.config.resource";

    public static final String CTS_EXCLUDES_FILE = "cts.excludes.file";
    public static final String CTS_EXCLUDES_RESOURCE = "cts.excludes.resource";
    
    public static final String CTS_DATABASE_URL = "cts.db.url";
    public static final String CTS_DATABASE_USERNAME = "cts.db.username";
    public static final String CTS_DATABASE_PASSWORD = "cts.db.password";

    private static Properties config = new Properties();
    private static Properties excludes = new Properties();
    
    static {
        try {
            
            String configFile = System.getProperty(CTS_CONFIG_FILE);
            if (configFile != null)
                loadConfigFile(configFile);
            else {
                String configResource = System.getProperty(CTS_CONFIG_RESOURCE);
                if (configResource != null)
                    loadConfigResource(configResource);
            }
            
            String excludesFile = System.getProperty(CTS_EXCLUDES_FILE);
            if (excludesFile != null)
                loadExcludesFile(excludesFile);
            else {
                String excludesResource = System.getProperty(CTS_EXCLUDES_RESOURCE);
                if (excludesResource != null)
                    loadExcludesResource(excludesResource);
            }

            String url = System.getProperty(CTS_DATABASE_URL);
            if (url != null)
                config.put("db1", url);
            
            String user = System.getProperty(CTS_DATABASE_USERNAME);
            if (user != null)
                config.put("user1", user);
            
            String password = System.getProperty(CTS_DATABASE_PASSWORD);
            if (password != null)
                config.put("password1", password);
            
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    
    private static Properties loadPropertiesFromFile(File file) 
        throws IOException 
    {
        if(!file.exists())
            throw new FileNotFoundException(file.getCanonicalPath());
        
        FileInputStream in = new FileInputStream(file);
        
        // load into temp collection, so we don't corrupt main config.
        Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }
        return props;
    }
    
    private static Properties loadPropertiesFromResource(String resourceName) 
        throws IOException 
    {
        ClassLoader cl = CTSTestConfig.class.getClassLoader();
        InputStream in = cl.getResourceAsStream(resourceName);
        if (in == null) {
            cl = Thread.currentThread().getContextClassLoader();
            in = cl.getResourceAsStream(resourceName);
        }
        
        if (in == null)
            throw new FileNotFoundException(
                    "Resource " + resourceName + " not found.");
        
        Properties props = new Properties();
        try {
            props.load(in);
        } finally {
            in.close();
        }
        return props;
    }

    /**
     * Load excludes from file.
     * 
     * @param fileName file from which excludes will be loaded.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadExcludesFile(String fileName) throws IOException {
        loadExcludesFile(new File(fileName));
    }
    
    /**
     * Load excludes from file.
     * 
     * @param file file from which excludes will be loaded.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadExcludesFile(File file) throws IOException {
        excludes.clear();
        excludes.putAll(loadPropertiesFromFile(file));
    }

    /**
     * Load excludes from resource in current class loader.
     * 
     * @param resourceName name of the resource.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadExcludesResource(String resourceName) throws IOException {
        excludes.clear();
        excludes.putAll(loadPropertiesFromResource(resourceName));
    }
    
    /**
     * Load configuration from file.
     * 
     * @param fileName name of the file from which configuration has to be loaded.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadConfigFile(String fileName) throws IOException {
        loadConfigFile(new File(fileName));
    }
    
    /**
     * Load configuration from file.
     * 
     * @param file file from which configuration has to be loaded.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadConfigFile(File file) throws IOException {
        config.clear();
        config.putAll(loadPropertiesFromFile(file));
    }
    
    /**
     * Load configuration from resource in current class loader.
     * 
     * @param resourceName name of the resource.
     * 
     * @throws IOException if I/O error occurs.
     */
    public static void loadConfigResource(String resourceName) throws IOException {
        config.clear();
        config.putAll(loadPropertiesFromResource(resourceName));
    }
    
    /**
     * Get current configuration as properties.
     * 
     * @return instance of {@link Properties} containing current configuration.
     */
    public static Properties getProperties() {
        return config;
    }
    
    /**
     * Is trace enabled?
     * 
     * @return <code>true</code> if trace is enabled.
     */
    public static boolean isTraceFlag() {
        return Boolean.valueOf(config.getProperty(
                        "harness.log.traceflag", "true")).booleanValue();        
    }

    /**
     * Check if test is enabled.
     * 
     * @param clazz class where test is defined.
     * @param method method implementing test case.
     * 
     * @return <code>true</code> if test is enabled.
     */
    public static boolean isTestEnabled(Class clazz, Method method) {
        String fullName = clazz.getName() + "." + method.getName();
        
        if (excludes.containsKey(fullName))
            return false;
        else
            return true;
    }
}
