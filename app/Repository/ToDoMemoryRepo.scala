package Repository

import models.{ListTask, NewTask, NewToDoList, TaskItem, ToDoList, UpdateToDoList}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class ToDoMemoryRepo extends ToDoRepoTrait {
  private val listTasks = new mutable.ListBuffer[ListTask]()
  private val todoLists = new mutable.ListBuffer[ToDoList]()

  todoLists += ToDoList(1, "List1")
  todoLists += ToDoList(2, "List2")
  private def nextId =
    todoLists.map(_.id).maxOption match {
      case Some(newId) => newId+1
      case None => 1
    }

  private def nextTaskId(todoList: ToDoList) =
    listTasks.filter(lt => lt.list == todoList).map(_.list.id).maxOption match {
      case Some(newId) => newId+1
      case None => 1
    }

  override def getToDoLists: ListBuffer[ToDoList] = todoLists
  override def getList(listId: Long): Option[ToDoList] = todoLists.find(l => l.id == listId)
  override def updateTodoList(listId: Long, updatedList: UpdateToDoList): Option[ToDoList] = {
    val index = todoLists.indexWhere(l => l.id == listId)
    if(index == -1) None
    todoLists(index) = todoLists(index).copy(listName = updatedList.listName)
    Some(todoLists(index))
  }

  override def addTodoList(newList: NewToDoList): Option[ToDoList] = {
    //Although we're adding regardless, return an option in case we want business logic to prevent adding
    val toAdd = ToDoList(nextId, newList.listName)
    todoLists += toAdd
    Some(toAdd)
  }

  override def deleteToDoList(listId: Long): Boolean = {
    val index = todoLists.indexWhere(l => l.id == listId)
    if(index == -1)
      false
    todoLists.remove(index)
    true
  }

  def OptToBool[x](opt: Option[x]) = opt match {
    case Some(a) => true
    case None => false
  }

  override def getTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]): Option[List[TaskItem]] = {
    var results = listTasks.filter(lt => lt.list.id == listId).map(lt => lt.task)
    if (OptToBool(priority)) {
      results = results.filter(t => t.priority == priority.get)
    }
    if(OptToBool(completed)) {
      results = results.filter(t => t.completed == completed.get)
    }
    if(OptToBool(orderby)) {
      if(orderby.get == "due-date")
        results = results.sortBy(t => t.dueDate)
      else if(orderby.get == "priority")
        results = results.sortBy(t => t.priority)
      else
        return None
    }
    if(results.isEmpty) None
    Some(results.toList)
  }

  override def addTaskToList(listId: Long, task: NewTask): Option[TaskItem] = {
    todoLists.find(l => l.id == listId) match
    {
      case Some(todoList) => {
        val newTask = TaskItem(nextTaskId(todoList), task.title, task.priority, task.completed, task.dueDate)
        listTasks += ListTask(todoList, newTask)
        Some(newTask)
      }
      case None => None
    }
  }

}
