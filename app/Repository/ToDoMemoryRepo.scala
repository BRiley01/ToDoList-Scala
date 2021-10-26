package Repository

import models.{NewToDoList, ToDoList}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Try

class ToDoMemoryRepo extends ToDoRepoTrait {
  private val todoLists = new mutable.ListBuffer[ToDoList]()
  todoLists += ToDoList(1, "List1")
  todoLists += ToDoList(2, "List2")
  private def nextId =
    todoLists.map(_.id).maxOption match {
      case Some(newId) => newId+1
      case None => 1
    }


  override def getToDoLists: ListBuffer[ToDoList] = todoLists
  override def getList(listId: Long): Option[ToDoList] = todoLists.find(l => l.id == listId)
  override def addTodoList(newList: NewToDoList): Option[ToDoList] = {
    //Although we're adding regardless, return an option in case we want business logic to prevent adding
    val toAdd = ToDoList(nextId, newList.listName)
    todoLists += toAdd
    Some(toAdd)
  }
}
