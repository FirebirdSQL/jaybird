/* JBoss, the OpenSource WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package javax.xml.rpc;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** The javax.xml.rpc.Call interface provides support for the dynamic invocation of a service endpoint.
 * The javax.xml.rpc.Service interface acts as a factory for the creation of Call instances.
 *
 * Once a Call instance is created, various setter and getter methods may be used to configure this Call instance.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision$
 */
public interface Call
{
   /** Standard property for encoding Style: Encoding style specified as a namespace URI. */
   public static final String ENCODINGSTYLE_URI_PROPERTY = "javax.xml.rpc.encodingstyle.namespace.uri";
   /** Standard property for operation style. */
   public static final String OPERATION_STYLE_PROPERTY = "javax.xml.rpc.soap.operation.style";
   /** Standard property: Password for authentication Type: java.lang.String */
   public static final String PASSWORD_PROPERTY = "javax.xml.rpc.security.auth.password";
   /** Standard property: This boolean property is used by a service client to indicate whether or not it wants to participate in a session with a service endpoint. */
   public static final String SESSION_MAINTAIN_PROPERTY = "javax.xml.rpc.session.maintain";
   /** Standard property for SOAPAction. */
   public static final String SOAPACTION_URI_PROPERTY = "javax.xml.rpc.soap.http.soapaction.uri";
   /** Standard property for SOAPAction. */
   public static final String SOAPACTION_USE_PROPERTY = "javax.xml.rpc.soap.http.soapaction.use";
   /** Standard property: User name for authentication Type: java.lang.String */
   public static final String USERNAME_PROPERTY = "javax.xml.rpc.security.auth.username";

   /** Removes all specified parameters from this Call instance. Note that this method removes only the parameters and
    * not the return type. The setReturnType(null) is used to remove the return type.
    * @throws JAXRPCException This exception may be thrown If this method is called when the method isParameterAndReturnSpecRequired returns false for this Call's operation.
    */
   public void removeAllParameters();

   /** Invokes a remote method using the one-way interaction mode.
    * The client thread does not normally block waiting for the completion of the server processing for this
    * remote method invocation. When the protocol in use is SOAP/HTTP, this method should block until an HTTP response
    * code has been received or an error occurs. This method must not throw any remote exceptions.
    * This method may throw a JAXRPCException during the processing of the one-way remote call.
    *
    * @param inputParams Object[]--Parameters for this invocation. This includes only the input params.
    * @throws JAXRPCException if there is an error in the configuration of the Call object
    * (example: a non-void return type has been incorrectly specified for the one-way call) or
    * if there is any error during the invocation of the one-way remote call
    */
   public void invokeOneWay(Object[] inputParams);

   /** Gets the address of a target service endpoint.
    * @return Address of the target service endpoint as an URI
    */
   public String getTargetEndpointAddress();

   /** Removes a named property.
    * @param name Name of the property
    * @throws JAXRPCException if an invalid or unsupported property name is passed.
    */
   public void removeProperty(String name);

   /** Sets the address of the target service endpoint. This address must correspond to the transport
    * specified in the binding for this Call instance.
    *
    * @param address Address of the target service endpoint; specified as an URI
    */
   public void setTargetEndpointAddress(String address);

   /** Gets the names of configurable properties supported by this Call object.
    * @return Iterator for the property names
    */
   public Iterator getPropertyNames();

   /** Returns a List values for the output parameters of the last invoked operation.
    *
    * @return java.util.List Values for the output parameters. An empty List is returned if there are no output values.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public List getOutputValues();

   /** Returns a Map of {name, value} for the output parameters of the last invoked operation.
    *  The parameter names in the returned Map are of type java.lang.String.
    *
    * @return Map Output parameters for the last Call.invoke(). Empty Map is returned if there are no output parameters.
    * @throws JAXRPCException If this method is invoked for a one-way operation or is invoked before any invoke method has been called.
    */
   public Map getOutputParams();

   /** Gets the name of the operation to be invoked using this Call instance.
    * @return Qualified name of the operation
    */
   public QName getOperationName();

   /** Gets the qualified name of the port type.
    * @return Qualified name of the port type
    */
   public QName getPortTypeName();

   /** Gets the return type for a specific operation
    *
    * @return Returns the XML type for the return value
    */
   public QName getReturnType();

   /** Sets the name of the operation to be invoked using this Call instance.
    *
    * @param operationName QName of the operation to be invoked using the Call instance
    */
   public void setOperationName(QName operationName);

   /** Sets the qualified name of the port type.
    *
    * @param portType - Qualified name of the port type
    */
   public void setPortTypeName(QName portType);

   /** Indicates whether addParameter and setReturnType methods are to be invoked to specify the parameter and return
    * type specification for a specific operation.
    * @param operationName Qualified name of the operation
    * @return Returns true if the Call implementation class requires addParameter and setReturnType to be invoked in the client code for the specified operation. This method returns false otherwise.
    * @throws IllegalArgumentException If invalid operation name is specified
    */
   public boolean isParameterAndReturnSpecRequired(QName operationName);

