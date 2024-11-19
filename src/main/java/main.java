import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.RootDSE;
import com.unboundid.ldap.sdk.SearchResultEntry;

import java.util.HashMap;
import java.util.Map;

public class main {
    public static void main(String[] args) {
        try {
            String trustStorePath = "/Users/usmanzahid/ldap/ssl/ldap.crt";

            LdapConnectionPoolManager poolManager = new LdapConnectionPoolManager(
                    "localhost", 636, "cn=admin,dc=mycompany,dc=com", "admin",
                    trustStorePath, "123456".toCharArray(), 10
            );

            try (LDAPConnection connection = poolManager.getConnection()) {
                // Use the connection here
                RootDSE rootDSE = connection.getRootDSE();
                System.out.println("RootDSE Retrieved: " + rootDSE);

                // Example of getting user DN
                String userDN = poolManager.getUserDN(connection, "jdoe", "dc=mycompany,dc=com");
                System.out.println("User DN: " + userDN);

                // Example of updating user attributes
                Map<String, String> attributes = new HashMap<>();
                attributes.put("givenName", "John");
                attributes.put("sn", "Doe");
                poolManager.updateUserAttributes(connection, userDN, attributes);

                // Test adding a new attribute

                poolManager.addUserAttribute(connection, userDN, "description", "Test User");
                System.out.println("Added 'description' attribute.");

                SearchResultEntry entry = connection.getEntry(userDN, "description");
                String description = entry.getAttributeValue("description");
                System.out.println("Description: " + description);

                // Test replacing an existing attribute
                poolManager.replaceUserAttribute(connection, userDN, "description", "Updated Test User");
                System.out.println("Replaced 'description' attribute.");

                entry = connection.getEntry(userDN, "description");
                description = entry.getAttributeValue("description");
                System.out.println("Description: " + description);

                // Test deleting an attribute
                poolManager.deleteUserAttribute(connection, userDN, "description");
                System.out.println("Deleted 'description' attribute.");

            }

            poolManager.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
