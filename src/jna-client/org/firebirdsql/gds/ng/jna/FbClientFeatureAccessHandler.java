/*
 * Firebird Open Source JavaEE Connector - JDBC Driver
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a source control history command.
 *
 * All rights reserved.
 */
package org.firebirdsql.gds.ng.jna;

import com.sun.jna.Library;
import com.sun.jna.NativeLibrary;
import org.firebirdsql.gds.JaybirdSystemProperties;
import org.firebirdsql.jna.fbclient.FbClientLibrary;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Set;

/**
 * Invocation handler to check for feature existence as part of the client library initialization.
 *
 * @since 4.0
 */
class FbClientFeatureAccessHandler implements InvocationHandler {

    private static final Method HAS_FEATURE;
    private static final Method GET_FEATURES;

    static {
        try {
            HAS_FEATURE = FbClientFeatureAccess.class.getMethod("hasFeature", FbClientFeature.class);
            GET_FEATURES = FbClientFeatureAccess.class.getMethod("getFeatures");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final FbClientLibrary clientLibrary;
    private final NativeLibrary nativeLibrary;
    private final Set<FbClientFeature> clientFeatures;
    private final InvocationHandler delegatedHandler;

    private FbClientFeatureAccessHandler(FbClientLibrary clientLibrary, NativeLibrary nativeLibrary,
            Set<FbClientFeature> clientFeatures, InvocationHandler delegatedHandler) {
        this.clientLibrary = clientLibrary;
        this.nativeLibrary = nativeLibrary;
        this.clientFeatures = clientFeatures;
        this.delegatedHandler = delegatedHandler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] inArgs) throws Throwable {
        // Handle implementation of FbClientFeatureAccess
        if (HAS_FEATURE.equals(method)) {
            //noinspection SuspiciousMethodCalls
            return clientFeatures.contains(inArgs[0]);
        }
        if (GET_FEATURES.equals(method)) {
            return clientFeatures;
        }

        return delegatedHandler.invoke(clientLibrary, method, inArgs);
    }

    NativeLibrary getNativeLibrary() {
        return nativeLibrary;
    }

    /**
     * Creates a {@code FbClientLibrary} proxy implementing {@link FbClientFeatureAccess}.
     *
     * @param library
     *         The original {@code FbClientLibrary} proxy object
     * @return New proxy for {@code library} that implements {@link FbClientFeatureAccess}
     * @throws IllegalArgumentException
     *         if {@code library} is not a proxy with {@link Library.Handler} as its invocation handler
     */
    static FbClientLibrary decorateWithFeatureAccess(FbClientLibrary library) {
        Class<?> libraryClass = library.getClass();
        if (!Proxy.isProxyClass(libraryClass)) {
            throw new IllegalArgumentException(
                    "Could not decorate client library with FbClientFeatureAccess: not a proxy");
        }
        InvocationHandler ih = Proxy.getInvocationHandler(library);
        if (!(ih instanceof Library.Handler)) {
            throw new IllegalArgumentException("Could not decorate client library with FbClientFeatureAccess: "
                    + "unexpected invocation handler type " + ih.getClass());
        }

        Library.Handler originalHandler = (Library.Handler) ih;
        NativeLibrary nativeLibrary = originalHandler.getNativeLibrary();
        Set<FbClientFeature> clientFeatures = determineClientFeatures(nativeLibrary);

        InvocationHandler delegatedHandler = syncWrapIfNecessary(library, originalHandler);

        FbClientFeatureAccessHandler fbClientFeatureAccessHandler =
                new FbClientFeatureAccessHandler(library, nativeLibrary, clientFeatures, delegatedHandler);

        Class<?> interfaceClass = originalHandler.getInterfaceClass();
        ClassLoader loader = interfaceClass.getClassLoader();
        Object proxy = Proxy.newProxyInstance(loader, new Class[] { interfaceClass, FbClientFeatureAccess.class },
                fbClientFeatureAccessHandler);
        return (FbClientLibrary) proxy;
    }

    private static Set<FbClientFeature> determineClientFeatures(NativeLibrary nativeLibrary) {
        EnumSet<FbClientFeature> features = EnumSet.allOf(FbClientFeature.class);
        for (FbClientFeature feature : FbClientFeature.values()) {
            for (String methodName : feature.methodNames()) {
                if (!hasMethod(nativeLibrary, methodName)) {
                    features.remove(feature);
                    break;
                }
            }
        }
        return features;
    }

    private static boolean hasMethod(NativeLibrary nativeLibrary, String methodName) {
        try {
            return nativeLibrary.getFunction(methodName) != null;
        } catch (UnsatisfiedLinkError e) {
            // not found
            return false;
        }
    }

    private static InvocationHandler syncWrapIfNecessary(final FbClientLibrary clientLibrary,
            final Library.Handler originalHandler) {
        if (JaybirdSystemProperties.isSyncWrapNativeLibrary()) {
            // Mimics com.sun.jna.Native.synchronizedLibrary(..) with creating a proxy
            return new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    synchronized (originalHandler.getNativeLibrary()) {
                        return originalHandler.invoke(clientLibrary, method, args);
                    }
                }
            };
        }
        return originalHandler;
    }
}
