/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for longs.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class LongHolder
   implements Holder
{
   public long value;

   public LongHolder()
   {
      
   }      
   public LongHolder(long value)
   {
      this.value = value;
   }
}
