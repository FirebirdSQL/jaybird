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

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.gds.DatabaseParameterBuffer;
import org.firebirdsql.gds.Parameter;
import org.firebirdsql.gds.impl.DatabaseParameterBufferImp;
import org.firebirdsql.logging.Logger;
import org.firebirdsql.logging.LoggerFactory;

import static org.firebirdsql.gds.ISCConstants.*;

/**
 * Mutable implementation of {@link IConnectionProperties}
 *
 * @author <a href="mailto:mrotteveel@users.sourceforge.net">Mark Rotteveel</a>
 * @see FbImmutableConnectionProperties
 * @since 3.0
 */
public final class FbConnectionProperties extends AbstractAttachProperties<IConnectionProperties>
        implements IConnectionProperties {

    private static final Logger log = LoggerFactory.getLogger(FbConnectionProperties.class);

    private String databaseName;
    private short connectionDialect = IConnectionProperties.DEFAULT_DIALECT;
    private int pageCacheSize;
    private boolean resultSetDefaultHoldable;
    private boolean columnLabelForName;
    private final DatabaseParameterBuffer extraDatabaseParameters = new DatabaseParameterBufferImp(
            DatabaseParameterBufferImp.DpbMetaData.DPB_VERSION_1,
            EncodingFactory.getDefaultInstance().getDefaultEncoding());

    private FbImmutableConnectionProperties immutableConnectionPropertiesCache;

    /**
     * Copy constructor for FbConnectionProperties.
     * <p>
     * All properties defined in {@link IConnectionProperties} are
     * copied from <code>src</code> to the new instance.
     * </p>
     *
     * @param src
     *         Source to copy from
     */
    public FbConnectionProperties(IConnectionProperties src) {
        super(src);
        if (src != null) {
            databaseName = src.getDatabaseName();
            connectionDialect = src.getConnectionDialect();
            pageCacheSize = src.getPageCacheSize();
            resultSetDefaultHoldable = src.isResultSetDefaultHoldable();
            columnLabelForName = src.isColumnLabelForName();
            for (Parameter parameter : src.getExtraDatabaseParameters()) {
                parameter.copyTo(extraDatabaseParameters, null);
            }
        }
    }

    /**
     * Default constructor for FbConnectionProperties
     */
    public FbConnectionProperties() {
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
        dirtied();
    }

    @Override
    public String getAttachObjectName() {
        return getDatabaseName();
    }

    @Override
    public short getConnectionDialect() {
        return connectionDialect;
    }

    @Override
    public void setConnectionDialect(short connectionDialect) {
        this.connectionDialect = connectionDialect;
        dirtied();
    }

    @Override
    public int getPageCacheSize() {
        return pageCacheSize;
    }

    @Override
    public void setPageCacheSize(int pageCacheSize) {
        this.pageCacheSize = pageCacheSize;
        dirtied();
    }

    @Override
    public void setResultSetDefaultHoldable(final boolean holdable) {
        resultSetDefaultHoldable = holdable;
        dirtied();
    }

    @Override
    public boolean isResultSetDefaultHoldable() {
        return resultSetDefaultHoldable;
    }

    @Override
    public void setColumnLabelForName(final boolean columnLabelForName) {
        this.columnLabelForName = columnLabelForName;
        dirtied();
    }

    @Override
    public boolean isColumnLabelForName() {
        return columnLabelForName;
    }

    @Override
    public DatabaseParameterBuffer getExtraDatabaseParameters() {
        return extraDatabaseParameters;
    }

    @Override
    public IConnectionProperties asImmutable() {
        if (immutableConnectionPropertiesCache == null) {
            immutableConnectionPropertiesCache = new FbImmutableConnectionProperties(this);
        }
        return immutableConnectionPropertiesCache;
    }

    @Override
    public IConnectionProperties asNewMutable() {
        return new FbConnectionProperties(this);
    }

    /**
     * Method to populate an FbConnectionProperties from a database parameter buffer.
     * <p>
     * Unsupported or unknown properties are ignored.
     * </p>
     *
     * @param dpb
     *         Database parameter buffer
     * @deprecated TODO: This method is only intended to simplify migration of the protocol implementation and needs to be removed.
     */
    @Deprecated
    public void fromDpb(DatabaseParameterBuffer dpb) {
        for (Parameter parameter : dpb) {
            switch (parameter.getType()) {
            case isc_dpb_user_name:
                setUser(parameter.getValueAsString());
                break;
            case isc_dpb_password:
                setPassword(parameter.getValueAsString());
                break;
            case isc_dpb_sql_role_name:
                setRoleName(parameter.getValueAsString());
                break;
            case isc_dpb_lc_ctype:
                setEncoding(parameter.getValueAsString());
                break;
            case isc_dpb_local_encoding:
                setCharSet(parameter.getValueAsString());
                break;
            case isc_dpb_sql_dialect:
                setConnectionDialect((short) parameter.getValueAsInt());
                break;
            case isc_dpb_num_buffers:
                setPageCacheSize(parameter.getValueAsInt());
                break;
            case isc_dpb_connect_timeout:
                setConnectTimeout(parameter.getValueAsInt());
                break;
            case isc_dpb_so_timeout:
                setSoTimeout(parameter.getValueAsInt());
                break;
            case isc_dpb_socket_buffer_size:
                setSocketBufferSize(parameter.getValueAsInt());
                break;
            case isc_dpb_result_set_holdable:
                setResultSetDefaultHoldable(true);
                break;
            case isc_dpb_column_label_for_name:
                setColumnLabelForName(true);
                break;
            case isc_dpb_utf8_filename:
                // Filter out, handled explicitly in protocol implementation
                break;
            case isc_dpb_specific_auth_data:
                break;
            default:
                log.warn(String.format("Unknown or unsupported parameter with type %d added to extra database parameters", parameter.getType()));
                parameter.copyTo(getExtraDatabaseParameters(), null);
                dirtied();
            }
        }
    }

    @Override
    protected void dirtied() {
        immutableConnectionPropertiesCache = null;
    }
}
