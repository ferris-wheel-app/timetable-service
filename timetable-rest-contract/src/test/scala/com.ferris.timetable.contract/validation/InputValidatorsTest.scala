package com.ferris.timetable.contract.validation

import com.ferris.microservice.exceptions.ApiExceptions.{InvalidFieldException, InvalidFieldPayload}
import com.ferris.timetable.contract.sample.{SampleData => SD}
import org.scalatest.{Assertions, FunSpec, Matchers}

class InputValidatorsTest extends FunSpec with Matchers with Assertions {

  describe("validating a timetable template creation") {
    it("should allow the creation of a valid object") {
      SD.timetableTemplateCreation.copy(
        `type` = SD.backlogItemCreation.`type`
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.backlogItemCreation.copy(
          `type` = "itch"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught.message shouldBe expected.message
    }
  }
}
