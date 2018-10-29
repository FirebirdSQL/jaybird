package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbDatabase;
import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.FbMetadataBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

import static org.firebirdsql.gds.ISCConstants.*;

/**
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

    public IMetadataBuilderImpl(FbDatabase database, int fieldCount) throws FbException {
        this.database = (IDatabaseImpl)database;
        this.master = this.database.getMaster();
        this.status = this.database.getStatus();
        this.fieldCount = fieldCount;
        this.metadataBuilder = master.getMetadataBuilder(status, fieldCount);
    }

    @Override
    public FbMessageMetadata getMessageMetadata() throws FbException {

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

    public int addField() throws FbException {
        return this.metadataBuilder.addField(status);
    }

    @Override
    public void addSmallint(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_SHORT);
        metadataBuilder.setLength(status, index, Short.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addInteger(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_LONG);
        metadataBuilder.setLength(status, index, Integer.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addBigint(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_INT64);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
        metadataBuilder.setScale(status, index, 0);
    }

    @Override
    public void addFloat(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_FLOAT);
        metadataBuilder.setLength(status, index, Float.SIZE / Byte.SIZE);
    }

    @Override
    public void addNumeric(int index, int size, int scale) throws FbException {
        metadataBuilder.setType(status, index, SQL_SHORT);
        metadataBuilder.setLength(status, index, size);
        if (scale > 0)
            scale = -scale;
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_NUMERIC);
    }

    @Override
    public void addDecimal(int index, int size, int scale) throws FbException {
        metadataBuilder.setType(status, index, SQL_LONG);
        metadataBuilder.setLength(status, index, size);
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_DECIMAL);
    }

    @Override
    public void addDouble(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_DOUBLE);
        metadataBuilder.setLength(status, index, Double.SIZE / Byte.SIZE);
    }

    @Override
    public void addDecfloat16(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_DEC16);
        metadataBuilder.setLength(status, index, IDecFloat16.STRING_SIZE);
    }

    @Override
    public void addDecfloat34(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_DEC34);
        metadataBuilder.setLength(status, index, IDecFloat34.STRING_SIZE);
    }

    @Override
    public void addBlob(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_BLOB);
        metadataBuilder.setLength(status, index, (Integer.SIZE / Byte.SIZE) * 2);
    }

    @Override
    public void addBlob(int index, int subtype) throws FbException {
        metadataBuilder.setType(status, index, SQL_BLOB);
        metadataBuilder.setLength(status, index, (Integer.SIZE / Byte.SIZE) * 2);
        metadataBuilder.setSubType(status, index, subtype);
    }

    @Override
    public void addBoolean(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_BOOLEAN);
        metadataBuilder.setLength(status, index, Short.SIZE / Byte.SIZE);
    }

    @Override
    public void addDate(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_DATE);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
    }

    @Override
    public void addTime(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_TYPE_TIME);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
    }

    @Override
    public void addTimestamp(int index) throws FbException {
        metadataBuilder.setType(status, index, SQL_TIMESTAMP);
        metadataBuilder.setLength(status, index, Long.SIZE / Byte.SIZE);
    }

    @Override
    public void addChar(int index, int length) throws FbException {
        metadataBuilder.setType(status, index, SQL_TEXT);
        metadataBuilder.setLength(status, index, length);
    }

    @Override
    public void addVarchar(int index, int length) throws FbException {
        metadataBuilder.setType(status, index, SQL_VARYING);
        metadataBuilder.setLength(status, index, length);
    }

    @Override
    public void addChar(int index, int length, int charSet) throws FbException {
        metadataBuilder.setType(status, index, SQL_TEXT);
        metadataBuilder.setLength(status, index, length);
        metadataBuilder.setCharSet(status, index, charSet);
    }

    @Override
    public void addVarchar(int index, int length, int charSet) throws FbException {
        metadataBuilder.setType(status, index, SQL_VARYING);
        metadataBuilder.setLength(status, index, length);
        metadataBuilder.setCharSet(status, index, charSet);
    }

    @Override
    public void addDecDecimal(int index, int size, int scale) throws FbException {
        metadataBuilder.setType(status, index, SQL_DEC_FIXED);
        metadataBuilder.setLength(status, index, size);
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_DECIMAL);
    }

    @Override
    public void addDecNumeric(int index, int size, int scale) throws FbException {
        metadataBuilder.setType(status, index, SQL_DEC_FIXED);
        metadataBuilder.setLength(status, index, size);
        metadataBuilder.setScale(status, index, scale);
        metadataBuilder.setSubType(status, index, SUBTYPE_NUMERIC);
    }
}
