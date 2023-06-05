package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;
import org.firebirdsql.gds.ng.fields.RowDescriptor;
import org.firebirdsql.gds.ng.fields.RowValue;
import org.firebirdsql.gds.ng.listeners.ExceptionListener;
import org.firebirdsql.gds.ng.listeners.ExceptionListenerDispatcher;
import org.firebirdsql.jdbc.FBBlob;
import org.firebirdsql.jdbc.FBClob;
import org.firebirdsql.jdbc.FBDriverNotCapableException;
import org.firebirdsql.jdbc.SQLStateConstants;
import org.firebirdsql.jdbc.field.FBField;
import org.firebirdsql.jdbc.field.FieldDataProvider;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public abstract class AbstractFbBatch implements FbBatch {

    protected final ExceptionListenerDispatcher exceptionListenerDispatcher = new ExceptionListenerDispatcher(this);
    private final BatchParameterBuffer batchParameterBuffer;
    protected FbTransaction transaction;
    private FbDatabase database;

    private RowDescriptor rowDescriptor;
    private FBField[] fields = null;
    private RowValue fieldValues;

    protected AbstractFbBatch(FbDatabase database, BatchParameterBuffer batchParameterBuffer) {
        this.database = database;
        this.batchParameterBuffer = batchParameterBuffer;
    }

    @Override
    public void addExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.addListener(listener);
    }

    @Override
    public void removeExceptionListener(ExceptionListener listener) {
        exceptionListenerDispatcher.removeListener(listener);
    }

    public BatchParameterBuffer getBatchParameterBuffer() {
        return batchParameterBuffer;
    }

    @Override
    public FbTransaction getTransaction() {
        return transaction;
    }

    public void setTransaction(FbTransaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public FbDatabase getDatabase() {
        return database;
    }

    public void setDatabase(final FbDatabase database) {
        this.database = database;
    }

    protected RowValue getFieldValues() {
        return fieldValues;
    }

    /**
     * Creating a string descriptor from metadata.
     *
     * @throws SQLException For errors when preparing batch
     *
     */
    protected void prepareBatch() throws SQLException {

        if (getStatement() != null)
            rowDescriptor = getStatement().getParameterDescriptor();
        else
            rowDescriptor = createRowDescriptor();
        assert rowDescriptor != null : "RowDescriptor should not be null after prepare";

        int fieldCount = rowDescriptor.getCount();
        fieldValues = rowDescriptor.createDefaultFieldValues();
        fields = new FBField[fieldCount];

        for (int i = 0; i < fieldCount; i++) {
            final int fieldPosition = i;

            FieldDataProvider dataProvider = new FieldDataProvider() {
                public byte[] getFieldData() {
                    return fieldValues.getFieldData(fieldPosition);
                }

                public void setFieldData(byte[] data) {
                    fieldValues.setFieldData(fieldPosition, data);
                }
            };

            fields[i] = FBField.createField(getParameterDescriptor(i + 1),
                    dataProvider, null, false);
        }
    }

    protected RowDescriptor createRowDescriptor() throws SQLException {
        List<FieldDescriptor> fieldDescriptors = new ArrayList<>();
        FbMessageMetadata metadata = getMetadata();
        int count = metadata.getCount();
        for (int i = 0; i < count; i++) {
            fieldDescriptors.add(new FieldDescriptor(i,
                    getDatabase().getDatatypeCoder(),
                    metadata.getType(i),
                    metadata.getSubType(i),
                    metadata.getScale(i),
                    metadata.getLength(i),
                    metadata.getField(i),
                    null,
                    null,
                    null,
                    null));
        }
        return RowDescriptor.createRowDescriptor(fieldDescriptors.toArray(new FieldDescriptor[0]),
                getDatabase().getDatatypeCoder());
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        getField(parameterIndex).setNull();
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream, int length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream inputStream) throws SQLException {
        getField(parameterIndex).setBinaryStream(inputStream);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        getField(parameterIndex).setBytes(x);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        getField(parameterIndex).setBoolean(x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        getField(parameterIndex).setByte(x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        getField(parameterIndex).setDate(x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        getField(parameterIndex).setDouble(x);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        getField(parameterIndex).setFloat(x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        getField(parameterIndex).setInteger(x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        getField(parameterIndex).setLong(x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        getField(parameterIndex).setObject(x);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        getField(parameterIndex).setShort(x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        getField(parameterIndex).setString(x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        getField(parameterIndex).setTime(x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        getField(parameterIndex).setTimestamp(x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        getField(parameterIndex).setBigDecimal(x);
    }

    /**
     * Returns the {@link FieldDescriptor} of the specified parameter.
     *
     * @param columnIndex 1-based index of the parameter
     * @return Field descriptor
     */
    protected FieldDescriptor getParameterDescriptor(int columnIndex) throws SQLException {
        return rowDescriptor.getFieldDescriptor(columnIndex - 1);
    }

    /**
     * Factory method for the field access objects
     */
    protected FBField getField(int columnIndex) throws SQLException {
        if (columnIndex > fields.length) {
            throw new SQLException("Invalid column index: " + columnIndex,
                    SQLStateConstants.SQL_STATE_INVALID_DESC_FIELD_ID);
        }

        return fields[columnIndex - 1];
    }

    /**
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream, int)}.
     * </p>
     */
    public final void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

    /**
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream, long)}.
     * </p>
     */
    public final void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        setBinaryStream(parameterIndex, x, length);
    }

    /**
     * <p>
     * Implementation note: works identical to {@link #setBinaryStream(int, InputStream)}.
     * </p>
     */
    public final void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        setBinaryStream(parameterIndex, x);
    }

    /**
     * <p>
     * Jaybird does not support array types.
     * </p>
     */
    public void setURL(int parameterIndex, URL url) throws SQLException {
        throw new FBDriverNotCapableException("Type URL not supported");
    }

    /**
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(int, Reader, long)}.
     * </p>
     */
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        setCharacterStream(parameterIndex, value, length);
    }

    /**
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setCharacterStream(int, Reader)}.
     * </p>
     */
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        setCharacterStream(parameterIndex, value);
    }

    /**
     * <p>
     * Implementation note: This method behaves exactly the same as {@link #setString(int, String)}.
     * </p>
     */
    public void setNString(int parameterIndex, String value) throws SQLException {
        setString(parameterIndex, value);
    }

    public void clearParameters() throws SQLException {
        if (fieldValues != null) {
            fieldValues.reset();
        }
    }

    /**
     * <p>
     * Implementation note: ignores {@code scale} and {@code targetSqlType} and works as
     * {@link #setObject(int, Object)}.
     * </p>
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
        setObject(parameterIndex, x);
    }

    /**
     * <p>
     * Implementation note: ignores {@code targetSqlType} and works as {@link #setObject(int, Object)}.
     * </p>
     */
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        setObject(parameterIndex, x);
    }

    public void setRef(int i, Ref x) throws SQLException {
        throw new FBDriverNotCapableException("Type REF not supported");
    }

    protected void setBlob(int parameterIndex, Blob blob) throws SQLException {
        getField(parameterIndex).setBlob((FBBlob) blob);
    }

    protected void setClob(int parameterIndex, Clob clob) throws SQLException {
        getField(parameterIndex).setClob((FBClob) clob);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        getField(parameterIndex).setCharacterStream(reader);
    }

    public final LockCloseable withLock() {
        return database.withLock();
    }
}
