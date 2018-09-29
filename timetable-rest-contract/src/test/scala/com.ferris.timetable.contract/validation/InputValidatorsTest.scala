package com.ferris.timetable.contract.validation

import java.time.LocalTime

import com.ferris.microservice.exceptions.ApiExceptions.{InvalidFieldException, InvalidFieldPayload}
import com.ferris.timetable.contract.sample.{SampleData => SD}
import org.scalatest.{Assertions, FunSpec, Matchers}

class InputValidatorsTest extends FunSpec with Matchers with Assertions {

  describe("validating a timetable template creation") {
    it("should allow the creation of a valid object") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      SD.timetableTemplateCreation.copy(
        blocks = SD.timeBlockTemplateCreation.copy(
          start = firstStartTime,
          finish = firstFinishTime
        ) :: SD.timeBlockTemplateCreation.copy(
          start = secondStartTime,
          finish = secondFinishTime
        ) :: SD.timeBlockTemplateCreation.copy(
          start = thirdStartTime,
          finish = thirdFinishTime
        ) :: Nil
      )
    }

    it("should throw an exception if the time-blocks are not in order") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            start = secondStartTime,
            finish = secondFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should be provided in order and not contain any gaps or overlaps",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }

    it("should throw an exception if there are gaps between time-blocks") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime.plusHours(1), firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = secondStartTime,
            finish = secondFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should be provided in order and not contain any gaps or overlaps",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }

    it("should throw an exception if there are overlaps between time-blocks") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime.minusHours(1L), firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = secondStartTime,
            finish = secondFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should be provided in order and not contain any gaps or overlaps",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }

    it("should throw an exception if the total duration of the combined time-blocks is less than 16 hours.") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(8, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(5))

      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = secondStartTime,
            finish = secondFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should in total be at least 12 hours and no more than 16 hours",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }

    it("should throw an exception if the total duration of the combined time-blocks is more than 12 hours.") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(3, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(3))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(3))

      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = SD.timeBlockTemplateCreation.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = secondStartTime,
            finish = secondFinishTime
          ) :: SD.timeBlockTemplateCreation.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should in total be at least 12 hours and no more than 16 hours",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }

    it("should throw an exception if time-blocks are not provided") {
      val caught = intercept[InvalidFieldException] {
        SD.timetableTemplateCreation.copy(
          blocks = Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Time-blocks should in total be at least 12 hours and no more than 16 hours",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }
  }

  describe("validating a time-block template creation") {
    it("should allow the creation of a valid object") {
      SD.timeBlockTemplateCreation.copy(
        start = LocalTime.now,
        finish = LocalTime.now.plusHours(1L)
      )
    }

    it("should throw an exception if the start-time occurs after the finish-time") {
      val caught = intercept[InvalidFieldException] {
        SD.timeBlockTemplateCreation.copy(
          start = LocalTime.now.plusHours(1L),
          finish = LocalTime.now
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "The start-time should occur before the finish-time",
        Some(InvalidFieldPayload("start"))
      )

      caught shouldBe expected
    }
  }

  describe("validating a task template creation") {
    it("should allow the creation of a valid object") {
      SD.taskTemplateCreation.copy(`type` = "thread")
      SD.taskTemplateCreation.copy(`type` = "weave")
      SD.taskTemplateCreation.copy(`type` = "laser_donut")
      SD.taskTemplateCreation.copy(`type` = "hobby")
    }

    it("should throw an exception if the task-type is not valid") {
      val caught = intercept[InvalidFieldException] {
        SD.taskTemplateCreation.copy(`type` = "mischief")
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Invalid Type",
        Some(InvalidFieldPayload("type"))
      )

      caught shouldBe expected
    }
  }

  describe("validation a timetable update") {
    it("should allow the creation of a valid object") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      SD.timetableUpdate.copy(
        blocks = SD.scheduledTimeBlockUpdate.copy(
          start = firstStartTime,
          finish = firstFinishTime
        ) :: SD.scheduledTimeBlockUpdate.copy(
          start = secondStartTime,
          finish = secondFinishTime
        ) :: SD.scheduledTimeBlockUpdate.copy(
          start = thirdStartTime,
          finish = thirdFinishTime
        ) :: Nil
      )
    }

    it("should throw an exception if any of the time-blocks are invalid") {
      val (firstStartTime, firstFinishTime) = (LocalTime.of(0, 0), LocalTime.of(4, 0))
      val (secondStartTime, secondFinishTime) = (firstFinishTime, firstFinishTime.plusHours(4))
      val (thirdStartTime, thirdFinishTime) = (secondFinishTime, secondFinishTime.plusHours(4))

      val caught = intercept[InvalidFieldException] {
        SD.timetableUpdate.copy(
          blocks = SD.scheduledTimeBlockUpdate.copy(
            start = firstStartTime,
            finish = firstFinishTime
          ) :: SD.scheduledTimeBlockUpdate.copy(
            start = secondFinishTime,
            finish = secondStartTime
          ) :: SD.scheduledTimeBlockUpdate.copy(
            start = thirdStartTime,
            finish = thirdFinishTime
          ) :: Nil
        )
      }
      val expected = InvalidFieldException(
        "InvalidField",
        "Invalid time-blocks have been provided",
        Some(InvalidFieldPayload("blocks"))
      )

      caught shouldBe expected
    }
  }
}
