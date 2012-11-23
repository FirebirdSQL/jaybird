package org.firebirdsql.gds.impl.wire;

/**
 * Implements JDK1.4+ specific socket creation.
 * 
 * This is necessary because of a bug found in the JDK1.4 and is / will not be
 * fixed until JDK7.0.
 * 
 * See bug details: <a
 * href='http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092063'>
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5092063 </a>
 * 
 * @author <a href="mailto:sjardine@users.sourceforge.net">Steve Jardine </a>
 */
public class JavaGDSImpl extends AbstractJavaGDSImpl {

}
