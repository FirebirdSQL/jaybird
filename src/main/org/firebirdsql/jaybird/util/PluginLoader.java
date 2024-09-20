/*
 * Firebird Open Source JDBC Driver
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
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.jaybird.util;

import org.jspecify.annotations.Nullable;

import java.lang.System.Logger.Level;
import java.util.*;

import static java.util.Collections.unmodifiableSet;

/**
 * Helper methods for loading plugins using {@link java.util.ServiceLoader}.
 *
 * @author Mark Rotteveel
 * @since 6
 */
public final class PluginLoader {

    private static final Set<ClassSource> DEFAULT_SOURCES = unmodifiableSet(EnumSet.allOf(ClassSource.class));

    private PluginLoader() {
        // no instances
    }

    /**
     * Finds plugins of type {@code spiClass} on the classpath using the class loader of the {@code spiClass} and
     * the thread context class loader.
     *
     * @param spiClass
     *         service provider interface (SPI) of the plugin
     * @param fallbackClassNames
     *         list of class names to load when no plugin SPI instances were found using {@code ServiceLoader}
     * @param <T>
     *         type of the plugin SPI
     * @return collection of plugin SPI instances
     */
    public static <T> Collection<T> findPlugins(Class<T> spiClass, Collection<String> fallbackClassNames) {
        return findPlugins(spiClass, fallbackClassNames, DEFAULT_SOURCES);
    }

    /**
     * Finds plugins of type {@code spiClass} on the classpath using class loader indicated by {@code classResource}.
     *
     * @param spiClass
     *         service provider interface (SPI) of the plugin
     * @param fallbackClassNames
     *         list of class names to load when no plugin SPI instances were found using {@code ServiceLoader}
     * @param classSource
     *         source to determine the class loader to use
     * @param <T>
     *         type of the plugin SPI
     * @return collection of plugin SPI instances
     */
    public static <T> Collection<T> findPlugins(
            Class<T> spiClass, Collection<String> fallbackClassNames, ClassSource classSource) {
        return findPlugins(spiClass, fallbackClassNames, Set.of(classSource));
    }

    /**
     * Finds plugins of type {@code spiClass} on the classpath using class loader indicated by {@code classResource}.
     *
     * @param spiClass
     *         service provider interface (SPI) of the plugin
     * @param fallbackClassNames
     *         list of class names to load when no plugin SPI instances were found using {@code ServiceLoader}
     * @param classSources
     *         sources to determine the class loader to use
     * @param <T>
     *         type of the plugin SPI
     * @return collection of plugin SPI instances
     */
    public static <T> Collection<T> findPlugins(
            Class<T> spiClass, Collection<String> fallbackClassNames, ClassSource... classSources) {
        EnumSet<ClassSource> classSourceSet = EnumSet.noneOf(ClassSource.class);
        classSourceSet.addAll(Arrays.asList(classSources));
        return findPlugins(spiClass, fallbackClassNames, classSourceSet);
    }

    /**
     * Finds plugins of type {@code spiClass} on the classpath using class loader indicated by {@code classResource}.
     *
     * @param spiClass
     *         service provider interface (SPI) of the plugin
     * @param fallbackClassNames
     *         list of class names to load when no plugin SPI instances were found using {@code ServiceLoader}
     * @param classSources
     *         sources to determine the class loader to use
     * @param <T>
     *         type of the plugin SPI
     * @return collection of plugin SPI instances
     */
    public static <T> Collection<T> findPlugins(
            Class<T> spiClass, Collection<String> fallbackClassNames, Set<ClassSource> classSources) {
        if (!spiClass.isInterface()) {
            throw new IllegalArgumentException("excepted interface type, received " + spiClass);
        }
        if (classSources.isEmpty()) throw new IllegalArgumentException("at least one ClassSource is required");

        final Collection<ClassLoader> classLoaders = classLoadersForLoading(spiClass, classSources);

        final var plugins = new LinkedHashSet<T>();
        for (ClassLoader cl : classLoaders) {
            plugins.addAll(findPlugins(spiClass, cl));
        }

        if (!plugins.isEmpty()) {
            return plugins;
        }
        return loadFallbackPlugins(spiClass, fallbackClassNames, classLoaders);
    }

