package org.firebirdsql.gds.ng;

import org.firebirdsql.nativeoo.gds.ng.FbException;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public interface FbMessageMetadata {

    int getOffset(int index) throws FbException;

    int getNullOffset(int index) throws FbException;

    int getLength(int index) throws FbException;

    String getAlias(int index) throws FbException;

    String getField(int index) throws FbException;

    String getOwner(int index) throws FbException;

    String getRelation(int index) throws FbException;

    int getAlignedLength() throws FbException;

    int getAlignment() throws FbException;

    int getCount() throws FbException;

    int getCharSet(int index) throws FbException;

    int getMessageLength() throws FbException;

    int getScale(int index) throws FbException;

    int getSubType(int index) throws FbException;

    int getType(int index) throws FbException;

    IMetadataBuilder getBuilder() throws FbException;
}
