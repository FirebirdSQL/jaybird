package org.firebirdsql.gds.ng;

import org.firebirdsql.gds.BlobParameterBuffer;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public interface FbMessageBuilder {

    void addSmallint(int index, short value) throws SQLException;

    void addInteger(int index, int value) throws SQLException;

    void addBigint(int index, long value) throws SQLException;

    void addFloat(int index, float value) throws SQLException;

    void addDouble(int index, double value) throws SQLException;

    void addNumeric(int index, double value) throws SQLException;

    void addDecimal(int index, double value) throws SQLException;

    void addDecfloat16(int index, BigDecimal value) throws SQLException;

    void addDecfloat34(int index, BigDecimal value) throws SQLException;

    void addBlob(int index, long blobId) throws SQLException;

    void addBoolean(int index, boolean value) throws SQLException;

    void addDate(int index, Date value) throws SQLException;

    void addTime(int index, Time value) throws SQLException;

    void addTimestamp(int index, Timestamp value) throws SQLException;

    void addChar(int index, String value) throws SQLException;

    void addVarchar(int index, String value) throws SQLException;

    byte[] getData();

    void clear();

    void addStreamData(byte[] data) throws IOException;

    byte[] getStreamData();

    void clearStream();

    void addBlobData(byte[] data, long blobId) throws IOException;

    long addBlobHeader(long blobId, BlobParameterBuffer buffer) throws IOException;

    void addBlobSegment(byte[] data, long offset, boolean lastSegment) throws IOException;

    byte[] getBlobStreamData();

    void clearBlobStream();
}
