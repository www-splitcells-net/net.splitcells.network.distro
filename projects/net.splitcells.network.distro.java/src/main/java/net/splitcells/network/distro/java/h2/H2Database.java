package net.splitcells.network.distro.java.h2;

import net.splitcells.dem.lang.annotations.JavaLegacyArtifact;

import java.sql.Connection;
import java.sql.DriverManager;

@JavaLegacyArtifact
public class H2Database {
    public static void main(String[] a)
            throws Exception {
        Class.forName("org.h2.Driver");
        Connection conn = DriverManager.getConnection("jdbc:h2:./database", "sa", "");
        conn.close();
    }
}
