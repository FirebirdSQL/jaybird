package org.firebirdsql.gds.ng;

import java.sql.SQLException;

/**
 * Interface to access batch fields metadata
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbMessageMetadata {

    int getOffset(int index) throws SQLException;

    int getNullOffset(int index) throws SQLException;

    int getLength(int index) throws SQLException;

    String getAlias(int index) throws SQLException;

    String getField(int index) throws SQLException;

    String getOwner(int index) throws SQLException;

    String getRelation(int index) throws SQLException;

    int getAlignedLength() throws SQLException;

    int getAlignment() throws SQLException;

    int getCount() throws SQLException;

    int getCharSet(int index) throws SQLException;

    int getMessageLength() throws SQLException;

    int getScale(int index) throws SQLException;

    int getSubType(int index) throws SQLException;

    int getType(int index) throws SQLException;

    FbMetadataBuilder getBuilder() throws SQLException;
}
