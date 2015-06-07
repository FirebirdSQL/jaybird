/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
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
package org.firebirdsql.gds.ng;

/**
 * Mutable implementation of {@link IServiceProperties}.
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbImmutableServiceProperties
 * @since 3.0
 */
public final class FbServiceProperties extends AbstractAttachProperties<IServiceProperties>
        implements IServiceProperties {

    private String serviceName = IServiceProperties.DEFAULT_SERVICE_NAME;

    private FbImmutableServiceProperties immutableServicePropertiesCache;

    /**
     * Copy constructor for FbServiceProperties.
     * <p>
     * All properties defined in {@link IServiceProperties} are copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbServiceProperties(IServiceProperties src) {
        super(src);
        if (src != null) {
            serviceName = src.getServiceName();
        }
    }

    /**
     * Default constructor for FbServiceProperties
     */
    public FbServiceProperties() {
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
        dirtied();
    }

    @Override
    public String getAttachObjectName() {
        return getServiceName();
    }

    @Override
    public IServiceProperties asImmutable() {
        if (immutableServicePropertiesCache == null) {
            immutableServicePropertiesCache = new FbImmutableServiceProperties(this);
        }
        return immutableServicePropertiesCache;
    }

    @Override
    public IServiceProperties asNewMutable() {
        return new FbServiceProperties(this);
    }

    @Override
    protected void dirtied() {
        immutableServicePropertiesCache = null;
    }
}
