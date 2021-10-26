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
}
