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
package org.firebirdsql.gds.ng.wire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Collection of protocols for a connect request.
 * <p>
 * In general use {@link ProtocolCollection#getDefaultCollection()} to retrieve
 * the default collection.
 * </p>
 * 
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @since 2.3
 */
public final class ProtocolCollection implements Iterable<ProtocolDescriptor> {

    private final Map<Integer, ProtocolDescriptor> descriptorMap;
    private static final ProtocolCollection DEFAULT_COLLECTION;

    static {
        // Load protocol implementation information
        ServiceLoader<ProtocolDescriptor> descriptors = ServiceLoader.load(ProtocolDescriptor.class,
                ProtocolCollection.class.getClassLoader());
        List<ProtocolDescriptor> supportedProtocols = new ArrayList<ProtocolDescriptor>();
        for (ProtocolDescriptor protocol : descriptors) {
            supportedProtocols.add(protocol);
        }
        DEFAULT_COLLECTION = create(supportedProtocols.toArray(new ProtocolDescriptor[0]));
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
        List<Integer> versions = new ArrayList<Integer>();
        for (ProtocolDescriptor descriptor : this) {
            versions.add(descriptor.getVersion());
        }
        return versions;
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
        Map<Integer, ProtocolDescriptor> descriptorMap = new HashMap<Integer, ProtocolDescriptor>();
        for (ProtocolDescriptor descriptor : descriptors) {
            ProtocolDescriptor existingDescriptor = descriptorMap.get(descriptor.getVersion());
            if (existingDescriptor == null || descriptor.getWeight() > existingDescriptor.getWeight()) {
                descriptorMap.put(descriptor.getVersion(), descriptor);
            }
        }
        return new ProtocolCollection(descriptorMap);
    }

    /**
     * Returns the default ProtocolCollection.
     * <p>
     * The default ProtocolCollection is created when this class is loaded by
     * the classloader.
     * </p>
     * <p>
     * This implementation uses the {@link ServiceLoader} to load the default
     * collection based on all {@link ProtocolDescriptor} implementations found
     * using all the
     * <code>/META-INF/services/org.firebirdsql.gds.ng.wire.ProtocolDescriptor</code>
     * in the classpath. If multiple implementations with the same value for
     * {@link ProtocolDescriptor#getVersion()} are found, then the first
     * implementation with the highest value for
     * {@link ProtocolDescriptor#getWeight()} will be loaded into the default
     * collection.
     * </p>
     * 
     * @return The default ProtocolCollection
     * @see ProtocolCollection#create(ProtocolDescriptor...)
     */
    public static ProtocolCollection getDefaultCollection() {
        return DEFAULT_COLLECTION;
    }
}
