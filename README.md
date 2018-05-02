Trivial Jooby Study
====

## What is Jooby

[Jooby](https://jooby.org/) is a Scalable, fast and modular micro web framework for Java. It is also support Kotlin.

## About This Study

- Use [Handlebars](http://jknack.github.io/handlebars.java/) for view template.
- Use LDAP with [PAC4J](https://www.pac4j.org/index.html) for login

## Test LDAP Server

To launch test ldap server, run below commands:

~~~
$ docker pull osixia/openldap
$ docker pull osixia/phpldapadmin                                                                 
$ docker run --name openldap --hostname openldap -p 10389:389 --detach osixia/openldap
$ docker run --name phpldapadmin -p 18080:80 --hostname phpldapadmin --link openldap:ldap-host --env PHPLDAPADMIN_LDAP_HOSTS=ldap-host --env PHPLDAPADMIN_HTTPS=false --detach osixia/phpldapadmin
~~~

And then open <http://localhost:18080/> in your browser, import these ldif

~~~
version: 1

dn: ou=people,dc=example,dc=org
objectclass: top
objectclass: organizationalUnit
ou: people

dn: uid=admin,ou=people,dc=example,dc=org
cn: Kazuki Shimizu
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
sn: Kazuki
uid: admin
userpassword: {CRYPT}7pnoyta7lRz7M

dn: uid=user,ou=people,dc=example,dc=org
cn: Taro Yamada
objectclass: top
objectclass: person
objectclass: organizationalPerson
objectclass: inetOrgPerson
sn: Taro
uid: user
userpassword: {CRYPT}5yE50Zf2Dqg2o

dn: ou=groups,dc=example,dc=org
objectclass: top
objectclass: organizationalUnit
ou: groups

dn: cn=admin,ou=groups,dc=example,dc=org
cn: admin
objectclass: groupOfUniqueNames
objectclass: top
uniquemember: uid=admin,ou=people,dc=example,dc=org

dn: cn=user,ou=groups,dc=example,dc=org
cn: user
objectclass: groupOfUniqueNames
objectclass: top
uniquemember: uid=admin,ou=people,dc=example,dc=org
uniquemember: uid=user,ou=people,dc=example,dc=org
~~~

By importing these ldif, create these users and groups

| User ID | Password | Full Name | Group (= Role)   |
|---------|----------|----------------|-------------|
| admin   | password | Kazuki Shimizu | admin, user |
| user    | password | Taro Yamada    | user        |

# Related Work

- [Pac4j LDAP](https://www.pac4j.org/docs/authenticators/ldap.html)
