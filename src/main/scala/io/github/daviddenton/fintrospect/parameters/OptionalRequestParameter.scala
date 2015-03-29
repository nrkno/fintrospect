package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class OptionalRequestParameter[T](name: String, description: Option[String], location: Location, paramType: ParamType, parse: (String => Option[T]))
  extends RequestParameter[T](name, description, location, paramType, parse) {
  def from(request: Request): Option[T] = location.from(name, request).flatMap(parse)
}


object OptionalRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[OptionalRequestParameter]() {
    def apply[T](name: String, description: Option[String], paramType: ParamType, parse: (String => Option[T])) = new OptionalRequestParameter[T](name, description, location, paramType, parse)
  }
}
