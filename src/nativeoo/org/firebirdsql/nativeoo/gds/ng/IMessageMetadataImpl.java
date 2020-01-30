package org.firebirdsql.nativeoo.gds.ng;

import org.firebirdsql.gds.ng.FbMessageMetadata;
import org.firebirdsql.gds.ng.FbMetadataBuilder;
import org.firebirdsql.nativeoo.gds.ng.FbInterface.*;

import java.sql.SQLException;

/**
 * Implementation of {@link FbMessageMetadata} for native OO API.
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class IMessageMetadataImpl implements FbMessageMetadata {

    private final IDatabaseImpl database;
    private IMetadataBuilderImpl metadataBuilderImpl;
    private final IMetadataBuilder metadataBuilder;
    private final IMessageMetadata metadata;
    private final IStatus status;

    public IMessageMetadataImpl(FbMetadataBuilder metadataBuilder) throws SQLException {
        this.metadataBuilderImpl = (IMetadataBuilderImpl)metadataBuilder;
        this.database = (IDatabaseImpl)this.metadataBuilderImpl.getDatabase();
        this.metadataBuilder = this.metadataBuilderImpl.getMetadataBuilder();
        this.status = this.database.getStatus();
        this.metadata = this.metadataBuilder.getMetadata(getStatus());
        processStatus();
    }

    public IMessageMetadataImpl(IDatabaseImpl database, IMessageMetadata metadata) throws SQLException {
        this.database = database;
        this.metadata = metadata;
        this.status = this.database.getStatus();
        metadataBuilderImpl = null;
        metadataBuilder = null;
    }

    public IMessageMetadata getMetadata() {
        return metadata;
    }

    public int getOffset(int index) throws SQLException {
        int result = metadata.getOffset(getStatus(), index);
        processStatus();
        return result;
    }

    public int getNullOffset(int index) throws SQLException {
        int result = metadata.getNullOffset(getStatus(), index);
        processStatus();
        return result;
    }

    public int getLength(int index) throws SQLException {
        int result = metadata.getLength(getStatus(), index);
        processStatus();
        return result;
    }

    public String getAlias(int index) throws SQLException {
        String result = metadata.getAlias(getStatus(), index);
        processStatus();
        return result;
    }

    public String getField(int index) throws SQLException {
        String result = metadata.getField(getStatus(), index);
        processStatus();
        return result;
    }

    public String getOwner(int index) throws SQLException {
        String result = metadata.getOwner(getStatus(), index);
        processStatus();
        return result;
    }

    public String getRelation(int index) throws SQLException {
        String result = metadata.getRelation(getStatus(), index);
        processStatus();
        return result;
    }

    public int getAlignedLength() throws SQLException {
        int result = metadata.getAlignedLength(getStatus());
        processStatus();
        return result;
    }

    public int getAlignment() throws SQLException {
        int result = metadata.getAlignment(getStatus());
        processStatus();
        return result;
    }

    public int getCount() throws SQLException {
        int result = metadata.getCount(getStatus());
        processStatus();
        return result;
    }

    public int getCharSet(int index) throws SQLException {
        int result = metadata.getCharSet(getStatus(), index);
        processStatus();
        return result;
    }

    public int getMessageLength() throws SQLException {
        int result = metadata.getMessageLength(getStatus());
        processStatus();
        return result;
    }

    public int getScale(int index) throws SQLException {
        int result = metadata.getScale(getStatus(), index);
        processStatus();
        return result;
    }

    public int getSubType(int index) throws SQLException {
        int result = metadata.getSubType(getStatus(), index);
        processStatus();
        return result;
    }

    public int getType(int index) throws SQLException {
        int result = metadata.getType(getStatus(), index);
        processStatus();
        return result;
    }

    public FbMetadataBuilder getBuilder() throws SQLException {
        if (metadataBuilderImpl == null)
            metadataBuilderImpl = new IMetadataBuilderImpl(this.database, metadata.getCount(getStatus()));
        processStatus();
        return metadataBuilderImpl;
    }

    private IStatus getStatus() {
        status.init();
        return status;
    }

    private IDatabaseImpl getDatabase() {
        return database;
    }

    private void processStatus() throws SQLException {
        getDatabase().processStatus(status, null);
    }
}
