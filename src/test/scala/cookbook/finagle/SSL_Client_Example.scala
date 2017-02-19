package cookbook.finagle

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Await

object SSL_Client_Example extends App {

  val client = Http.client.withTls("api.github.com").newService("api.github.com:443")

  val request = Request("/users/daviddenton/repos")

  request.headerMap("User-Agent") = "Fintrospect client"

  println(Await.result(client(request)).contentString)
}