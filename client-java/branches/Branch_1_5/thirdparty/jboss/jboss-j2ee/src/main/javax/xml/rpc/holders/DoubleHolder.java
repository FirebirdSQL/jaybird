/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for doubles.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class DoubleHolder
   implements Holder
{
   public double value;

   public DoubleHolder()
   {
      
   }      
   public DoubleHolder(double value)
   {
      this.value = value;
   }
}
