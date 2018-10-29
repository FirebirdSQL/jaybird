package org.firebirdsql.gds.impl;

import org.firebirdsql.gds.BatchParameterBuffer;
import org.firebirdsql.gds.ParameterBuffer;
import org.firebirdsql.gds.impl.argument.ArgumentType;
import org.firebirdsql.nativeoo.gds.ng.FbInterface;

/**
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 * @since 4.0
 */
public class BatchParameterBufferImpl extends ParameterBufferBase implements BatchParameterBuffer {

    public BatchParameterBufferImpl() {
        super(BatchMetaData.BATCH_VERSION_1);
    }

    private enum BatchMetaData implements ParameterBufferMetaData {

        BATCH_VERSION_1(FbInterface.IBatch.VERSION1);

        private final int batchVersion;

        BatchMetaData(int batchVersion) {
            this.batchVersion = batchVersion;
        }

        @Override
        public final int getType() {
            return batchVersion;
        }

        @Override
        public final void addPreamble(ParameterBuffer parameterBuffer) {
            // Do nothing
        }

        @Override
        public final ArgumentType getStringArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }

        @Override
        public final ArgumentType getByteArrayArgumentType(int tag) {
            return ArgumentType.TraditionalDpb;
        }

        @Override
        public final ArgumentType getIntegerArgumentType(int tag) {
            return ArgumentType.Wide;
        }
    }
}