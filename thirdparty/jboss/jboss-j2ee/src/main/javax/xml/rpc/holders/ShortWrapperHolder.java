/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Shorts.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ShortWrapperHolder
   implements Holder
{
   public Short value;

   public ShortWrapperHolder()
   {
      
   }      
   public ShortWrapperHolder(Short value)
   {
      this.value = value;
   }
}
