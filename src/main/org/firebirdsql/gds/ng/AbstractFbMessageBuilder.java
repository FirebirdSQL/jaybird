package org.firebirdsql.gds.ng;

import org.firebirdsql.encodings.EncodingFactory;
import org.firebirdsql.extern.decimal.Decimal128;
import org.firebirdsql.extern.decimal.Decimal64;
import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.FbBatch;
import org.firebirdsql.gds.ng.FbMessageBuilder;
import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.SeekableByteArrayOutputStream;
import org.firebirdsql.gds.ng.jna.LittleEndianDatatypeCoder;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import static org.firebirdsql.gds.ISCConstants.SQL_INT64;
import static org.firebirdsql.gds.ISCConstants.SQL_LONG;
import static org.firebirdsql.gds.ISCConstants.SQL_SHORT;

/**
 * Common implementation of {@link org.firebirdsql.gds.ng.FbMessageBuilder} to build firebird message
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public abstract class AbstractFbMessageBuilder<E extends FbBatch> implements FbMessageBuilder {

    private FbMessageMetadata metadata;
    private ByteBuffer buffer;
    private final LittleEndianDatatypeCoder datatypeCoder = new LittleEndianDatatypeCoder(EncodingFactory.createInstance(StandardCharsets.UTF_8));
    private final SeekableByteArrayOutputStream stream = new SeekableByteArrayOutputStream();
    private final SeekableByteArrayOutputStream blobStream = new SeekableByteArrayOutputStream();
    private int messageAlign;
    private int messageLength;
    private int blobAlign;
    private int segmentedBlobSize = 0;

    private int align(int target, int alignment) {
        return (((target) + alignment - 1) & ~(alignment - 1));
    }

    protected AbstractFbMessageBuilder(E batch) throws SQLException {
        this.metadata = batch.getMetadata();
        this.messageLength = metadata.getMessageLength();
        this.messageAlign = metadata.getAlignedLength();
        this.blobAlign = batch.getBlobAlignment();
        buffer = ByteBuffer.allocate(this.messageLength);
    }

    @Override
    public void addSmallint(int index, short value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeShort(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addInteger(int index, int value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeInt(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addBigint(int index, long value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeLong(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addFloat(int index, float value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeFloat(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addDouble(int index, double value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);
        int type = metadata.getType(index);
        byte[] bytes = null;
        if (type == SQL_INT64) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            BigInteger integer = decimal.unscaledValue();
            bytes = datatypeCoder.encodeLong(integer.longValue());
        }
        else
            bytes = datatypeCoder.encodeDouble(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addNumeric(int index, double value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);
        int type = metadata.getType(index);
        byte[] bytes = null;
        if (type == SQL_INT64) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            BigInteger integer = decimal.unscaledValue();
            bytes = datatypeCoder.encodeLong(integer.longValue());
        } else if (type == SQL_SHORT) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            short encodedShort = (short)decimal.unscaledValue().intValue();
            bytes = datatypeCoder.encodeShort(encodedShort);
        } else if (type == SQL_LONG) {
//            long encodedLong = Double.doubleToLongBits(value);
            BigDecimal decimal = BigDecimal.valueOf(value);
            long encodedLong = (short)decimal.unscaledValue().longValue();
            bytes = datatypeCoder.encodeLong(encodedLong);
        }
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addDecimal(int index, double value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);
        int type = metadata.getType(index);
        byte[] bytes = null;
        if (type == SQL_INT64) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            BigInteger integer = decimal.unscaledValue();
            bytes = datatypeCoder.encodeLong(integer.longValue());
        } else if (type == SQL_SHORT) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            short encodedShort = (short)decimal.unscaledValue().intValue();
            bytes = datatypeCoder.encodeShort(encodedShort);
        } else if (type == SQL_LONG) {
            BigDecimal decimal = BigDecimal.valueOf(value);
            long encodedLong = decimal.unscaledValue().longValue();
            bytes = datatypeCoder.encodeLong(encodedLong);
        }
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addDecfloat16(int index, BigDecimal value) throws SQLException {
        final Decimal64 decimal128 = Decimal64.valueOf(value);
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeDecimal64(decimal128);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addDecfloat34(int index, BigDecimal value) throws SQLException {
        final Decimal128 decimal128 = Decimal128.valueOf(value);
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeDecimal128(decimal128);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addBlob(int index, long blobId) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeLong(blobId);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addBoolean(int index, boolean value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeBoolean(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addDate(int index, Date value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeDate(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addTime(int index, Time value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeTime(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addTimestamp(int index, Timestamp value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeTimestamp(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addChar(int index, String value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeString(value);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public void addVarchar(int index, String value) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        byte[] bytes = datatypeCoder.encodeString(value);
        byte[] encodeShort = datatypeCoder.encodeShort(bytes.length);
        byte[] nullShort = datatypeCoder.encodeShort(0);

        buffer.position(offset);
        buffer.put(encodeShort);
        offset += encodeShort.length;
        buffer.position(offset);
        buffer.put(bytes);
        buffer.position(nullOffset);
        buffer.put(nullShort);
    }

    @Override
    public byte[] getData() {
        return buffer.array();
    }

    @Override
    public void clear() {
        buffer.clear();
    }

    @Override
    public void addStreamData(byte[] data) throws IOException {

        stream.write(data);

        int align = align(messageLength, messageAlign);

        if (align != 0) {
            byte[] shift = ByteBuffer.allocate(Math.abs(data.length - align)).array();
            stream.write(shift);
        }
    }

    @Override
    public void clearStream() {
        stream.reset();
    }

    @Override
    public byte[] getStreamData() {
       return stream.toByteArray();
    }

    @Override
    public void addBlobData(byte[] data, long blobId) throws IOException {
        long position = addBlobHeader(blobId, null);
        blobStream.write(data);

        long oldPosition = blobStream.getStreamPosition();
        blobStream.seek(position);

        blobStream.write(datatypeCoder.encodeInt(data.length));

        blobStream.seek(oldPosition);

        int align = align(blobStream.size(), blobAlign);

        if (align != 0 && blobStream.size() - align < 0) {
            byte[] shift = ByteBuffer.allocate(Math.abs(blobStream.size() - align)).array();
            blobStream.write(shift);
        }
    }

    @Override
    public long addBlobHeader(long blobId, BlobParameterBuffer buffer) throws IOException {

        int align = align(blobStream.size(), blobAlign);

        if (align != 0 && blobStream.size() - align < 0) {
            byte[] shift = ByteBuffer.allocate(Math.abs(blobStream.size() - align)).array();
            blobStream.write(shift);
        }

        blobStream.write(datatypeCoder.encodeLong(blobId));

        int rc = blobStream.size();
        segmentedBlobSize = rc;

        if (buffer != null) {
            byte[] bytes = buffer.toBytesWithType();
            byte[] bytesLength = datatypeCoder.encodeInt(bytes.length);
            blobStream.write(bytesLength);
            blobStream.write(bytesLength);
            blobStream.write(bytes);
        } else {
            byte[] bytesLength = datatypeCoder.encodeInt(0);
            blobStream.write(bytesLength);
            blobStream.write(bytesLength);
        }

        return rc;
    }

    @Override
    public void addBlobSegment(byte[] data, long offset, boolean lastSegment) throws IOException {
        int align = align(blobStream.size(), FbBatch.BLOB_SEGHDR_ALIGN);
        if (align != 0 && blobStream.size() - align < 0) {
            byte[] shift = ByteBuffer.allocate(Math.abs(blobStream.size() - align)).array();
            blobStream.write(shift);
        }
        long oldPosition = blobStream.getStreamPosition();
        blobStream.seek(offset);

        byte[] dataLength = datatypeCoder.encodeShort(data.length);
        segmentedBlobSize += align(data.length + dataLength.length, FbBatch.BLOB_SEGHDR_ALIGN);
        blobStream.write(datatypeCoder.encodeShort(segmentedBlobSize));

        blobStream.seek(oldPosition);

        blobStream.write(dataLength);
        blobStream.write(data);

        // If the last blob segment is added,
        // then blob stream must be aligned using blobAlign
        if (lastSegment) {
            align = align(blobStream.size(), blobAlign);

            if (align != 0 && blobStream.size() - align < 0) {
                byte[] shift = ByteBuffer.allocate(Math.abs(blobStream.size() - align)).array();
                blobStream.write(shift);
            }
        }
    }

    @Override
    public void clearBlobStream() {
        segmentedBlobSize = 0;
        blobStream.reset();
    }

    @Override
    public byte[] getBlobStreamData() {
        return blobStream.toByteArray();
    }
}
