/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

import java.math.BigInteger;

/** A holder for BigIntegers.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class BigIntegerHolder
   implements Holder
{
   public BigInteger value;

   public BigIntegerHolder()
   {
      
   }      
   public BigIntegerHolder(BigInteger value)
   {
      this.value = value;
   }
}
