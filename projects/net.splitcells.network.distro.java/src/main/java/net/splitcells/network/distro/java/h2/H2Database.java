/* SPDX-License-Identifier: EPL-2.0 OR GPL-2.0-or-later
 * SPDX-FileCopyrightText: Contributors To The `net.splitcells.*` Projects
 */
package net.splitcells.network.distro.java.h2;

import net.splitcells.dem.lang.annotations.JavaLegacy;

import java.sql.Connection;
import java.sql.DriverManager;

@JavaLegacy
public class H2Database {
    public static void main(String[] a)
            throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:./database", "sa", "");
        conn.close();
    }
}
