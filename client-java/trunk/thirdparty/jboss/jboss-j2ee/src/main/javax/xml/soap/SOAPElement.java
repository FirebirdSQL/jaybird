/*
 * JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.soap;

import java.util.Iterator;

import org.w3c.dom.Element;

/** An object representing an element of a SOAP message that is allowed but not
 * specifically prescribed by a SOAP specification. This interface serves as the
 * base interface for those objects that are specifically prescribed by a SOAP
 * specification.
 * 
 * Methods in this interface that are required to return SAAJ specific objects
 * may "silently" replace nodes in the tree as required to successfully return
 * objects of the correct type. See getChildElements() and javax.xml.soap for
 * details.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface SOAPElement extends Node, Element
{
   /** Adds an attribute with the specified name and value to this SOAPElement object.
    *
    * @param name a Name object with the name of the attribute
    * @param value a String giving the value of the attribute
    * @return the SOAPElement object into which the attribute was inserted
    * @throws SOAPException if there is an error in creating the Attribute
    */
   public abstract SOAPElement addAttribute(Name name, String value) throws SOAPException;

   /** Creates a new SOAPElement object initialized with the specified local name and adds the new element to this SOAPElement object.
    *
    * @param name  a String giving the local name for the element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement addChildElement(String name) throws SOAPException;

   /** Creates a new SOAPElement object initialized with the specified local name and prefix and adds the new element to this SOAPElement object.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix a String giving the namespace prefix for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement addChildElement(String localName, String prefix) throws SOAPException;

   /** Creates a new SOAPElement object initialized with the specified local name, prefix, and URI and adds the new element to this SOAPElement object.
    *
    * @param localName a String giving the local name for the new element
    * @param prefix a String giving the namespace prefix for the new element
    * @param uri a String giving the URI of the namespace to which the new element belongs
    * @return the new SOAPElement object that was created
    * @throws SOAPException  if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement addChildElement(String localName, String prefix, String uri) throws SOAPException;

   /** Creates a new SOAPElement object initialized with the given Name object and adds the new element to this SOAPElement object.
    *
    * @param name a Name object with the XML name for the new element
    * @return the new SOAPElement object that was created
    * @throws SOAPException if there is an error in creating the SOAPElement object
    */
   public abstract SOAPElement addChildElement(Name name) throws SOAPException;

   /** Add a SOAPElement as a child of this SOAPElement instance. The SOAPElement is expected to be created by a SOAPElementFactory.
    *
    * Callers should not rely on the element instance being added as is into the XML tree.
    * Implementations could end up copying the content of the SOAPElement passed into an instance of a different SOAPElement
    * implementation. For instance if addChildElement() is called on a SOAPHeader, element will be copied into an instance of a SOAPHeaderElement.
    *
    * The fragment rooted in element is either added as a whole or not at all, if there was an error.
    *
    * The fragment rooted in element cannot contain elements named "Envelope", "Header" or "Body" and in the SOAP namespace.
    * Any namespace prefixes present in the fragment should be fully resolved using appropriate namespace declarations within the fragment itself.
    *
    * @param child the SOAPElement to be added as a new child
    * @return an instance representing the new SOAP element that was actually added to the tree.
    * @throws SOAPException if there was an error in adding this element as a child
    */
   public abstract SOAPElement addChildElement(SOAPElement child) throws SOAPException;

   /** Adds a namespace declaration with the specified prefix and URI to this SOAPElement object.
    *
    * @param prefix a String giving the prefix of the namespace
    * @param uri a String giving the uri of the namespace
    * @return the SOAPElement object into which this namespace declaration was inserted.
    * @throws SOAPException if there is an error in creating the namespace
    */
   public abstract SOAPElement addNamespaceDeclaration(String prefix, String uri) throws SOAPException;

   /** Creates a new Text object initialized with the given String and adds it to this SOAPElement object.
    *
    * @param text a String object with the textual content to be added
    * @return the SOAPElement object into which the new Text object was inserted
    * @throws SOAPException if there is an error in creating the new Text object
    */
   public abstract SOAPElement addTextNode(String text) throws SOAPException;

   /** Returns an Iterator over all of the attribute Name objects in this SOAPElement object.
    *
    * The iterator can be used to get the attribute names, which can then be passed to the method getAttributeValue to
    * retrieve the value of each attribute.
    *
    * @return an iterator over the names of the attributes
    */
   public abstract Iterator getAllAttributes();

   /** Returns the value of the attribute with the specified name.
    *
    * @param name a Name object with the name of the attribute
    * @return a String giving the value of the specified attribute
    */
   public abstract String getAttributeValue(Name name);

   /** Returns an Iterator over all the immediate child Nodes of this element.
    *
    * This includes javax.xml.soap.Text objects as well as SOAPElement objects.
    * Calling this method may cause child Element, SOAPElement and org.w3c.dom.Text nodes to be replaced by SOAPElement,
    * SOAPHeaderElement, SOAPBodyElement or javax.xml.soap.Text nodes as appropriate for the type of this parent node.
    * As a result the calling application must treat any existing references to these child nodes that have been obtained
    * through DOM APIs as invalid and either discard them or refresh them with the values returned by this Iterator.
    * This behavior can be avoided by calling the equivalent DOM APIs. See javax.xml.soap for more details.
    *
    * @return an iterator with the content of this SOAPElement object
    */
   public abstract Iterator getChildElements();

   /** Returns an Iterator over all the immediate child Nodes of this element with the specified name.
    *
    * All of these children will be SOAPElement nodes.
    * Calling this method may cause child Element, SOAPElement and org.w3c.dom.Text nodes to be replaced by SOAPElement,
    * SOAPHeaderElement, SOAPBodyElement or javax.xml.soap.Text nodes as appropriate for the type of this parent node.
    * As a result the calling application must treat any existing references to these child nodes that have been obtained
    * through DOM APIs as invalid and either discard them or refresh them with the values returned by this Iterator.
    * This behavior can be avoided by calling the equivalent DOM APIs. See javax.xml.soap for more details.
    *
    * @param name a Name object with the name of the child elements to be returned
    * @return an Iterator object over all the elements in this SOAPElement object with the specified name
    */
   public abstract Iterator getChildElements(Name name);

   /** Returns the name of this SOAPElement object.
    *
    * @return a Name object with the name of this SOAPElement object
    */
   public abstract Name getElementName();

   /** Returns the encoding style for this SOAPElement object.
    *
    * @return a String giving the encoding style
    */
   public abstract String getEncodingStyle();

   /** Returns an Iterator over the namespace prefix Strings declared by this element.
    *
    * The prefixes returned by this iterator can be passed to the method getNamespaceURI to retrieve the URI of each namespace.
    *
    * @return an iterator over the namespace prefixes in this SOAPElement object
    */
   public abstract Iterator getNamespacePrefixes();

   /** Returns the URI of the namespace that has the given prefix.
    *
    * @param prefix a String giving the prefix of the namespace for which to search
    * @return a String with the uri of the namespace that has the given prefix
    */
   public abstract String getNamespaceURI(String prefix);

   /** Returns an Iterator over the namespace prefix Strings visible to this element.
    *
    * The prefixes returned by this iterator can be passed to the method getNamespaceURI to retrieve the URI of each namespace.
    *
    * @return an iterator over the namespace prefixes are within scope of this SOAPElement object
    */
   public abstract Iterator getVisibleNamespacePrefixes();

   /** Removes the attribute with the specified name.
    *
    * @param name the Name object with the name of the attribute to be removed
    * @return true if the attribute was removed successfully; false if it was not
    */
   public abstract boolean removeAttribute(Name name);

   /** Detaches all children of this SOAPElement.
    *
    * This method is useful for rolling back the construction of partially completed SOAPHeaders and SOAPBodys in
    * preparation for sending a fault when an error condition is detected.
    * It is also useful for recycling portions of a document within a SOAP message.
    */
   public abstract void removeContents();

   /** Removes the namespace declaration corresponding to the given prefix.
    *
    * @param prefix a String giving the prefix for which to search
    * @return true if the namespace declaration was removed successfully; false if it was not
    */
   public abstract boolean removeNamespaceDeclaration(String prefix);

   /** Sets the encoding style for this SOAPElement object to one specified.
    *
    * @param encodingStyle  a String giving the encoding style
    * @throws SOAPException  if there was a problem in the encoding style being set.
    */
   public abstract void setEncodingStyle(String encodingStyle) throws SOAPException;

}
