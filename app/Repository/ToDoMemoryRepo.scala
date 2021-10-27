package Repository

import models._
import scala.collection.immutable.HashMap

class ToDoMemoryRepo extends ToDoRepoTrait {
  // Store internally in HashMaps for O(1) time lookup/addition
  // "var" allows this to be a replaceable HashMap even though the Hashmap itself is immutable
  // If this were stored externally, we have a completely immutable codebase
  private var listTasks = new HashMap[ToDoList, HashMap[Long, TaskItem]]
  private var todoLists: HashMap[Long, ToDoList] = HashMap(1L -> ToDoList(1, "List1"), 2L -> ToDoList(2, "List2"))

  // this is used to test if an option is empty - could have also used "if(x.getOrElse(y) == y)" inline instead
  private def OptToBool[x](opt: Option[x]): Boolean = opt match {
    case Some(a) => true
    case None => false
  }

  // Could have stored this separately - ideally it would be an identity and determined by the DB or even a GUID
  // this would have made manually "curling" from the command prompt more difficult though.
  // This instead becomes an O(n) operation to determine the next id
  private def nextId =
    todoLists.keySet.maxOption match {
      case Some(newId) => newId+1
      case None => 1
    }

  // see above comment
  private def nextTaskId(todoList: ToDoList): Long = {
    if(!listTasks.contains(todoList))
      1
    else
      listTasks(todoList).keySet.maxOption match {
        case Some(newId) => newId+1
        case None => 1
      }
  }

  //This is straightforward and simply returns all of the values of the hashset
  override def getToDoLists:List[ToDoList] = todoLists.values.toList

  //find a specific item in the hashset, if not found, return None via option
  override def getList(listId: Long): Option[ToDoList] = {
    if(todoLists.contains(listId))
      Some(todoLists(listId))
    else
      None
  }

  // Update an existing todolist - really the only field currently updatable is the title as per the model
  // could have passed the title as an additional param instead of a model, but the model will be more extensible
  override def updateTodoList(listId: Long, updatedList: UpdateToDoList): Option[ToDoList] = {
    if(!todoLists.contains(listId))
      None
    else{
      // this is immutable, so we can't modify the hashset, instead we create a new update item
      // then we create a new list where we remove the hashset, and an additional list where we add it back in
      // I'm pretty sure that doing this on an immutable list ultimately changes it from an O(1) operation to an
      // O(n) since we need visit each element in order to rebuild the list.
      // We actually end up doing that twice. Will need to research to see if there is a better way here
      val update = todoLists(listId).copy(listName = updatedList.listName)
      todoLists -= listId
      todoLists += (update.id -> update)
      Some(todoLists(listId))
    }
  }

  //Add a new item to the todo list
  override def addTodoList(newList: NewToDoList): Option[ToDoList] = {
    //Although we're adding regardless, return an option in case we want business logic to prevent adding
    val toAdd = ToDoList(nextId, newList.listName)
    // again, this copies the hashset and adds a new item to the copied list.
    todoLists += (toAdd.id -> toAdd)
    Some(toAdd)
  }

  //This is straight forward, though needed to ensure we remove the listid from both hashsets being maintained
  override def deleteToDoList(listId: Long): Boolean = {
    if(!todoLists.contains(listId))
      false
    else
    {
      val theList = todoLists(listId)
      todoLists -= listId
      if(listTasks.contains(theList))
        listTasks -= theList
      true
    }
  }

  //This does all of the additional filtering and sorting
  // Each field is passed in as "options" so we are able to determine if it was actually presented in the query string params
  override def getTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]): Option[List[TaskItem]] = {
    if(!todoLists.contains(listId))
      None
    else {
      //Get the base tasks for the todo list
      val todoList = todoLists(listId)
      if(!listTasks.contains(todoList)) {
        return Some(List[TaskItem]())
      }
      var results = listTasks(todoList).values.toList

      //then filter that based on priority if passed
      if (OptToBool(priority)) {
        results = results.filter(t => t.priority == priority.get)
      }

      //then filter that based on completed if passed
      if (OptToBool(completed)) {
        results = results.filter(t => t.completed == completed.get)
      }

      //then sort by the orderby if passed
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

  //Returns the individual taskitem if it exists
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

  // Adds a new task to a specific list
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

  // update a task associated with a speicifc list
  override def updateTask(listId: Long, taskId: Long, task: UpdateTask): Option[TaskItem] = {
    //Greatly simplifies "if" logic, as would need to gradually chain more components
    lazy val todoList = todoLists(listId)
    lazy val listTask = listTasks(todoList)
    lazy val origTask = listTask(taskId)

    if(!(todoLists.contains(listId) && listTasks.contains(todoList) && listTask.contains(taskId)))
      None
    else{
      origTask
      //Create the updated task
      val updatedTask = TaskItem(
        id = origTask.id,
        title = task.title.getOrElse(origTask.title),
        priority = task.priority.getOrElse(origTask.priority),
        completed = task.completed.getOrElse(origTask.completed),
        dueDate = task.dueDate.getOrElse(origTask.dueDate)
      )

      // create an immutable HashMap of the id and task
      val updateTasks = listTask + (updatedTask.id -> updatedTask)

      //recreate listTasks hashSet removing the long item and adding in the new
      //By doing this in one line reduces chance of concurrent operation on Hashset
      listTasks = listTasks - todoList + (todoList -> updateTasks)
      Some(updatedTask)
    }
  }

  // remove a specific task if it exists
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
