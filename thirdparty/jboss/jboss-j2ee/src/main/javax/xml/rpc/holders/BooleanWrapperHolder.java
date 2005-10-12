/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Booleans.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class BooleanWrapperHolder
   implements Holder
{
   public Boolean value;

   public BooleanWrapperHolder()
   {
      
   }      
   public BooleanWrapperHolder(Boolean value)
   {
      this.value = value;
   }
}
