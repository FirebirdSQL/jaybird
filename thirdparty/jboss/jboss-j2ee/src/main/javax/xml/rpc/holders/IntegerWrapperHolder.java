/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Integers.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class IntegerWrapperHolder
   implements Holder
{
   public Integer value;

   public IntegerWrapperHolder()
   {
      
   }      
   public IntegerWrapperHolder(Integer value)
   {
      this.value = value;
   }
}
