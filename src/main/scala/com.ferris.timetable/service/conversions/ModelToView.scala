package com.ferris.timetable.service.conversions

import com.ferris.timetable.contract.resource.Resources.Out._
import com.ferris.timetable.model.Model._

object ModelToView {

  implicit class MessageConversion(message: Message) {
    def toView: MessageView = {
      MessageView(
        uuid = message.uuid,
        sender = message.sender,
        content = message.content
      )
    }
  }

  implicit class ConcreteTimeBlockConversion(timeBlock: ConcreteBlock) {
    def toView: ConcreteBlockView = {
      ConcreteBlockView(
        start = timeBlock.start,
        finish = timeBlock.finish,
        task = timeBlock.task
      )
    }
  }

  implicit class BufferTimeBlockConversion(timeBlock: BufferBlock) {
    def toView: BufferBlockView = {
      BufferBlockView(
        start = timeBlock.start,
        finish = timeBlock.finish,
        firstTask = timeBlock.firstTask,
        secondTask = timeBlock.secondTask
      )
    }
  }

  implicit class TimeBlockConversion(timeBlock: TimeBlock) {
    def toView: TimeBlockView = timeBlock match {
      case concrete: ConcreteBlock => concrete.toView
      case buffer: BufferBlock => buffer.toView
    }
  }

  implicit class TimetableTemplateConversion(timetableTemplate: TimetableTemplate) {
    def toView: TimetableTemplateView = {
      TimetableTemplateView(
        blocks = timetableTemplate.blocks.map(_.toView)
      )
    }
  }

  implicit class RoutineConversion(routine: Routine) {
    def toView: RoutineView = {
      RoutineView(
        uuid = routine.uuid,
        name = routine.name,
        monday = routine.monday.toView,
        tuesday = routine.monday.toView,
        wednesday = routine.monday.toView,
        thursday = routine.monday.toView,
        friday = routine.monday.toView,
        saturday = routine.monday.toView,
        sunday = routine.monday.toView
      )
    }
  }

  implicit class TimetableConversion(timetable: Timetable) {
    def toView: TimetableView = {
      TimetableView(
        date = timetable.date,
        blocks = timetable.blocks.map(_.toView)
      )
    }
  }
}
