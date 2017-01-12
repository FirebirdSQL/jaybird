package org.firebirdsql.pool;

import org.firebirdsql.gds.impl.GDSType;

/**
 * @deprecated This class will be removed in Jaybird 3.1; use {@link org.firebirdsql.ds.FBSimpleDataSource}
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
