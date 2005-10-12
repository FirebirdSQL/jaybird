/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for bytes.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ByteHolder
   implements Holder
{
   public byte value;

   public ByteHolder()
   {
      
   }      
   public ByteHolder(byte value)
   {
      this.value = value;
   }
}
