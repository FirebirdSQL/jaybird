/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Bytes.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ByteWrapperHolder
   implements Holder
{
   public Byte value;

   public ByteWrapperHolder()
   {
      
   }      
   public ByteWrapperHolder(Byte value)
   {
      this.value = value;
   }
}
