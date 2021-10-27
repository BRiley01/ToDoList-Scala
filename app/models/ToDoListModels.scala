package models

import org.joda.time.DateTime


case class NewToDoList(listName: String)
case class UpdateToDoList(listName: String)
case class ToDoList(id: Long, listName: String)

//Datetime format for joda: YYYY-MM-DDThh:mm:ss-zz:00  (where zz= timezone offset)

case class TaskItem(id: Long, title: String, priority: Int, completed: Boolean, dueDate: DateTime)
case class NewTask(title: String, priority: Int, completed: Boolean, dueDate: DateTime)
case class UpdateTask(title: Option[String], priority: Option[Int], completed: Option[Boolean], dueDate: Option[DateTime])