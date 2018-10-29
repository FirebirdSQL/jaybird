package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.FbMetadataBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IMessageMetadataImpl implements FbMessageMetadata {

    private IDatabaseImpl database;
    private IMetadataBuilderImpl metadataBuilderImpl;
    private IMetadataBuilder metadataBuilder;
    private IMessageMetadata metadata;

    public IMessageMetadataImpl(FbMetadataBuilder metadataBuilder) throws FbException {

        this.metadataBuilderImpl = (IMetadataBuilderImpl)metadataBuilder;
        this.database = (IDatabaseImpl)this.metadataBuilderImpl.getDatabase();

        this.metadataBuilder = this.metadataBuilderImpl.getMetadataBuilder();
        this.metadata = this.metadataBuilder.getMetadata(database.getStatus());
    }

    public IMessageMetadataImpl(IDatabaseImpl database, IMessageMetadata metadata) throws FbException {
        this.database = database;
        this.metadata = metadata;
    }

    public IMessageMetadata getMetadata() {
        return metadata;
    }

    public int getOffset(int index) throws FbException {
        return metadata.getOffset(database.getStatus(), index);
    }

    public int getNullOffset(int index) throws FbException {
        return metadata.getNullOffset(database.getStatus(), index);
    }

    public int getLength(int index) throws FbException {
        return metadata.getLength(database.getStatus(), index);
    }

    public String getAlias(int index) throws FbException {
        return metadata.getAlias(database.getStatus(), index);
    }

    public String getField(int index) throws FbException {
        return metadata.getField(database.getStatus(), index);
    }

    public String getOwner(int index) throws FbException {
        return metadata.getOwner(database.getStatus(), index);
    }

    public String getRelation(int index) throws FbException {
        return metadata.getRelation(database.getStatus(), index);
    }

    public int getAlignedLength() throws FbException {
        return metadata.getAlignedLength(database.getStatus());
    }

    public int getAlignment() throws FbException {
        return metadata.getAlignment(database.getStatus());
    }

    public int getCount() throws FbException {
        return metadata.getCount(database.getStatus());
    }

    public int getCharSet(int index) throws FbException {
        return metadata.getCharSet(database.getStatus(), index);
    }

    public int getMessageLength() throws FbException {
        return metadata.getMessageLength(database.getStatus());
    }

    public int getScale(int index) throws FbException {
        return metadata.getScale(database.getStatus(), index);
    }

    public int getSubType(int index) throws FbException {
        return metadata.getSubType(database.getStatus(), index);
    }

    public int getType(int index) throws FbException {
        return metadata.getType(database.getStatus(), index);
    }

    public IMetadataBuilder getBuilder() throws FbException {
        return metadata.getBuilder(database.getStatus());
    }

}
