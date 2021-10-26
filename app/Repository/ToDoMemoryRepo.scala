package Repository

import models.ToDoList

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ToDoMemoryRepo extends ToDoRepoTrait {
  private val todoLists = new mutable.ListBuffer[ToDoList]()
  todoLists += ToDoList(1, "List1")
  todoLists += ToDoList(2, "List2")

  def getToDoLists: ListBuffer[ToDoList] = todoLists
}
