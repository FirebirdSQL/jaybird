/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc.holders;

import java.util.Calendar;

/** A holder for Calendars.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public final class CalendarHolder
   implements Holder
{
   public Calendar value;

   public CalendarHolder()
   {
      
   }      
   public CalendarHolder(Calendar value)
   {
      this.value = value;
   }
}