   /** Sets the return type for a specific operation. Invoking setReturnType(null) removes the return type for this Call object.
    *
    * @param xmlType XML data type of the return value
    * @throws JAXRPCException This exception may be thrown when the method isParameterAndReturnSpecRequired returns false.
    * @throws IllegalArgumentException If an illegal XML type is specified
    */
   public void setReturnType(QName xmlType);

   /** Sets the return type for a specific operation.
    *
    * @param xmlType XML data type of the return value
    * @param javaType Java Class of the return value
    * @throws JAXRPCException
    * <ul>
    *    <li>This exception may be thrown if this method is invoked when the method isParameterAndReturnSpecRequired returns false.
    *    <li>If XML type and Java type cannot be mapped using the standard type mapping or TypeMapping registry
    * </ul>
    * @throws UnsupportedOperationException If this method is not supported
    * @throws IllegalArgumentException If an illegal XML type is specified
    */
   public void setReturnType(QName xmlType, Class javaType);

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    *
    * @param inputParams Object[]--Parameters for this invocation. This includes only the input params
    * @return Returns the return value or null
    * @throws RemoteException if there is any error in the remote method invocation
    * @throws javax.xml.rpc.soap.SOAPFaultException Indicates a SOAP fault
    * @throws JAXRPCException
    * <ul>
    *    <li>If there is an error in the configuration of the Call object
    *    <li> If inputParams do not match the required parameter set (as specified through the addParameter invocations or in the corresponding WSDL)
    *    <li> If parameters and return type are incorrectly specified
    * </ul>
    */
   public Object invoke(Object[] inputParams) throws RemoteException;

   /** Gets the value of a named property.
    *
    * @param name Name of the property
    * @return Value of the named property
    * @throws JAXRPCException if an invalid or unsupported property name is passed.
    */
   public Object getProperty(String name);

   /** Sets the value for a named property.
    * JAX-RPC specification specifies a standard set of properties that may be passed to the Call.setProperty method.
    *
    * @param name Name of the property
    * @param value Value of the property
    * @throws JAXRPCException
    * <ul>
    *    <li> If an optional standard property name is specified, however this Call implementation class does not support the configuration of this property.
    *    <li> If an invalid (or unsupported) property name is specified or if a value of mismatched property type is passed.
    *    <li> If there is any error in the configuration of a valid property.
    * </ul>
    */
   public void setProperty(String name, Object value);

   /** Gets the XML type of a parameter by name.
    *
    * @param paramName - Name of the parameter
    * @return Returns XML type for the specified parameter
    */
   public QName getParameterTypeByName(String paramName);

   /** Invokes a specific operation using a synchronous request-response interaction mode.
    *
    * @param operationName QName of the operation
    * @param inputParams Object[]--Parameters for this invocation. This includes only the input params.
    * @return Return value or null
    * @throws RemoteException if there is any error in the remote method invocation.
    * @throws javax.xml.rpc.soap.SOAPFaultException - Indicates a SOAP fault
    * @throws JAXRPCException
    * <ul>
    *    <li> If there is an error in the configuration of the Call object
    *    <li> If inputParams do not match the required parameter set (as specified through the addParameter invocations or in the corresponding WSDL)
    *    <li> If parameters and return type are incorrectly specified
    * </ul>
    */
   public Object invoke(QName operationName, Object[] inputParams) throws RemoteException;

   /** Adds a parameter type and mode for a specific operation.
    * Note that the client code may not call any addParameter and setReturnType methods before calling the invoke method.
    * In this case, the Call implementation class determines the parameter types by using reflection on parameters,
    * using the WSDL description and configured type mapping registry.
    *
    * @param paramName Name of the parameter
    * @param xmlType XML type of the parameter
    * @param parameterMode Mode of the parameter-whether ParameterMode.IN, ParameterMode.OUT, or ParameterMode.INOUT
    * @throws JAXRPCException This exception may be thrown if the method isParameterAndReturnSpecRequired returns false for this operation.
    * @throws IllegalArgumentException If any illegal parameter name or XML type is specified
    */
   public void addParameter(String paramName, QName xmlType, ParameterMode parameterMode);

   /** Adds a parameter type and mode for a specific operation.
    * This method is used to specify the Java type for either OUT or INOUT parameters.
    *
    * @param paramName Name of the parameter
    * @param xmlType XML type of the parameter
    * @param javaType  Java class of the parameter
    * @param parameterMode Mode of the parameter-whether ParameterMode.IN, ParameterMode.OUT, or ParameterMode.INOUT
    * @throws JAXRPCException
    * <ul>
    *    <li>This exception may be thrown if the method isParameterAndReturnSpecRequired returns false for this operation.
    *    <li>If specified XML type and Java type mapping is not valid. For example, TypeMappingRegistry has no serializers for this mapping.
    * </ul>
    * @throws IllegalArgumentException If any illegal parameter name or XML type is specified
    * @throws UnsupportedOperationException If this method is not supported
    */
   public void addParameter(String paramName, QName xmlType, Class javaType, ParameterMode parameterMode);

}
