/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for byte[]s.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ByteArrayHolder
   implements Holder
{
   public byte[] value;

   public ByteArrayHolder()
   {
      
   }      
   public ByteArrayHolder(byte[] value)
   {
      this.value = value;
   }
}
