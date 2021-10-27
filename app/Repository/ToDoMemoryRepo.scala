package Repository

import models._
import scala.collection.immutable.HashMap

class ToDoMemoryRepo extends ToDoRepoTrait {
  private var listTasks = new HashMap[ToDoList, HashMap[Long, TaskItem]]
  private var todoLists: HashMap[Long, ToDoList] = HashMap(1L -> ToDoList(1, "List1"), 2L -> ToDoList(2, "List2"))

  private def OptToBool[x](opt: Option[x]): Boolean = opt match {
    case Some(a) => true
    case None => false
  }

  private def nextId =
    todoLists.keySet.maxOption match {
      case Some(newId) => newId+1
      case None => 1
    }

  private def nextTaskId(todoList: ToDoList): Long = {
    if(!listTasks.contains(todoList))
      1
    else
      listTasks(todoList).keySet.maxOption match {
        case Some(newId) => newId+1
        case None => 1
      }
  }

  override def getToDoLists:List[ToDoList] = todoLists.values.toList
  override def getList(listId: Long): Option[ToDoList] = {
    if(todoLists.contains(listId))
      Some(todoLists(listId))
    else
      None
  }
  override def updateTodoList(listId: Long, updatedList: UpdateToDoList): Option[ToDoList] = {
    if(!todoLists.contains(listId))
      None
    else{
      val update = todoLists(listId).copy(listName = updatedList.listName)
      todoLists -= listId
      todoLists += (update.id -> update)
      Some(todoLists(listId))
    }
  }

  override def addTodoList(newList: NewToDoList): Option[ToDoList] = {
    //Although we're adding regardless, return an option in case we want business logic to prevent adding
    val toAdd = ToDoList(nextId, newList.listName)
    todoLists += (toAdd.id -> toAdd)
    Some(toAdd)
  }

  override def deleteToDoList(listId: Long): Boolean = {
    if(!todoLists.contains(listId))
      false
    else
    {
      todoLists -= listId
      true
    }
  }

  override def getTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]): Option[List[TaskItem]] = {
    if(!todoLists.contains(listId))
      None
    else {
      val todoList = todoLists(listId)
      if(!listTasks.contains(todoList)) {
        return Some(List[TaskItem]())
      }
      var results = listTasks(todoList).values.toList
      if (OptToBool(priority)) {
        results = results.filter(t => t.priority == priority.get)
      }
      if (OptToBool(completed)) {
        results = results.filter(t => t.completed == completed.get)
      }
      if (OptToBool(orderby)) {
        if (orderby.get == "due-date")
          results = results.sortBy(t => t.dueDate)
        else if (orderby.get == "priority")
          results = results.sortBy(t => t.priority)
        else
          return None
      }
      Some(results)
    }
  }

  override def getTask(listId: Long, taskId: Long): Option[TaskItem] = {
    if(!todoLists.contains(listId))
      None
    else {
      val todoList = todoLists(listId)
      if(listTasks.contains(todoList) && listTasks(todoList).contains(taskId))
        Some(listTasks(todoList)(taskId))
      else
        None
    }
  }

  override def addTaskToList(listId: Long, task: NewTask): Option[TaskItem] = {
    if(!todoLists.contains(listId))
      None
    else {
      //Get todoList by ID -- we already know it exists from the line above
      //Technically between the contains and the lookup, item could be removed.
      //If that were the case it will throw an exception and I'm fine with that for now
      val todoList = todoLists(listId)

      //Create a new task by getting the highest prior taskID in the list
      val newTask = TaskItem(nextTaskId(todoList), task.title, task.priority, task.completed, task.dueDate)

      // Get the old list of tasks, remove it from the hashmap
      // re-add a copy of the oldlist with the new item added (maintains immutability of list)
      // Done in one line to eliminate need for var
      val updateTasks = (if (listTasks.contains(todoList)) listTasks(todoList) else HashMap[Long, TaskItem]()) + (newTask.id -> newTask)

      //By doing this in one line reduces chance of concurrent operation on Hashset
      listTasks = listTasks - todoList + (todoList -> updateTasks)
      Some(newTask)
    }
  }

  override def updateTask(listId: Long, taskId: Long, task: UpdateTask): Option[TaskItem] = {
    //Greatly simplifies "if" logic, as would need to gradually chain more components
    lazy val todoList = todoLists(listId)
    lazy val listTask = listTasks(todoList)
    lazy val origTask = listTask(taskId)

    if(!(todoLists.contains(listId) && listTasks.contains(todoList) && listTask.contains(taskId)))
      None
    else{
      origTask
      val updatedTask = TaskItem(
        id = origTask.id,
        title = task.title.getOrElse(origTask.title),
        priority = task.priority.getOrElse(origTask.priority),
        completed = task.completed.getOrElse(origTask.completed),
        dueDate = task.dueDate.getOrElse(origTask.dueDate)
      )

      val updateTasks = listTask + (updatedTask.id -> updatedTask)

      //By doing this in one line reduces chance of concurrent operation on Hashset
      listTasks = listTasks - todoList + (todoList -> updateTasks)
      Some(updatedTask)
    }
  }

  override def deleteTask(listId: Long, taskId: Long): Boolean = {
    //Greatly simplifies "if" logic, as would need to gradually chain more components
    lazy val todoList = todoLists(listId)
    lazy val listTask = listTasks(todoList)
    if(!(todoLists.contains(listId) && listTasks.contains(todoList) && listTask.contains(taskId)))
      false
    else {
      val updateTasks = listTask - taskId
      //By doing this in one line reduces chance of concurrent operation on Hashset
      listTasks = listTasks - todoList + (todoList -> updateTasks)
      true
    }

  }
}
