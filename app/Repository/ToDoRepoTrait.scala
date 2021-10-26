package Repository

import com.google.inject.ImplementedBy
import models.ToDoList

import scala.collection.mutable.ListBuffer

@ImplementedBy(classOf[ToDoMemoryRepo])
trait ToDoRepoTrait {
  def getToDoLists: ListBuffer[ToDoList]
}
