package org.firebirdsql.gds.impl.jni;

import java.io.ByteArrayOutputStream;

import org.firebirdsql.gds.ISCConstants;
import org.firebirdsql.gds.TransactionParameterBuffer;

/**
 * 
 */
public class TransactionParameterBufferImpl extends ParameterBufferBase
        implements TransactionParameterBuffer {

    /*
     * (non-Javadoc)
     * 
     * @see org.firebirdsql.gds.TransactionParameterBuffer#deepCopy()
     */
    public TransactionParameterBuffer deepCopy() {
        TransactionParameterBufferImpl result = new TransactionParameterBufferImpl();

        result.getArgumentsList().addAll(this.getArgumentsList());

        return result;
    }

    /**
     * Pacakage local method for obtaining buffer suitable for passing to native
     * method.
     * 
     * @return
     */
    byte[] getBytesForNativeCode() {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        byteArrayOutputStream.write(ISCConstants.isc_tpb_version3);

        super.writeArgumentsTo(byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }
}
