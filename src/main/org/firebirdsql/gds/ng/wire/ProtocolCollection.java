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
package org.firebirdsql.gds.ng.wire;

import org.firebirdsql.jaybird.props.AttachmentProperties;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import java.util.*;
import java.util.stream.Stream;

import static java.lang.String.format;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.FB_PROTOCOL_FLAG;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.MAXIMUM_SUPPORTED_PROTOCOL_VERSION;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.MINIMUM_SUPPORTED_PROTOCOL_VERSION;
import static org.firebirdsql.gds.impl.wire.WireProtocolConstants.PROTOCOL_VERSION10;

/**
 * Collection of protocols for a connect request.
 * <p>
 * In general, {@link #getProtocols(String)} should be used with the {@code enableProtocol} connection property value.
 * </p>
 * 
 * @author Mark Rotteveel
 * @since 3.0
 */
public final class ProtocolCollection implements Iterable<ProtocolDescriptor> {

    private static final Logger log = LoggerFactory.getLogger(ProtocolCollection.class);

    private final Map<Integer, ProtocolDescriptor> descriptorMap;
    private static final ProtocolCollection AVAILABLE_PROTOCOLS;
    private static final ProtocolCollection SUPPORTED_PROTOCOLS;

    static {
        // Load protocol implementation information
        final Set<ProtocolDescriptor> availableProtocols = new HashSet<>();
        final Collection<ClassLoader> classLoaders = classLoadersForLoading();
        for (ClassLoader classLoader : classLoaders) {
            final ServiceLoader<ProtocolDescriptor> descriptors =
                    ServiceLoader.load(ProtocolDescriptor.class, classLoader);
            // We can't use foreach here, because the descriptors are lazily loaded, which might trigger a ServiceConfigurationError
            Iterator<ProtocolDescriptor> descriptorIterator = descriptors.iterator();
            int retry = 0;
            while (retry < 2) {
                try {
                    while (descriptorIterator.hasNext()) {
                        try {
                            ProtocolDescriptor protocol = descriptorIterator.next();
                            availableProtocols.add(protocol);
                        } catch (Exception | ServiceConfigurationError e) {
                            log.errorDebug("Could not load protocol descriptor (skipping)", e);
                        }
                    }
                    break;
                } catch (ServiceConfigurationError e) {
                    log.error("Error finding next ProtocolDescriptor", e);
                    retry++;
                }
            }
        }

        if (availableProtocols.isEmpty()) {
            for (ClassLoader classLoader : classLoaders) {
                availableProtocols.addAll(loadProtocolsFallback(classLoader));
            }
        }
        AVAILABLE_PROTOCOLS = create(availableProtocols.toArray(new ProtocolDescriptor[0]));
        SUPPORTED_PROTOCOLS = create(availableProtocols.stream()
                .filter(p -> MINIMUM_SUPPORTED_PROTOCOL_VERSION <= p.getVersion()
                             && p.getVersion() <= MAXIMUM_SUPPORTED_PROTOCOL_VERSION)
                .toArray(ProtocolDescriptor[]::new));
    }

