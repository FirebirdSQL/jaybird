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
import org.firebirdsql.logging.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Invocation handler to check for feature existence as part of the client library initialization.
 *
 * @since 4.0
 */
class FbClientFeatureAccessHandler extends Library.Handler {

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

    private final Set<FbClientFeature> clientFeatures;

    FbClientFeatureAccessHandler(Library.Handler originalHandler) {
        super(originalHandler.getLibraryName(), originalHandler.getInterfaceClass(), extractOptions(originalHandler));
        clientFeatures = unmodifiableSet(determineClientFeatures(originalHandler.getNativeLibrary()));
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
        
        return super.invoke(proxy, method, inArgs);
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

    private static Map<String, ?> extractOptions(Library.Handler originalHandler) {
        try {
            Field optionsField = Library.Handler.class.getDeclaredField("options");
            optionsField.setAccessible(true);
            @SuppressWarnings("unchecked") Map<String, ?> optionsMap =
                    (Map<String, ?>) optionsField.get(originalHandler);
            return optionsMap;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LoggerFactory.getLogger(FbClientFeatureAccessHandler.class)
                    .warn("Unable to determine options, using empty options: " + e);
            return Collections.emptyMap();
        }
    }
}
