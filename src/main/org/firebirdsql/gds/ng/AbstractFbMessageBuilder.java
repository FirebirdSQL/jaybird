package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Common implementation of {@link org.firebirdsql.gds.ng.FbMessageBuilder} to build firebird message
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public abstract class AbstractFbMessageBuilder<E extends FbBatch> implements FbMessageBuilder {

    private final FbMessageMetadata metadata;
    private final FbStatement statement;
    private final ByteBuffer buffer;
    private final SeekableByteArrayOutputStream stream = new SeekableByteArrayOutputStream();
    private final SeekableByteArrayOutputStream blobStream = new SeekableByteArrayOutputStream();
    private final int messageAlign;
    private final int messageLength;
    private final int blobAlign;
    private final byte[] nulls = new byte[] {0, 0};
    private int segmentedBlobSize = 0;
    private int segmentedBlobOffset = 0;

    private int align(int target, int alignment) {
        return (((target) + alignment - 1) & ~(alignment - 1));
    }

    protected AbstractFbMessageBuilder(E batch) throws SQLException {
        this.metadata = batch.getMetadata();
        this.statement = batch.getStatement();
        this.messageLength = metadata.getMessageLength();
        this.messageAlign = metadata.getAlignedLength();
        this.blobAlign = batch.getBlobAlignment();
        buffer = ByteBuffer.allocate(this.messageLength);
    }

    @Override
    public void addData(int index, byte[] data, FieldDescriptor parameterDescriptor) throws SQLException {
        int nullOffset = metadata.getNullOffset(index);
        int offset = metadata.getOffset(index);

        if (parameterDescriptor.isVarying()) {
            byte[] dataLen;
            if (data == null)
                dataLen = parameterDescriptor.getDatatypeCoder().encodeShort(0);
            else
                dataLen = parameterDescriptor.getDatatypeCoder().encodeShort(data.length);
            buffer.position(offset);
            buffer.put(dataLen);
            offset += dataLen.length;
        }

        buffer.position(offset);
        if (data == null) {
            buffer.position(nullOffset);
            buffer.put(parameterDescriptor.getDatatypeCoder().encodeShort(1));
        } else {
            buffer.put(data);
            buffer.position(nullOffset);
            buffer.put(nulls);
        }
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

        blobStream.write(statement.getDatabase().getDatatypeCoder().encodeInt(data.length));

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

        blobStream.write(statement.getDatabase().getDatatypeCoder().encodeLong(blobId));

        segmentedBlobOffset = blobStream.size();
        segmentedBlobSize = segmentedBlobOffset;

        if (buffer != null) {
            byte[] bytes = buffer.toBytesWithType();
            byte[] bytesLength = statement.getDatabase().getDatatypeCoder().encodeInt(bytes.length);
            blobStream.write(bytesLength);
            blobStream.write(bytesLength);
            blobStream.write(bytes);
        } else {
            byte[] bytesLength = statement.getDatabase().getDatatypeCoder().encodeInt(0);
            blobStream.write(bytesLength);
            blobStream.write(bytesLength);
        }

        return segmentedBlobOffset;
    }

    @Override
    public void addBlobSegment(byte[] data, boolean lastSegment) throws IOException {
        int align = align(blobStream.size(), FbBatch.BLOB_SEGHDR_ALIGN);
        if (align != 0 && blobStream.size() - align < 0) {
            byte[] shift = ByteBuffer.allocate(Math.abs(blobStream.size() - align)).array();
            blobStream.write(shift);
        }
        long oldPosition = blobStream.getStreamPosition();
        blobStream.seek(segmentedBlobOffset);

        byte[] dataLength = statement.getDatabase().getDatatypeCoder().encodeShort(data.length);
        segmentedBlobSize += align(data.length + dataLength.length, FbBatch.BLOB_SEGHDR_ALIGN);
        blobStream.write(statement.getDatabase().getDatatypeCoder().encodeShort(segmentedBlobSize));

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
