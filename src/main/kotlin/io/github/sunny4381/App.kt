package io.github.sunny4381

import com.typesafe.config.Config
import org.jooby.Kooby
import org.jooby.Results
import org.jooby.hbs.Hbs
import org.jooby.internal.pac4j2.Pac4jLoginForm
import org.jooby.pac4j.Pac4j
import org.ldaptive.ConnectionConfig
import org.ldaptive.DefaultConnectionFactory
import org.ldaptive.auth.Authenticator
import org.ldaptive.auth.FormatDnResolver
import org.ldaptive.auth.PooledBindAuthenticationHandler
import org.ldaptive.pool.*
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.ldap.profile.service.LdapProfileService
import java.time.Duration
import org.pac4j.core.profile.CommonProfile

class App : Kooby({
    use(Hbs("/", ".hbs"))

    get("/login") {
        val view = Results.html("login")
        val error = param("error")
        if (error.isSet) {
            view.put("error", error.value())
        }
        view
    }

    use(Pac4j().client({ conf -> FormClient("/login", createLapProfileService(conf)) }))

    get("/") {
        val profile = require(CommonProfile::class.java)
        Results.html("index").put("model", profile.attributes["cn"])
    }
}) {
    companion object {
        fun createLapProfileService(conf: Config) : LdapProfileService {
            val dnResolver = FormatDnResolver()
            dnResolver.format = "uid=%s,ou=people,dc=example,dc=org"

            val connectionConfig = ConnectionConfig()
            connectionConfig.connectTimeout = Duration.ofMillis(500)
            connectionConfig.responseTimeout = Duration.ofMillis(1000)
            connectionConfig.ldapUrl = "ldap://localhost:10389"

            val connectionFactory = DefaultConnectionFactory()
            connectionFactory.connectionConfig = connectionConfig

            val poolConfig = PoolConfig()
            poolConfig.setMinPoolSize(1)
            poolConfig.setMaxPoolSize(2)
            poolConfig.setValidateOnCheckOut(true)
            poolConfig.setValidateOnCheckIn(true)
            poolConfig.setValidatePeriodically(false)

            val searchValidator = SearchValidator()

            val pruneStrategy = IdlePruneStrategy()

            val connectionPool = BlockingConnectionPool()
            connectionPool.setPoolConfig(poolConfig)
            connectionPool.setBlockWaitTime(Duration.ofMillis(1000))
            connectionPool.setValidator(searchValidator)
            connectionPool.setPruneStrategy(pruneStrategy)
            connectionPool.setConnectionFactory(connectionFactory)
            connectionPool.initialize()

            val pooledConnectionFactory = PooledConnectionFactory()
            pooledConnectionFactory.setConnectionPool(connectionPool)

            val handler = PooledBindAuthenticationHandler()
            handler.setConnectionFactory(pooledConnectionFactory)

            val ldaptiveAuthenticator = Authenticator()
            ldaptiveAuthenticator.setDnResolver(dnResolver)
            ldaptiveAuthenticator.setAuthenticationHandler(handler)

            val ldapProfileService = LdapProfileService()
            ldapProfileService.ldapAuthenticator = ldaptiveAuthenticator
            ldapProfileService.connectionFactory = connectionFactory
            ldapProfileService.usersDn = "ou=people,dc=example,dc=org"
            ldapProfileService.idAttribute = "uid"
            ldapProfileService.attributes = "cn,sn"

            return ldapProfileService
        }
    }
}