    /**
     * List of class loaders to use for loading the {@link ProtocolDescriptor} implementations.
     *
     * @return Collection of {@link ClassLoader} instances
     */
    private static List<ClassLoader> classLoadersForLoading() {
        final List<ClassLoader> classLoaders = new ArrayList<>(2);
        final ClassLoader classLoader = ProtocolDescriptor.class.getClassLoader();
        if (classLoader != null) {
            classLoaders.add(classLoader);
        } else {
            classLoaders.add(ClassLoader.getSystemClassLoader());
        }

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null && !classLoaders.contains(contextClassLoader)) {
            classLoaders.add(contextClassLoader);
        }
        return classLoaders;
    }

    /**
     * Loads the protocols from a hardcoded list of class names.
     * <p>
     * This method is intended as a fallback in case the plugins could not be discovered from the
     * {@code META-INF/services/org.firebirdsql.gds.ng.wire.ProtocolDescriptor} file(s). See also
     * <a href="http://tracker.firebirdsql.org/browse/JDBC-325">issue JDBC-325</a>
     * </p>
     *
     * @param classLoader Class loader to use for loading
     * @return List of protocol descriptors
     */
    private static List<ProtocolDescriptor> loadProtocolsFallback(ClassLoader classLoader) {
        String[] protocolClasses = {
                "org.firebirdsql.gds.ng.wire.version10.Version10Descriptor",
                "org.firebirdsql.gds.ng.wire.version11.Version11Descriptor",
                "org.firebirdsql.gds.ng.wire.version12.Version12Descriptor",
                "org.firebirdsql.gds.ng.wire.version13.Version13Descriptor",
                "org.firebirdsql.gds.ng.wire.version15.Version15Descriptor",
                "org.firebirdsql.gds.ng.wire.version16.Version16Descriptor",
                "org.firebirdsql.gds.ng.wire.version18.Version18Descriptor",
        };
        final List<ProtocolDescriptor> protocols = new ArrayList<>(protocolClasses.length);
        for (String className : protocolClasses) {
            try {
                Class<?> clazz = classLoader.loadClass(className);
                ProtocolDescriptor protocol = (ProtocolDescriptor) clazz.getDeclaredConstructor().newInstance();
                protocols.add(protocol);
            } catch (Exception e) {
                log.warnDebug(format("Unable to load protocol %s in loadProtocolsFallback; skipping", className), e);
            }
        }
        return protocols;
    }

    private ProtocolCollection(Map<Integer, ProtocolDescriptor> protocolDescriptors) {
        // Note: we are not making a defensive copy as the Map should never leak outside this implementation
        this.descriptorMap = Collections.unmodifiableMap(protocolDescriptors);
    }

    @Override
    public Iterator<ProtocolDescriptor> iterator() {
        return descriptorMap.values().iterator();
    }

    /**
     * @param protocolVersion
     *            Version of the protocol
     * @return ProtocolDescriptor for the specified version, or null if the
     *         version is not in this ProtocolCollection
     */
    public ProtocolDescriptor getProtocolDescriptor(int protocolVersion) {
        return descriptorMap.get(protocolVersion);
    }
    
    /**
     * @return The protocol count
     */
    public int getProtocolCount() {
        return descriptorMap.size();
    }
    
    /**
     * Get a list with the protocol versions in this collection.
     * <p>
     * The returned List is created fresh on every call. Changes to
     * the list have no effect on this object. 
     * </p>
     * 
     * @return Protocol version numbers
     */
    public List<Integer> getProtocolVersions() {
        List<Integer> versions = new ArrayList<>();
        for (ProtocolDescriptor descriptor : this) {
            versions.add(descriptor.getVersion());
        }
        return versions;
    }

    /**
     * @return stream of the protocol descriptors held by this protocol collection
     * @since 6
     */
    public Stream<ProtocolDescriptor> stream() {
        return descriptorMap.values().stream();
    }

    /**
     * Creates a ProtocolCollection with the specified ProtocolDescriptors.
     * <p>
     * If <code>descriptors</code> contains multiple implementations with the
     * same value for {@link ProtocolDescriptor#getVersion()}, then the first
     * implementation with the highest value for
     * {@link ProtocolDescriptor#getWeight()} will be loaded into the
     * collection.
     * </p>
     * 
     * @param descriptors
     *            Vararg parameter with ProtocolDescriptors
     * @return ProtocolCollection
     */
    public static ProtocolCollection create(ProtocolDescriptor... descriptors) {
        Map<Integer, ProtocolDescriptor> descriptorMap = new HashMap<>();
        for (ProtocolDescriptor descriptor : descriptors) {
            ProtocolDescriptor existingDescriptor = descriptorMap.get(descriptor.getVersion());
            if (existingDescriptor == null || descriptor.getWeight() > existingDescriptor.getWeight()) {
                descriptorMap.put(descriptor.getVersion(), descriptor);
            }
        }
        return new ProtocolCollection(descriptorMap);
    }

    /**
     * Returns the supported ProtocolCollection.
     * <p>
     * The supported ProtocolCollection is created when this class is loaded by the classloader.
     * </p>
     * <p>
     * The returned collection is a subset of {@link #getAvailableProtocols()}, retaining only the supported protocol
     * versions.
     * </p>
     * 
     * @return supported ProtocolCollection
     * @see ProtocolCollection#create(ProtocolDescriptor...)
     * @since 6
     */
    public static ProtocolCollection getSupportedProtocols() {
        return SUPPORTED_PROTOCOLS;
    }

    /**
     * Returns the available ProtocolCollection.
     * <p>
     * The available ProtocolCollection is created when this class is loaded by the classloader.
     * </p>
     * <p>
     * This implementation uses the {@link ServiceLoader} to load the collection based on all {@link ProtocolDescriptor}
     * implementations found using all the
     * {@code /META-INF/services/org.firebirdsql.gds.ng.wire.ProtocolDescriptor} in the classpath. If multiple
     * implementations with the same value for {@link ProtocolDescriptor#getVersion()} are found, then the first
     * implementation with the highest value for {@link ProtocolDescriptor#getWeight()} will be loaded into the default
     * collection.
     * </p>
     *
     * @return available ProtocolCollection
     * @see ProtocolCollection#create(ProtocolDescriptor...)
     * @since 6
     */
    public static ProtocolCollection getAvailableProtocols() {
        return AVAILABLE_PROTOCOLS;
    }

    /**
     * Returns the protocol collection consisting of the supported protocol versions and the additional protocol
     * versions defined by the {@code enableProtocol} connection string property.
     *
     * @param enableProtocol
     *         enable protocol connection property value, see {@link AttachmentProperties#getEnableProtocol()}.
     * @return supported protocols and the <em>available</em> additional protocols listed in {@code enableProtocol}
     * @since 6
     */
    public static ProtocolCollection getProtocols(String enableProtocol) {
        if (enableProtocol == null) return getSupportedProtocols();
        return switch (enableProtocol.trim()) {
            case "" -> getSupportedProtocols();
            case "*" -> getAvailableProtocols();
            default -> getProtocols0(enableProtocol);
        };
    }

    private static ProtocolCollection getProtocols0(String enableProtocol) {
        return create(
                Stream.concat(
                                SUPPORTED_PROTOCOLS.stream(),
                                Arrays.stream(enableProtocol.split(","))
                                        .map(ProtocolCollection::tryParseInt)
                                        .filter(Objects::nonNull)
                                        .mapToInt(Integer::intValue)
                                        .distinct()
                                        .mapToObj(ProtocolCollection::tryGetProtocolDescriptorUnmaskedAndMasked)
                                        .filter(Objects::nonNull))
                        .toArray(ProtocolDescriptor[]::new));
    }

    private static ProtocolDescriptor tryGetProtocolDescriptorUnmaskedAndMasked(int version) {
        ProtocolDescriptor descriptor = AVAILABLE_PROTOCOLS.getProtocolDescriptor(version);
        if (descriptor == null && (version & FB_PROTOCOL_FLAG) != FB_PROTOCOL_FLAG && version != PROTOCOL_VERSION10) {
            descriptor = AVAILABLE_PROTOCOLS.getProtocolDescriptor(FB_PROTOCOL_FLAG | version);
        }
        return descriptor;
    }

    private static Integer tryParseInt(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
