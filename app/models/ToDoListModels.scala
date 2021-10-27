package models

import org.joda.time.DateTime


case class NewToDoList(listName: String)
case class UpdateToDoList(listName: String)
case class ToDoList(id: Long, listName: String)

case class TaskItem(id: Long, title: String, priority: Int, completed: Boolean, dueDate: Option[DateTime])
case class NewTask(title: String, priority: Int, completed: Boolean, dueDate: Option[DateTime])
