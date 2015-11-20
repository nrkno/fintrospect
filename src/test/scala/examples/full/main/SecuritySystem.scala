package examples.full.main

import java.time.Clock

import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, ListeningServer}
import com.twitter.util.Future
import io.fintrospect.FintrospectModule
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.renderers.swagger2dot0.{ApiInfo, Swagger2dot0Json}

class SecuritySystem(serverPort: Int, userDirectoryPort: Int, entryLoggerPort: Int, clock: Clock) {

  private var server: ListeningServer = null
  private val apiInfo = ApiInfo("Security System", "1.0", Option("Building security system"))
  private val userDirectory = new UserDirectory(s"localhost:$userDirectoryPort")
  private val entryLogger = new EntryLogger(s"localhost:$entryLoggerPort", clock)

  // use CORs settings that suit your particular use-case. This one allows any cross-domain traffic at all and is applied
  // to all routes in the module
  private val globalCorsFilter = new HttpFilter(Cors.UnsafePermissivePolicy)

  private val secretKeyChecker = new SecretKeyChecker()
  private val securityModule = FintrospectModule(Root / "security", Swagger2dot0Json(apiInfo), globalCorsFilter)
    .withRoutes(new KnockKnock(secretKeyChecker, userDirectory, entryLogger))
    .withRoutes(new ByeBye(secretKeyChecker, entryLogger))

  private val statusModule = FintrospectModule(Root / "internal", SimpleJson()).withRoute(new Ping().route)

  def start() = {
    server = Http.serve(s":$serverPort", FintrospectModule.toService(securityModule combine statusModule))
    Future.Done
  }

  def stop() = server.close()

}
