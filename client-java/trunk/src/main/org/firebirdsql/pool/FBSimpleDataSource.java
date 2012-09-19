package org.firebirdsql.pool;

import org.firebirdsql.gds.impl.GDSType;

/**
 * @deprecated Use {@link org.firebirdsql.dl.FBSimpleDataSource}
 */
@Deprecated
public class FBSimpleDataSource extends org.firebirdsql.ds.FBSimpleDataSource {
    
    private static final long serialVersionUID = 3156578540634970427L;

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource() {
        super();
    }

    /**
     * Create instance of this class.
     */
    public FBSimpleDataSource(GDSType type) {
        super(type);
    }
}
