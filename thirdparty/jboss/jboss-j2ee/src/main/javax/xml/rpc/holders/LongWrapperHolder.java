/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Longs.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class LongWrapperHolder
   implements Holder
{
   public Long value;

   public LongWrapperHolder()
   {
      
   }      
   public LongWrapperHolder(Long value)
   {
      this.value = value;
   }
}
