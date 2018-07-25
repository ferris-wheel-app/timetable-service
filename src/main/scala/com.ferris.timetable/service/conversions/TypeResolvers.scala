package com.ferris.timetable.service.conversions

import com.ferris.timetable.contract.resource.TypeFields
import com.ferris.timetable.model.Model.DaysOfTheWeek.DayOfTheWeek
import com.ferris.timetable.model.Model._

object TypeResolvers {

  sealed trait TypeResolver[T <: TypeEnum] {
    def withName(name: String): T
    def toString(`type`: T): String
  }

  object DayOfTheWeek extends TypeResolver[DaysOfTheWeek.DayOfTheWeek] {
    import TypeFields.DayOfTheWeek._

    override def withName(name: String): DayOfTheWeek = name match {
      case `monday` => DaysOfTheWeek.Monday
      case `tuesday` => DaysOfTheWeek.Tuesday
      case `wednesday` => DaysOfTheWeek.Wednesday
      case `thursday` => DaysOfTheWeek.Thursday
      case `friday` => DaysOfTheWeek.Friday
      case `saturday` => DaysOfTheWeek.Saturday
      case `sunday` => DaysOfTheWeek.Sunday
      case o => throw new IllegalArgumentException(s"Invalid day of the week: $o")
    }

    override def toString(`type`: DayOfTheWeek): String = `type` match {
      case DaysOfTheWeek.Monday => monday
      case DaysOfTheWeek.Tuesday => tuesday
      case DaysOfTheWeek.Wednesday => wednesday
      case DaysOfTheWeek.Thursday => thursday
      case DaysOfTheWeek.Friday => friday
      case DaysOfTheWeek.Saturday => saturday
      case DaysOfTheWeek.Sunday => sunday
      case o => throw new IllegalArgumentException(s"Invalid day of the week: $o")
    }
  }
}
