package models

import org.joda.time.DateTime

case class NewToDoList(listName: String)
case class UpdateToDoList(listName: String)
case class ToDoList(id: Long, listName: String)
case class TaskItem(id: Long, title: String, priority: Int, DueDate: DateTime)
case class ListTasks(list: ToDoList, task: TaskItem)
