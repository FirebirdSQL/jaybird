/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for ints.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class IntHolder
   implements Holder
{
   public int value;

   public IntHolder()
   {
      
   }      
   public IntHolder(int value)
   {
      this.value = value;
   }
}
