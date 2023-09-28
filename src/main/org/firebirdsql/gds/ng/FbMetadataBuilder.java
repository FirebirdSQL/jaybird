package org.firebirdsql.gds.ng;

import java.sql.SQLException;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbMetadataBuilder {

    FbMessageMetadata getMessageMetadata() throws SQLException;

    int addField() throws SQLException;

    void addSmallint(int index) throws SQLException;

    void addInteger(int index) throws SQLException;

    void addBigint(int index) throws SQLException;

    void addFloat(int index) throws SQLException;

    void addNumeric(int index, int size, int scale) throws SQLException;

    void addDecimal(int index, int size, int scale) throws SQLException;

    void addDouble(int index) throws SQLException;

    void addDecfloat16(int index) throws SQLException;

    void addDecfloat34(int index) throws SQLException;

    void addBlob(int index)  throws SQLException;

    void addBlob(int index, int subtype)  throws SQLException;

    void addBoolean(int index)  throws SQLException;

    void addDate(int index)  throws SQLException;

    void addTime(int index)  throws SQLException;

    void addTimestamp(int index)  throws SQLException;

    void addChar(int index, int length)  throws SQLException;

    void addVarchar(int index, int length)  throws SQLException;

    void addChar(int index, int length, int charSet)  throws SQLException;

    void addVarchar(int index, int length, int charSet)  throws SQLException;

    void addDecDecimal(int index, int size, int scale)  throws SQLException;

    void addDecNumeric(int index, int size, int scale)  throws SQLException;
}
