import org.scalatest.{GivenWhenThen, flatspec}
import Models._
import io.circe.parser.decode
import io.circe.generic.auto._
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper

class ModelsSpec extends flatspec.AnyFlatSpec with GivenWhenThen {

  "An Artist" must "deserialize from some fields" in {
    Given("a json string")
    val json_string =
      """
        |{
        |  "name":"Donna Summer"
        |}
        |""".stripMargin

    And("the expected value")
    val expected = Artist(name = "Donna Summer")

    When("deserialize")
    val deserialized = decode[Artist](json_string)

    Then("must be right")
    deserialized.isRight mustBe true

    And("must be the expected")
    deserialized.toOption.get mustBe expected
  }

}
