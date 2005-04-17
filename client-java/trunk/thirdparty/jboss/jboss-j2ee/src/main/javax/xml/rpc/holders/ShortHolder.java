/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for shorts.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class ShortHolder
   implements Holder
{
   public short value;

   public ShortHolder()
   {
      
   }      
   public ShortHolder(short value)
   {
      this.value = value;
   }
}
