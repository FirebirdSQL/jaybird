package org.firebirdsql.jgds;

import org.firebirdsql.gds.TransactionParameterBuffer;


/**
 * 
 */
public class TransactionParameterBufferImpl extends ParameterBufferBase
        implements TransactionParameterBuffer {

    /* (non-Javadoc)
     * @see org.firebirdsql.gds.TransactionParameterBuffer#deepCopy()
     */
    public TransactionParameterBuffer deepCopy() {
        TransactionParameterBufferImpl result = new TransactionParameterBufferImpl();
        
        result.getArgumentsList().addAll( this.getArgumentsList() );
        
        return result;
    }

}
