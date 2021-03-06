package examples.strictcontenttypes

import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Request
import com.twitter.finagle.http.filter.Cors
import com.twitter.finagle.http.filter.Cors.HttpFilter
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.{Http, Service}
import com.twitter.util.Await
import io.fintrospect.parameters.Body
import io.fintrospect.renderers.simplejson.SimpleJson
import io.fintrospect.util.MultiBodyType
import io.fintrospect.{RouteModule, RouteSpec}

/**
  * Shows how to add routes which can accept multiple body types (based on the Content-Type header)
  */
object MultiBodyTypeRoute extends App {

  private val json = Body.json("json body")

  private val echoJson = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Argo.ResponseBuilder._
    Ok(json <-- rq)
  }

  private val xml = Body.xml("xml body")

  private val echoXml = Service.mk { (rq: Request) =>
    import io.fintrospect.formats.Xml.ResponseBuilder._
    Ok(xml <-- rq)
  }

  val route = RouteSpec("echo posted content in either JSON or XML").at(Post) / "echo" bindTo MultiBodyType(json -> echoJson, xml -> echoXml)

  println("See the service description at: http://localhost:8080")

  Await.ready(
    Http.serve(":8080", new HttpFilter(Cors.UnsafePermissivePolicy)
      .andThen(RouteModule(Root, SimpleJson()).withRoute(route).toService))
  )
}
