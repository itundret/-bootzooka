package pl.softwaremill.bootstrap.rest

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class EntriesServletSpec extends ScalatraSpec {

    def is = "GET / on EntriesServlet" ^
        "should return status 200" ! root200 ^
        end

    addServlet(classOf[EntriesServlet], "/*")

    def root200 = get("/") {
        status must_== 200
    }

}
