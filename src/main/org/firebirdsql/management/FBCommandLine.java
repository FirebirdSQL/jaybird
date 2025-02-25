/*
 SPDX-FileCopyrightText: Copyright 2002 David Jencks
 SPDX-FileCopyrightText: Copyright 2003 Roman Rokytskyy
 SPDX-FileCopyrightText: Copyright 2004 Steven Jardine
 SPDX-FileCopyrightText: Copyright 2024 Mark Rotteveel
 SPDX-License-Identifier: LGPL-2.1-or-later
*/
package org.firebirdsql.management;

/**
 * FBCommandLine
 *
 * @author David Jencks
 */
@SuppressWarnings("java:S106")
public class FBCommandLine {

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            usage();
            return;
        }
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
        try (var fbm = new FBManager()) {
            System.out.printf("filename: %s, user: %s, password: %s%n", filename, user, password);
            fbm.start();
            if (args[0].equals("-create") || args[0].equals("-c")) {
                fbm.createDatabase(filename, user, password);
                return;
            }

            if (args[0].equals("-drop") || args[0].equals("-d")) {
                fbm.dropDatabase(filename, user, password);
                return;
            }
        }
        usage();
    }

    private static void usage() {
        System.out.println("Firebird driver command line db create/drop tool");
        System.out.println("This works only on localhost. Use filename rather than jdbc url.");
        System.out.println("create:");
        System.out.println("     -create <filename> -user <user> -password <password>");
        System.out.println("drop:");
        System.out.println("     -drop <filename>");
        System.out.println("flags -create may be abbreviated as -c, etc.");
    }

}
