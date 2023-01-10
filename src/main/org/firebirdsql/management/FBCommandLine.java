/*
 * Firebird Open Source JDBC Driver
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

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.firebirdsql.management;

/**
 * FBCommandLine.java
 * 
 * 
 * Created: Thu Oct 10 14:14:07 2002
 * 
 * @author David Jencks
 */

public class FBCommandLine {
    public FBCommandLine() {

    }

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            usage();
            return;
        } // end of if ()
        if (!(args[2].equals("-user") || args[2].equals("-u"))) {
            usage();
            return;
        }
        if (!(args[4].equals("-password") || args[4].equals("-p"))) {
            usage();
            return;
        }
        String filename = args[1];
        String user = args[3];
        String password = args[5];
        FBManager fbm = new FBManager();
        System.out.println("filename: " + filename + ", user: " + user
                + ", password: " + password);
        fbm.start();
        if (args[0].equals("-create") || args[0].equals("-c")) {
            fbm.createDatabase(filename, user, password);
            return;
        } // end of if ()
        if (args[0].equals("-drop") || args[0].equals("-d")) {
            fbm.dropDatabase(filename, user, password);
            return;
        }
        usage();

    } // end of main()

    private static void usage() {
        System.out.println("Firebird driver command line db create/drop tool");
        System.out
                .println("This works only on localhost. Use filename rather than jdbc url.");
        System.out.println("create:");
        System.out
                .println("     -create <filename> -user <user> -password <password>");
        System.out.println("drop:");
        System.out.println("     -drop <filename>");
        System.out.println("flags -create may be abbreviated as -c, etc.");
    }

}// FBCommandLine
