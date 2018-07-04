package com.ferris.planning.contract.validation

import java.util.UUID

import com.ferris.microservice.exceptions.ApiExceptions.{InvalidFieldException, InvalidFieldPayload}
import com.ferris.planning.contract.resource.Resources.In.{PyramidOfImportanceUpsert, TierUpsert}
import org.scalatest.{Assertions, FunSpec, Matchers}
import com.ferris.planning.contract.sample.{SampleData => SD}

class InputValidatorsTest extends FunSpec with Matchers with Assertions {

  describe("validating a backlog-item creation") {
    it("should allow the creation of a valid object") {
      SD.backlogItemCreation.copy(
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

  describe("validating a backlog-item update") {
    it("should allow the creation of a valid object") {
      SD.backlogItemUpdate.copy(
        `type` = SD.backlogItemUpdate.`type`
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.backlogItemUpdate.copy(
          `type` = Some("itch")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught.message shouldBe expected.message
    }
  }

  describe("validating a goal creation") {
    it("should allow the creation of a valid object") {
      SD.goalCreation.copy(
        backlogItems = UUID.randomUUID :: UUID.randomUUID :: Nil,
        graduation = SD.goalCreation.graduation,
        status = SD.goalCreation.status
      )
    }

    it("should throw an exception if there are more than 10 backlog-items") {
      val caught = intercept[InvalidFieldException] {
        SD.goalCreation.copy(
          backlogItems = (1 to 11).map(_ => UUID.randomUUID)
        )
      }
      val expected = InvalidFieldException("InvalidField", "Backlog Items must be a maximum of 10 items", Some(InvalidFieldPayload("backlogItems")))
      caught shouldBe expected
    }

    it("should throw an exception if there are duplicated backlog-items") {
      val duplicatedId = UUID.randomUUID
      val caught = intercept[InvalidFieldException] {
        SD.goalCreation.copy(
          backlogItems = duplicatedId :: duplicatedId :: UUID.randomUUID :: Nil
        )
      }
      val expected = InvalidFieldException("InvalidField", "Backlog Items cannot contain duplicate entries", Some(InvalidFieldPayload("backlogItems")))
      caught shouldBe expected
    }

    it("should throw an exception if the graduation is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.goalCreation.copy(
          graduation = "masters"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Graduation", Some(InvalidFieldPayload("graduation")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.goalCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a goal update") {
    it("should allow the update of a valid object") {
      SD.goalUpdate.copy(
        backlogItems = Some(UUID.randomUUID :: UUID.randomUUID :: Nil),
        graduation = SD.goalUpdate.graduation,
        status = SD.goalUpdate.status
      )
    }

    it("should throw an exception if there are more than 10 backlog-items") {
      val caught = intercept[InvalidFieldException] {
        SD.goalUpdate.copy(
          backlogItems = Some((1 to 11).map(_ => UUID.randomUUID))
        )
      }
      val expected = InvalidFieldException("InvalidField", "Backlog Items must be a maximum of 10 items", Some(InvalidFieldPayload("backlogItems")))
      caught shouldBe expected
    }

    it("should throw an exception if there are duplicated backlog-items") {
      val duplicatedId = UUID.randomUUID
      val caught = intercept[InvalidFieldException] {
        SD.goalUpdate.copy(
          backlogItems = Some(duplicatedId :: duplicatedId :: UUID.randomUUID :: Nil)
        )
      }
      val expected = InvalidFieldException("InvalidField", "Backlog Items cannot contain duplicate entries", Some(InvalidFieldPayload("backlogItems")))
      caught shouldBe expected
    }

    it("should throw an exception if the graduation is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.goalUpdate.copy(
          graduation = Some("masters")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Graduation", Some(InvalidFieldPayload("graduation")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.goalUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a thread creation") {
    it("should allow the creation of a valid object") {
      SD.threadCreation.copy(
        status = SD.threadCreation.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.threadCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a thread update") {
    it("should allow the creation of a valid object") {
      SD.threadUpdate.copy(
        status = SD.threadUpdate.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.threadUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a weave creation") {
    it("should allow the creation of a valid object") {
      SD.weaveCreation.copy(
        `type` = SD.weaveCreation.`type`,
        status = SD.weaveCreation.status
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.weaveCreation.copy(
          `type` = "straight"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.weaveCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a weave update") {
    it("should allow the creation of a valid object") {
      SD.weaveUpdate.copy(
        `type` = SD.weaveUpdate.`type`,
        status = SD.weaveUpdate.status
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.weaveUpdate.copy(
          `type` = Some("straight")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.weaveUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a laser-donut creation") {
    it("should allow the creation of a valid object") {
      SD.laserDonutCreation.copy(
        `type` = SD.laserDonutCreation.`type`,
        status = SD.laserDonutCreation.status
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.laserDonutCreation.copy(
          `type` = "round"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.laserDonutCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a laser-donut update") {
    it("should allow the creation of a valid object") {
      SD.laserDonutUpdate.copy(
        `type` = SD.laserDonutUpdate.`type`,
        status = SD.laserDonutUpdate.status
      )
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.laserDonutUpdate.copy(
          `type` = Some("round")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.laserDonutUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a portion creation") {
    it("should allow the creation of a valid object") {
      SD.portionCreation.copy(
        status = SD.portionCreation.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.portionCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a portion update") {
    it("should allow the creation of a valid object") {
      SD.portionUpdate.copy(
        status = SD.portionUpdate.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.portionUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a todo creation") {
    it("should allow the creation of a valid object") {
      SD.todoCreation.copy(
        status = SD.todoCreation.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.todoCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a todo update") {
    it("should allow the creation of a valid object") {
      SD.todoUpdate.copy(
        status = SD.todoUpdate.status
      )
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.todoUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a hobby creation") {
    it("should allow the creation of a valid object") {
      SD.hobbyCreation.copy(
        frequency = SD.hobbyCreation.frequency,
        `type` = SD.hobbyCreation.`type`,
        status = SD.hobbyCreation.status
      )
    }

    it("should throw an exception if the frequency is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyCreation.copy(
          frequency = "every weekend"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Frequency", Some(InvalidFieldPayload("frequency")))
      caught shouldBe expected
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyCreation.copy(
          `type` = "anti-social"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyCreation.copy(
          status = "dead"
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a hobby update") {
    it("should allow the creation of a valid object") {
      SD.hobbyUpdate.copy(
        frequency = SD.hobbyUpdate.frequency,
        `type` = SD.hobbyUpdate.`type`,
        status = SD.hobbyUpdate.status
      )
    }

    it("should throw an exception if the frequency is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyUpdate.copy(
          frequency = Some("every weekend")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Frequency", Some(InvalidFieldPayload("frequency")))
      caught shouldBe expected
    }

    it("should throw an exception if the type is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyUpdate.copy(
          `type` = Some("anti-social")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Type", Some(InvalidFieldPayload("type")))
      caught shouldBe expected
    }

    it("should throw an exception if the status is invalid") {
      val caught = intercept[InvalidFieldException] {
        SD.hobbyUpdate.copy(
          status = Some("dead")
        )
      }
      val expected = InvalidFieldException("InvalidField", "Invalid Status", Some(InvalidFieldPayload("status")))
      caught shouldBe expected
    }
  }

  describe("validating a tier upsert") {
    it("should allow the creation of a valid object") {
      TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID))
    }

    it("should throw an exception if there are more than 10 laser-donuts") {
      val caught = intercept[InvalidFieldException] {
        TierUpsert(laserDonuts = (1 to 11).map(_ => UUID.randomUUID))
      }
      val expected = InvalidFieldException("InvalidField", "Laser Donuts must be a maximum of 10 items", Some(InvalidFieldPayload("laserDonuts")))
      caught shouldBe expected
    }

    it("should throw an exception if there are less than 5 laser-donuts") {
      val caught = intercept[InvalidFieldException] {
        TierUpsert(laserDonuts = Nil)
      }
      val expected = InvalidFieldException("InvalidField", "Laser Donuts must be a minimum of 5 items", Some(InvalidFieldPayload("laserDonuts")))
      caught shouldBe expected
    }

    it("should throw an exception if there are duplicated laser-donuts") {
      val duplicatedId = UUID.randomUUID
      val caught = intercept[InvalidFieldException] {
        TierUpsert(laserDonuts = (duplicatedId :: duplicatedId :: Nil) ++ (1 to 3).map(_ => UUID.randomUUID))
      }
      val expected = InvalidFieldException("InvalidField", "Laser Donuts cannot contain duplicate entries", Some(InvalidFieldPayload("laserDonuts")))
      caught shouldBe expected
    }
  }

  describe("validating a pyramid upsert") {
    it("should allow the creation of a valid object") {
      PyramidOfImportanceUpsert(
        tiers = List(
          TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
          TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
          TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
          TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
          TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID))
        )
      )
    }

    it("should throw an exception if there are less than 5 tiers in a pyramid") {
      val caught = intercept[InvalidFieldException] {
        PyramidOfImportanceUpsert(
          tiers = List(
            TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
            TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID)),
            TierUpsert(laserDonuts = (1 to 5).map(_ => UUID.randomUUID))
          )
        )
      }
      val expected = InvalidFieldException("InvalidField", "Tiers must be a minimum of 5 items", Some(InvalidFieldPayload("tiers")))
      caught.message shouldBe "Tiers must be a minimum of 5 items"
      caught shouldBe expected
    }
  }
}
