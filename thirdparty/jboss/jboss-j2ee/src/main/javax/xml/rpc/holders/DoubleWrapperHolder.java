/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Doubles.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class DoubleWrapperHolder
   implements Holder
{
   public Double value;

   public DoubleWrapperHolder()
   {
      
   }      
   public DoubleWrapperHolder(Double value)
   {
      this.value = value;
   }
}
