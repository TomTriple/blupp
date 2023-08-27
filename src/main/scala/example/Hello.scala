package example

import zio._
import zio.http._
import zio.http.html._
import zio.http.endpoint.Endpoint
import zio.http.codec.PathCodec
import zio.http.codec.QueryCodec

object HelloWorld extends ZIOAppDefault {

  case class Lang(value:String)
  object Lang {
    def fromString(value:String):Either[String,Lang] = value.toLowerCase match {
      case "scala" => Right(Lang("scala"))
      case "java" => Right(Lang("java"))
      case other => Left(s"not valid: $other")
    }
  }

  val r1 = Method.GET / "rrr" -> Handler.attempt {
    throw new Exception("blublublu")
  }
  val rrr = Routes(r1)



  val app1 = Routes(

      Method.GET / "text" -> handler(Response.html(
        div(
          li(a(hrefAttr := "http://www.zio.dev", "Website zio.dev")), 
          li(a(hrefAttr := "http://www.ziverge.com", "Website Ziverge")),
          li(a(hrefAttr := "http://www.golem.cloud", "Golem Cloud")),
        )
      )), 

      Method.GET / "" -> handler(
        Response.html(
          div(
            li(a(hrefAttr := s"/language/scala", "Language: Scala")), 
            li(a(hrefAttr := s"/profile/blupp", "Profile: Blupp")), 
          )
        )
      ),

      Method.GET / "language" / string("lang").transformOrFailLeft(Lang.fromString)(_.value) -> handler { (lang:Lang, req:Request) =>
        Response.html(s"lang: $lang")
      },

      Method.GET / "number" / int("number") -> handler { (number:Int, _:Request) => 
        Response.text(s"number hier: $number")
      }

    )

  val endpointProfile = Endpoint(Method.GET / "profile" / string("name")).out[String]
  val app2 = endpointProfile.implement(Handler.fromFunction[String] { s =>
      s"name: $s"
  })
 
  val allRoutes = app1 ++ Routes(app2) ++ rrr.handleError(th => Response.text("fehler bei rrr: " + th.getMessage()))

  override val run =
    Server.serve(allRoutes.toHttpApp ++ Response.html(div("leider ein fehler"), Status.NotFound).toHandler.toHttpApp).provide(Server.default)
} 
