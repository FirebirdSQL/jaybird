/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A representation of a node whose value is text. A Text object may represent
 * text that is content or text that is a comment.
 *  
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface Text extends javax.xml.soap.Node, org.w3c.dom.Text
{

   /** Retrieves whether this Text object represents a comment.
    *
    * @return true if this Text object is a comment; false otherwise
    */
   public boolean isComment();
}
