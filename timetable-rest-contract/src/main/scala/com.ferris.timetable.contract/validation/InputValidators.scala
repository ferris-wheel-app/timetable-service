package com.ferris.timetable.contract.validation

import com.ferris.microservice.exceptions.ApiExceptions.{InvalidFieldException, InvalidFieldPayload}
import com.ferris.planning.contract.resource.Resources.In._
import com.ferris.planning.contract.resource.TypeFields._

object InputValidators {

  val TypeField = "type"
  val BacklogItemsField = "backlogItems"
  val GraduationField = "graduation"
  val StatusField = "status"
  val FrequencyField = "frequency"
  val LaserDonutsField = "laserDonuts"
  val TiersField = "tiers"

  val MaxGoalBacklogItemsSize = 10
  val MaxTierSize = 10
  val MinTierSize = 5
  val MinTopTierSize = 5
  val MinPyramidSize = 5

  def checkValidity(backlogItemCreation: BacklogItemCreation): Unit = {
    checkField(BacklogItemType.values.contains(backlogItemCreation.`type`), TypeField)
  }

  def checkValidity(backlogItemUpdate: BacklogItemUpdate): Unit = {
    backlogItemUpdate.`type`.foreach(`type` => checkField(BacklogItemType.values.contains(`type`), TypeField))
  }

  def checkValidity(goalCreation: GoalCreation): Unit = {
    checkMaxSize(goalCreation.backlogItems, BacklogItemsField, MaxGoalBacklogItemsSize)
    checkForDuplication(goalCreation.backlogItems, BacklogItemsField)
    checkField(GraduationType.values.contains(goalCreation.graduation), GraduationField)
    checkField(GoalStatus.values.contains(goalCreation.status), StatusField)
  }

  def checkValidity(goalUpdate: GoalUpdate): Unit = {
    goalUpdate.backlogItems.foreach(backlogItems => checkMaxSize(backlogItems, BacklogItemsField, MaxGoalBacklogItemsSize))
    goalUpdate.backlogItems.foreach(backlogItems => checkForDuplication(backlogItems, BacklogItemsField))
    goalUpdate.graduation.foreach(graduation => checkField(GraduationType.values.contains(graduation), GraduationField))
    goalUpdate.status.foreach(status => checkField(GoalStatus.values.contains(status), StatusField))
  }

  def checkValidity(threadCreation: ThreadCreation): Unit = {
    checkField(Status.values.contains(threadCreation.status), StatusField)
  }

  def checkValidity(threadUpdate: ThreadUpdate): Unit = {
    threadUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(weaveCreation: WeaveCreation): Unit = {
    checkField(WeaveType.values.contains(weaveCreation.`type`), TypeField)
    checkField(Status.values.contains(weaveCreation.status), StatusField)
  }

  def checkValidity(weaveUpdate: WeaveUpdate): Unit = {
    weaveUpdate.`type`.foreach(`type` => checkField(WeaveType.values.contains(`type`), TypeField))
    weaveUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(laserDonutCreation: LaserDonutCreation): Unit = {
    checkField(DonutType.values.contains(laserDonutCreation.`type`), TypeField)
    checkField(Status.values.contains(laserDonutCreation.status), StatusField)
  }

  def checkValidity(laserDonutUpdate: LaserDonutUpdate): Unit = {
    laserDonutUpdate.`type`.foreach(`type` => checkField(DonutType.values.contains(`type`), TypeField))
    laserDonutUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(portionCreation: PortionCreation): Unit = {
    checkField(Status.values.contains(portionCreation.status), StatusField)
  }

  def checkValidity(portionUpdate: PortionUpdate): Unit = {
    portionUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(todoCreation: TodoCreation): Unit = {
    checkField(Status.values.contains(todoCreation.status), StatusField)
  }

  def checkValidity(todoUpdate: TodoUpdate): Unit = {
    todoUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(hobbyCreation: HobbyCreation): Unit = {
    checkField(HobbyFrequency.values.contains(hobbyCreation.frequency), FrequencyField)
    checkField(HobbyType.values.contains(hobbyCreation.`type`), TypeField)
    checkField(Status.values.contains(hobbyCreation.status), StatusField)
  }

  def checkValidity(hobbyUpdate: HobbyUpdate): Unit = {
    hobbyUpdate.frequency.foreach(frequency => checkField(HobbyFrequency.values.contains(frequency), FrequencyField))
    hobbyUpdate.`type`.foreach(`type` => checkField(HobbyType.values.contains(`type`), TypeField))
    hobbyUpdate.status.foreach(status => checkField(Status.values.contains(status), StatusField))
  }

  def checkValidity(tierCreation: TierUpsert): Unit = {
    checkMaxSize(tierCreation.laserDonuts, LaserDonutsField, MaxTierSize)
    checkMinSize(tierCreation.laserDonuts, LaserDonutsField, MinTierSize)
    checkForDuplication(tierCreation.laserDonuts, LaserDonutsField)
  }

  def checkValidity(pyramidCreation: PyramidOfImportanceUpsert): Unit = {
    checkMinSize(pyramidCreation.tiers, TiersField, MinPyramidSize)
  }

  private def checkMaxSize[T](list: Seq[T], name: String, max: Int): Unit = {
    checkField(list.size <= max, name, s"${camelCaseToSpaced(name)} must be a maximum of $max items")
  }

  private def checkMinSize[T](list: Seq[T], name: String, min: Int): Unit = {
    checkField(list.size >= min, name, s"${camelCaseToSpaced(name)} must be a minimum of $min items")
  }

  private def checkForDuplication[A](ls: Seq[A], name: String): Unit = {
    checkField(ls.toSet.size == ls.size, name, s"${camelCaseToSpaced(name)} cannot contain duplicate entries")
  }

  private def checkField(cond: Boolean, name: String): Unit = {
    val displayedName: String = camelCaseToSpaced(name)
    if (!cond) throw InvalidFieldException("InvalidField", s"Invalid $displayedName", Some(InvalidFieldPayload(name)))
  }

  private def checkField(cond: Boolean, name: String, msg: String): Unit = {
    if (!cond) throw InvalidFieldException("InvalidField", msg, Some(InvalidFieldPayload(name)))
  }

  private def camelCaseToSpaced(s: String): String = {
    require(s.length > 0)
    val tail = s.substring(1).foldRight(List.empty[Char]){ (char, arr) =>
      if (char.isUpper) ' ' :: char :: arr
      else char :: arr
    }
    s.charAt(0).toUpper :: tail mkString ""
  }
}
