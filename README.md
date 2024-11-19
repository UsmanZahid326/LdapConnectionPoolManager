# LDAP Connection Pool Manager README

## Overview

The `LdapConnectionPoolManager` class is a utility for managing LDAP connections with secure SSL/TLS communication. It allows you to establish an LDAP connection pool, retrieve connections from the pool, search for users, and modify user attributes in an LDAP directory. The class also supports operations like adding, deleting, and replacing user attributes.

## Features

- **Secure LDAP Connection**: Supports secure communication with LDAP servers using SSL/TLS, with custom trust management using a specified trust store.
- **Connection Pooling**: Implements a connection pool to efficiently manage and reuse LDAP connections.
- **User Search**: Allows searching for a user by their username (`uid`) in the LDAP directory and retrieving the user's DN (Distinguished Name).
- **Modify User Attributes**: Enables modification of user attributes, including adding, deleting, and replacing them.
- **Exception Handling**: Proper error handling for LDAP operations, throwing meaningful exceptions when operations fail.

## Prerequisites

- Java 8 or higher
- Apache Directory LDAP SDK (`com.unboundid.ldap.sdk`)

## Dependencies

- `com.unboundid:unboundid-ldap-sdk` (LDAP SDK)
- `com.unboundid:unboundid-ssl` (SSL Utilities)

These dependencies can be added to your project via Maven or Gradle.

### Maven Dependency

```xml
<dependency>
    <groupId>com.unboundid</groupId>
    <artifactId>unboundid-ldap-sdk</artifactId>
    <version>6.0.3</version>
</dependency>

<dependency>
    <groupId>com.unboundid</groupId>
    <artifactId>unboundid-ssl</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle Dependency

```gradle
implementation 'com.unboundid:unboundid-ldap-sdk:6.0.3'
implementation 'com.unboundid:unboundid-ssl:1.0.0'
```

## Usage

### 1. Initializing the `LdapConnectionPoolManager`

You can create an instance of `LdapConnectionPoolManager` by passing the required parameters, such as the LDAP host, port, user DN, password, trust store path, trust store PIN, and the desired pool size.

```java
LdapConnectionPoolManager poolManager = new LdapConnectionPoolManager(
    "ldap.example.com",    // LDAP Host
    636,                   // LDAP Port (636 for SSL)
    "cn=admin,dc=example,dc=com", // Bind DN
    "password",            // Password
    "/path/to/truststore.p12",    // Trust Store Path
    "truststorePassword".toCharArray(), // Trust Store PIN
    10                      // Pool Size
);
```

### 2. Getting a Connection

Once the `LdapConnectionPoolManager` is initialized, you can get an LDAP connection from the connection pool:

```java
LDAPConnection connection = poolManager.getConnection();
```

### 3. Searching for a User

You can search for a user by their username (`uid`) and retrieve their Distinguished Name (DN).

```java
String userDN = poolManager.getUserDN(connection, "john.doe", "ou=users,dc=example,dc=com");
```

### 4. Updating User Attributes

To modify user attributes, you can use the `updateUserAttributes` method. You need to pass a map containing attribute names and their new values.

```java
Map<String, String> attributes = new HashMap<>();
attributes.put("mail", "john.doe@example.com");
attributes.put("phoneNumber", "1234567890");
poolManager.updateUserAttributes(connection, userDN, attributes);
```

### 5. Adding, Replacing, or Deleting User Attributes

You can add, replace, or delete individual attributes for a user.

#### Add an Attribute

```java
poolManager.addUserAttribute(connection, userDN, "address", "123 Main St");
```

#### Replace an Attribute

```java
poolManager.replaceUserAttribute(connection, userDN, "address", "456 New St");
```

#### Delete an Attribute

```java
poolManager.deleteUserAttribute(connection, userDN, "address");
```

### 6. Closing the Connection Pool

When you're done with the LDAP connection pool, you should close it to release resources:

```java
poolManager.close();
```

## Exception Handling

This class throws various `LDAPException` errors from the UnboundID SDK. Make sure to handle these exceptions appropriately in your code:

- `LDAPException`: Thrown for LDAP-related errors such as failed operations or connection issues.
- `GeneralSecurityException`: Thrown for SSL-related errors during connection initialization.

## Security Considerations

- The connection is secured using SSL/TLS (via port 636).
- Custom trust management is used to ensure the LDAP server's certificate is verified using the provided trust store.

## Conclusion

The `LdapConnectionPoolManager` class simplifies the management of LDAP connections and user attribute operations. It ensures secure communication with the LDAP server and provides an efficient way to handle multiple concurrent LDAP requests using connection pooling.