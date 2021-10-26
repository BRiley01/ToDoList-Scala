package Repository

import com.google.inject.ImplementedBy
import models._

import scala.collection.mutable.ListBuffer

@ImplementedBy(classOf[ToDoMemoryRepo])
trait ToDoRepoTrait {
  def getToDoLists: ListBuffer[ToDoList]
  def getList(listId: Long): Option[ToDoList]
  def addTodoList(newList: NewToDoList): Option[ToDoList]
  def updateTodoList(listId: Long, updatedList: UpdateToDoList): Option[ToDoList]
  def deleteToDoList(listId: Long): Boolean
  def getTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]): Option[List[TaskItem]]
  def addTaskToList(listId: Long, task: NewTask): Option[TaskItem]
}


