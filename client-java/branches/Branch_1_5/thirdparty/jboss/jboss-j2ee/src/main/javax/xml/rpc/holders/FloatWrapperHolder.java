/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

/** A holder for Floats.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class FloatWrapperHolder
   implements Holder
{
   public Float value;

   public FloatWrapperHolder()
   {
      
   }      
   public FloatWrapperHolder(Float value)
   {
      this.value = value;
   }
}
