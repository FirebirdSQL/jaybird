package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;
import org.firebirdsql.gds.ng.fields.FieldDescriptor;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Interface to building row message for {@link FbBatch}.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbMessageBuilder {

    /**
     * Add field data to message.
     *
     * @param index of field in statement.
     * @param data of field
     * @param parameterDescriptor field descriptor
     * @throws SQLException
     */
    void addData(int index, byte[] data, FieldDescriptor parameterDescriptor) throws SQLException;

    /**
     *
     * @return batch message.
     */
    byte[] getData();

    /**
     * Clear batch.
     */
    void clear();

    /**
     * Add blob stream to batch.
     * @param data stream data.
     * @throws IOException
     */
    void addStreamData(byte[] data) throws IOException;

    /**
     * Get stream bytes.
     * @return
     */
    byte[] getStreamData();

    /**
     * Clear stream data.
     */
    void clearStream();

    /**
     * Add data for blob with id.
     * @param data blob data.
     * @param blobId blob id.
     * @throws IOException
     */
    void addBlobData(byte[] data, long blobId) throws IOException;

    /**
     * Add blob header to message if blob is segmented.
     * @param blobId blob id.
     * @param buffer blob parameters.
     * @return
     * @throws IOException
     */
    long addBlobHeader(long blobId, BlobParameterBuffer buffer) throws IOException;

    /**
     * Add segmented data to segmented blob.
     * @param data blob data.
     * @param lastSegment indicates whether the added segment is the last portion of the blob.
     * @throws IOException
     */
    void addBlobSegment(byte[] data, boolean lastSegment) throws IOException;

    /**
     *
     * @return blob stream bytes.
     */
    byte[] getBlobStreamData();

    /**
     * Clear blob stream data.
     */
    void clearBlobStream();
}
