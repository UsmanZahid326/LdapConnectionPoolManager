import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPConnectionOptions;
import com.unboundid.ldap.sdk.LDAPConnectionPool;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.SearchRequest;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchScope;
import com.unboundid.util.ssl.AggregateTrustManager;
import com.unboundid.util.ssl.HostNameSSLSocketVerifier;
import com.unboundid.util.ssl.JVMDefaultTrustManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;

import javax.net.ssl.SSLSocketFactory;
import java.security.GeneralSecurityException;
import java.util.Map;

public class LdapConnectionPoolManager {
    private LDAPConnectionPool connectionPool;
    public LdapConnectionPoolManager(String ldapHost, int ldapPort, String bindDN, String password,
                                     String trustStorePath, char[] trustStorePIN, int poolSize)
            throws LDAPException, GeneralSecurityException {

        // Create a TrustStoreTrustManager
        TrustStoreTrustManager trustManager = new TrustStoreTrustManager(
                trustStorePath, trustStorePIN, "PKCS12", true);

        // Create an AggregateTrustManager
        AggregateTrustManager aggregateTrustManager = new AggregateTrustManager(
                false,
                JVMDefaultTrustManager.getInstance(),
                trustManager
        );

        // Create SSLUtil
        SSLUtil sslUtil = new SSLUtil(aggregateTrustManager);

        // Set up connection options with SSL socket verification
        LDAPConnectionOptions connectionOptions = new LDAPConnectionOptions();
        connectionOptions.setSSLSocketVerifier(new HostNameSSLSocketVerifier(true));

        // Establish a connection using SSL
        SSLSocketFactory sslSocketFactory = sslUtil.createSSLSocketFactory();
        LDAPConnection connection = new LDAPConnection(
                sslSocketFactory, connectionOptions, ldapHost, ldapPort, bindDN, password
        );

        // Initialize the connection pool with a secure connection
        connectionPool = new LDAPConnectionPool(connection, poolSize);
    }

    public LDAPConnection getConnection() throws LDAPException {
        return connectionPool.getConnection();
    }

    public String getUserDN(LDAPConnection connection, String userName, String baseDN) throws LDAPException {
        Filter filter = Filter.createEqualityFilter("uid", userName);
        SearchRequest searchRequest = new SearchRequest(baseDN, SearchScope.SUB, filter);
        SearchResult searchResult = connection.search(searchRequest);
        if (searchResult.getEntryCount() > 0) {
            return searchResult.getSearchEntries().get(0).getDN();
        }
        throw new LDAPException(ResultCode.NO_RESULTS_RETURNED, "No user found with the provided username.");
    }

    public void updateUserAttributes(LDAPConnection connection, String userDN, Map<String, String> attributes) throws LDAPException {
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            ModifyRequest modifyRequest = new ModifyRequest(userDN,
                    new Modification(ModificationType.REPLACE, entry.getKey(), entry.getValue()));
            connection.modify(modifyRequest);
        }
    }

    public void addUserAttribute(LDAPConnection connection, String userDN, String attributeName, String attributeValue) throws LDAPException {
        ModifyRequest modifyRequest = new ModifyRequest(userDN,
                new Modification(ModificationType.ADD, attributeName, attributeValue));
        connection.modify(modifyRequest);
    }

    public void deleteUserAttribute(LDAPConnection connection, String userDN, String attributeName) throws LDAPException {
        ModifyRequest modifyRequest = new ModifyRequest(userDN,
                new Modification(ModificationType.DELETE, attributeName));
        connection.modify(modifyRequest);
    }

    public void replaceUserAttribute(LDAPConnection connection, String userDN, String attributeName, String newValue) throws LDAPException {
        ModifyRequest modifyRequest = new ModifyRequest(userDN,
                new Modification(ModificationType.REPLACE, attributeName, newValue));
        connection.modify(modifyRequest);
    }

    public void close() {
        connectionPool.close();
    }
}
