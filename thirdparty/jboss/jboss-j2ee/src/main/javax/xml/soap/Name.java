/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

/** A representation of an XML name. This interface provides methods for
 getting the local and namespace-qualified names and also for getting the
 prefix associated with the namespace for the name. It is also possible to get
 the URI of the namespace.
 
 The following is an example of a namespace declaration in an element.
 
 <wombat:GetLastTradePrice xmlns:wombat="http://www.wombat.org/trader">
 
 ("xmlns" stands for "XML namespace".) The following shows what the methods in
 the Name interface will return. 
 
 getQualifiedName will return "prefix:LocalName" = "WOMBAT:GetLastTradePrice" 
 getURI will return "http://www.wombat.org/trader" 
 getLocalName will return "GetLastTracePrice" 
 getPrefix will return "WOMBAT" 
 XML namespaces are used to disambiguate SOAP identifiers from
 application-specific identifiers. 
 Name objects are created using the method SOAPEnvelope.createName, which has
 two versions. One method creates Name objects with a local name, a namespace
 prefix, and a namespace URI. and the second creates Name objects with just a
 local name. The following line of code, in which se is a SOAPEnvelope object,
 creates a new Name object with all three.
 
 Name name = se.createName("GetLastTradePrice", "WOMBAT",
 "http://www.wombat.org/trader");
 
 The following line of code gives an example of how a Name object can be used.
 The variable element is a SOAPElement object. This code creates a new
 SOAPElement object with the given name and adds it to element. 
 
 element.addChildElement(name);

 @author Scott.Stark@jboss.org
 @version $Revision$
 */
public interface Name
{
   /** Gets the local name part of the XML name that this Name object represents.
    *
    * @return a string giving the local name
    */
   public String getLocalName();


   /** Returns the prefix that was specified when this Name object was initialized.
    * This prefix is associated with the namespace for the XML name that this Name object represents.
    *
    * @return the prefix as a string
    */
   public String getPrefix();

   /** Gets the namespace-qualified name of the XML name that this Name object represents.
    *
    * @return the namespace-qualified name as a string
    */
   public String getQualifiedName();

   /** Returns the URI of the namespace for the XML name that this Name object represents.
    *
    * @return the URI as a string
    */
   public String getURI();
}