    @SuppressWarnings("java:S1141")
    private static <T> Set<T> findPlugins(Class<T> spiClass, ClassLoader cl) {
        var pluginLoader = ServiceLoader.load(spiClass, cl);
        // We can't use foreach here, because the descriptors are lazily loaded, which might trigger a ServiceConfigurationError
        Iterator<T> pluginIterator = pluginLoader.iterator();
        int retry = 0;
        var plugins =  new LinkedHashSet<T>();
        while (retry < 2) {
            try {
                while (pluginIterator.hasNext()) {
                    try {
                        plugins.add(pluginIterator.next());
                    } catch (Exception | ServiceConfigurationError e) {
                        String message = "Could not load " + spiClass.getSimpleName() + " (skipping)";
                        var log = System.getLogger(PluginLoader.class.getName());
                        log.log(Level.ERROR, message + "; see debug level for stacktrace");
                        log.log(Level.DEBUG, message, e);
                    }
                }
                break;
            } catch (ServiceConfigurationError e) {
                System.getLogger(PluginLoader.class.getName())
                        .log(Level.ERROR, "Error finding next " + spiClass.getSimpleName(), e);
                retry++;
            }
        }
        return plugins;
    }

    private static <T> Set<T> loadFallbackPlugins(Class<T> spiClass, Collection<String> fallbackClassNames,
            Collection<ClassLoader> classLoaders) {
        System.getLogger(PluginLoader.class.getName()).log(Level.WARNING,
                "Could not find any {0} through service loader; using fallback strategy", spiClass.getSimpleName());
        LinkedHashSet<T> plugins = new LinkedHashSet<>();
        for (ClassLoader cl : classLoaders) {
            for (String className : fallbackClassNames) {
                try {
                    Class<?> clazz = Class.forName(className, false, cl);
                    if (!spiClass.isAssignableFrom(clazz)) {
                        System.getLogger(PluginLoader.class.getName()).log(Level.WARNING,
                                "Class {0} is not an instance of plugin type {1}", className, spiClass.getName());
                        continue;
                    }
                    plugins.add(spiClass.cast(clazz.getDeclaredConstructor().newInstance()));

                } catch (Exception e) {
                    var log = System.getLogger(PluginLoader.class.getName());
                    if (log.isLoggable(Level.WARNING)) {
                        String message = "Unable to load " + spiClass.getSimpleName() + " " + className
                                + " as fallback; skipping";
                        log.log(Level.WARNING, message + "; see debug level for stacktrace");
                        log.log(Level.DEBUG, message, e);
                    }
                }
            }
            if (!plugins.isEmpty()) return plugins;
        }
        return Set.of();
    }

    /**
     * List of class loaders to use for loading the plugin implementations.
     *
     * @param spiClass
     *         service provider interface (SPI) of the plugin
     * @param classSources
     *         sources to determine the class loader to use
     * @return Collection of {@link ClassLoader} instances
     */
    private static Collection<ClassLoader> classLoadersForLoading(Class<?> spiClass, Set<ClassSource> classSources) {
        var classLoaders = new ArrayList<ClassLoader>(classSources.size());
        for (ClassSource classSource : classSources) {
            ClassLoader cl = classSource.getClassLoader(spiClass);
            if (cl != null && !classLoaders.contains(cl)) classLoaders.add(cl);
        }
        return classLoaders;
    }

    /**
     * Sources for loading classes (determines which class loaders to use)
     */
    public enum ClassSource {
        PLUGIN_CLASS_LOADER {
            @Override
            @Nullable ClassLoader getClassLoader(Class<?> spiClass) {
                ClassLoader cl = spiClass.getClassLoader();
                return cl != null ? cl : ClassLoader.getSystemClassLoader();
            }
        },
        CONTEXT_CLASS_LOADER {
            @Override
            @Nullable ClassLoader getClassLoader(Class<?> spiClass) {
                return Thread.currentThread().getContextClassLoader();
            }
        };

        /**
         * Determines the class loader to use.
         *
         * @param spiClass
         *         service provider interface (SPI) of the plugin
         * @return class loader to use (can be {@code null})
         */
        abstract @Nullable ClassLoader getClassLoader(Class<?> spiClass);
    }
}
