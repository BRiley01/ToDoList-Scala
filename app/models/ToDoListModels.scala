package models

import org.joda.time.DateTime

case class ToDoList(id: Long, listName: String)
case class TaskItem(id: Long, title: String, priority: Int, DueDate: DateTime)
case class ListTasks(list: ToDoList, task: TaskItem)
