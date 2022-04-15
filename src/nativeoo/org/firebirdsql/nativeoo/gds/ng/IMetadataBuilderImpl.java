package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.FbMetadataBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IMaster;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IMessageMetadata;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IMetadataBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.IStatus;

import java.sql.SQLException;

import static org.firebirdsql.gds.ISCConstants.SQL_BLOB;
import static org.firebirdsql.gds.ISCConstants.SQL_BOOLEAN;
import static org.firebirdsql.gds.ISCConstants.SQL_DATE;
import static org.firebirdsql.gds.ISCConstants.SQL_DEC16;
import static org.firebirdsql.gds.ISCConstants.SQL_DEC34;
import static org.firebirdsql.gds.ISCConstants.SQL_DOUBLE;
import static org.firebirdsql.gds.ISCConstants.SQL_FLOAT;
import static org.firebirdsql.gds.ISCConstants.SQL_INT64;
import static org.firebirdsql.gds.ISCConstants.SQL_INT128;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;
import static org.firebirdsql.gds.ISCConstants.SQL_TEXT;
import static org.firebirdsql.gds.ISCConstants.SQL_TIMESTAMP;
import static org.firebirdsql.gds.ISCConstants.SQL_TYPE_TIME;
import static org.firebirdsql.gds.ISCConstants.SQL_VARYING;

/**
 * Implementation of {@link org.firebirdsql.gds.ng.FbMetadataBuilder} for native OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IMetadataBuilderImpl implements FbMetadataBuilder {

    private static final int SUBTYPE_NUMERIC = 1;
    private static final int SUBTYPE_DECIMAL = 2;

    private IDatabaseImpl database;
    private IMaster master;
    private IStatus status;
    private int fieldCount;
    private IMetadataBuilder metadataBuilder;
    private IMessageMetadata messageMetadata;

    public IMetadataBuilderImpl(FbDatabase database, int fieldCount) throws SQLException {
        this.database = (IDatabaseImpl)database;
        this.master = this.database.getMaster();
        this.status = this.database.getStatus();
        this.fieldCount = fieldCount;
        this.metadataBuilder = master.getMetadataBuilder(status, fieldCount);
    }

    @Override
    public FbMessageMetadata getMessageMetadata() throws SQLException {

        messageMetadata = metadataBuilder.getMetadata(status);
        IMessageMetadataImpl metadata = new IMessageMetadataImpl(this);

        return metadata;
    }

    public IMetadataBuilder getMetadataBuilder() {
        return this.metadataBuilder;
    }

    public FbDatabase getDatabase() {
        return this.database;
    }

    public int addField() throws SQLException {
        return this.metadataBuilder.addField(status);
    }

    @Override
    public void addSmallint(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_SHORT);
        metadataBuilder.setLength(status, index, Short.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addInteger(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_LONG);
        metadataBuilder.setLength(status, index, Integer.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addBigint(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_INT64);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addFloat(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_FLOAT);
        metadataBuilder.setLength(status, index, Float.SIZE / Byte.SIZE);
    }

    @Override
    public void addNumeric(int index, int size, int scale) throws SQLException {
        int length = 0;
        if (size < 5) {
            metadataBuilder.setType(status, index, SQL_SHORT);
            length = 2;
        } else if (size < 10) {
            metadataBuilder.setType(status, index, SQL_LONG);
            length = 4;
        } else if (size < 19) {
            metadataBuilder.setType(status, index, SQL_INT64);
            length = 8;
        } else {
            metadataBuilder.setType(status, index, SQL_INT128);
            length = 16;
        }
        metadataBuilder.setLength(status, index, length);
        if (scale > 0)
            scale = -scale;
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_NUMERIC);
    }

    @Override
    public void addDecimal(int index, int size, int scale) throws SQLException {
        int length = 0;
        if (size < 5) {
            metadataBuilder.setType(status, index, SQL_SHORT);
            length = 2;
        } else if (size < 10) {
            metadataBuilder.setType(status, index, SQL_LONG);
            length = 4;
        } else if (size < 19) {
            metadataBuilder.setType(status, index, SQL_INT64);
            length = 8;
        } else {
            metadataBuilder.setType(status, index, SQL_INT128);
            length = 16;
        }
        metadataBuilder.setLength(status, index, length);
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_DECIMAL);
    }

    @Override
    public void addDouble(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_DOUBLE);
        metadataBuilder.setLength(status, index, Double.SIZE / Byte.SIZE);
    }

    @Override
    public void addDecfloat16(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_DEC16);
        metadataBuilder.setLength(status, index, Byte.SIZE);
    }

    @Override
    public void addDecfloat34(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_DEC34);
        metadataBuilder.setLength(status, index, Byte.SIZE * 2);
    }

    @Override
    public void addBlob(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_BLOB);
        metadataBuilder.setLength(status, index, (Integer.SIZE / Byte.SIZE) * 2);
    }

    @Override
    public void addBlob(int index, int subtype) throws SQLException {
        metadataBuilder.setType(status, index, SQL_BLOB);
        metadataBuilder.setLength(status, index, (Integer.SIZE / Byte.SIZE) * 2);
        metadataBuilder.setSubType(status, index, subtype);
    }

    @Override
    public void addBoolean(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_BOOLEAN);
        metadataBuilder.setLength(status, index, 1);
    }

    @Override
    public void addDate(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_DATE);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
    }

    @Override
    public void addTime(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_TYPE_TIME);
        metadataBuilder.setLength(status, index, Integer.SIZE / Byte.SIZE);
    }

    @Override
    public void addTimestamp(int index) throws SQLException {
        metadataBuilder.setType(status, index, SQL_TIMESTAMP);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
    }

    @Override
    public void addChar(int index, int length) throws SQLException {
        metadataBuilder.setType(status, index, SQL_TEXT);
        metadataBuilder.setLength(status, index, length);
    }

    @Override
    public void addVarchar(int index, int length) throws SQLException {
        metadataBuilder.setType(status, index, SQL_VARYING);
        metadataBuilder.setLength(status, index, length);
    }

    @Override
    public void addChar(int index, int length, int charSet) throws SQLException {
        metadataBuilder.setType(status, index, SQL_TEXT);
        metadataBuilder.setLength(status, index, length);
        metadataBuilder.setCharSet(status, index, charSet);
    }

    @Override
    public void addVarchar(int index, int length, int charSet) throws SQLException {
        metadataBuilder.setType(status, index, SQL_VARYING);
        metadataBuilder.setLength(status, index, length);
        metadataBuilder.setCharSet(status, index, charSet);
    }

    @Override
    public void addDecDecimal(int index, int size, int scale) throws SQLException {
        addDecimal(index, size, scale);
    }

    @Override
    public void addDecNumeric(int index, int size, int scale) throws SQLException {
        addNumeric(index, size, scale);
    }
}
