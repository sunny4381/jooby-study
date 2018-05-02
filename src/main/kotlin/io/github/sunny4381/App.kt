package io.github.sunny4381

import com.typesafe.config.Config
import org.jooby.Kooby
import org.jooby.Results
import org.jooby.hbs.Hbs
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
        Results.html("index").put("model", profile.attributes["cn"] ?: profile.id)
    }
}) {
    companion object {
        fun createLapProfileService(conf: Config) : LdapProfileService {
            val ldapUrl = conf.getString("ldap.url")
            val ldapUsersDn = conf.getString("ldap.usersDn")
            val ldapId = conf.getString("ldap.id")

            val dnResolver = FormatDnResolver()
            dnResolver.format = "$ldapId=%s,$ldapUsersDn"

            val connectionConfig = ConnectionConfig()
            connectionConfig.connectTimeout = Duration.ofMillis(500)
            connectionConfig.responseTimeout = Duration.ofMillis(1000)
            connectionConfig.ldapUrl = ldapUrl

            val connectionFactory = DefaultConnectionFactory()
            connectionFactory.connectionConfig = connectionConfig

            val poolConfig = PoolConfig()
            poolConfig.minPoolSize = 1
            poolConfig.maxPoolSize = 2
            poolConfig.isValidateOnCheckOut = true
            poolConfig.isValidateOnCheckIn = true
            poolConfig.isValidatePeriodically = false

            val searchValidator = SearchValidator()

            val pruneStrategy = IdlePruneStrategy()

            val connectionPool = BlockingConnectionPool()
            connectionPool.poolConfig = poolConfig
            connectionPool.blockWaitTime = Duration.ofMillis(1000)
            connectionPool.validator = searchValidator
            connectionPool.pruneStrategy = pruneStrategy
            connectionPool.connectionFactory = connectionFactory
            connectionPool.initialize()

            val pooledConnectionFactory = PooledConnectionFactory()
            pooledConnectionFactory.connectionPool = connectionPool

            val handler = PooledBindAuthenticationHandler()
            handler.connectionFactory = pooledConnectionFactory

            val ldaptiveAuthenticator = Authenticator()
            ldaptiveAuthenticator.dnResolver = dnResolver
            ldaptiveAuthenticator.authenticationHandler = handler

            val ldapProfileService = LdapProfileService()
            ldapProfileService.ldapAuthenticator = ldaptiveAuthenticator
            ldapProfileService.connectionFactory = connectionFactory
            ldapProfileService.usersDn = ldapUsersDn
            ldapProfileService.idAttribute = ldapId
            ldapProfileService.attributes = "cn,sn"

            return ldapProfileService
        }
    }
}
