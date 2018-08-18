package com.ferris.timetable.service.conversions

import com.ferris.timetable.model.Model.TaskTypes.TaskType
import com.ferris.timetable.model.Model.{TaskTypes, TypeEnum}

object TypeResolvers {

  sealed trait TypeResolver[T <: TypeEnum] {
    def withName(name: String): T
    def toString(`type`: T): String
  }

  object TaskType extends TypeResolver[TaskTypes.TaskType] {

    import com.ferris.timetable.contract.resource.TypeFields.TaskType._

    override def withName(name: String): TaskType = name match {
      case `thread` => TaskTypes.Thread
      case `weave` => TaskTypes.Weave
      case `laserDonut` => TaskTypes.LaserDonut
      case `hobby` => TaskTypes.Hobby
      case o => throw new IllegalArgumentException(s"Invalid task type: $o")
    }

    override def toString(`type`: TaskType): String = `type` match {
      case TaskTypes.Thread => thread
      case TaskTypes.Weave => weave
      case TaskTypes.LaserDonut => laserDonut
      case TaskTypes.Hobby => hobby
      case o => throw new IllegalArgumentException(s"Invalid task type: $o")
    }
  }
}
