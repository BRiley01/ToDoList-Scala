package controllers

import Repository.ToDoRepoTrait
import models._

//https://stackoverflow.com/questions/31462673/how-to-use-joda-datetime-with-play-json
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import play.api.libs.json.JodaWrites._
import play.api.libs.json.JodaReads._

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.collection.mutable

@Singleton
class ToDoListController @Inject()(val controllerComponents: ControllerComponents, repo: ToDoRepoTrait) extends BaseController {
  //The following need to be added for each model to allow json de/serialization
  implicit val todoListJson = Json.format[ToDoList]
  implicit val newToDoListJson = Json.format[NewToDoList]
  implicit val updateToDoListJson = Json.format[UpdateToDoList]
  implicit val taskItemJson = Json.format[TaskItem]
  implicit val newTaskJson = Json.format[NewTask]
  implicit val updateTaskJson = Json.format[UpdateTask]


  // GET /api/v1/todo-lists
  def getAllLists: Action[AnyContent] = Action {
    val todoLists = repo.getToDoLists
    if(todoLists.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(todoLists))
    }
  }

  // GET /api/v1/todo-lists/:listId
  def getList(listId: Long): Action[AnyContent] = Action {
    val todoList = repo.getList(listId)
    todoList match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  // POST /api/v1/todo-lists
  def addNewToDoList = Action { implicit request =>
    //Deserialize body and return "created" if valid, else BadRequest
    val jsonBody = request.body.asJson

    jsonBody
      .map {json =>
        repo.addTodoList(json.as[NewToDoList]) match {
          case Some(listItem) => Created(Json.toJson(listItem))
          case None => BadRequest
        }
      }
      .getOrElse{
        BadRequest("Expecting application/json request body")
      }
  }

  // PUT /api/v1/todo-lists/:listId
  def updateList(listId: Long) = Action { implicit request =>
    val jsonBody = request.body.asJson

    jsonBody
      .map {json =>
        repo.updateTodoList(listId, json.as[UpdateToDoList]) match {
          case Some(listItem) => Ok(Json.toJson(listItem))
          case None => BadRequest
        }
      }
      .getOrElse{
        BadRequest("Expecting application/json request body")
      }
  }

  // DELETE  /api/v1/todo-lists/:listId
  def deleteList(listId: Long) = Action {
    if(repo.deleteToDoList(listId)) Ok
    else BadRequest
  }

  //GET     /api/v1/todo-lists/:listId/tasks
  def getListTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]) = Action {
    val tasksOpt =  repo.getTasks(listId, priority, completed, orderby)
    tasksOpt match
    {
      case Some(tasks) => Ok(Json.toJson(tasks))
      case None => BadRequest
    }
  }

  //GET     /api/v1/todo-lists/:listId/tasks/:taskId
  def getListTask(listId: Long, taskId: Long) = Action {
    val taskOpt =  repo.getTask(listId, taskId)
    taskOpt match
    {
      case Some(task) => Ok(Json.toJson(task))
      case None => BadRequest
    }
  }

  //POST    /api/v1/todo-lists/:listId/tasks
  def addListTask(listId: Long) = Action { implicit request =>
    val jsonBody = request.body.asJson

    jsonBody
      .map {json =>
        val newTaskObj = json.as[NewTask]
        repo.addTaskToList(listId, newTaskObj) match {
          case Some(task) => Created(Json.toJson(task))
          case None => BadRequest
        }
      }
      .getOrElse{
        BadRequest("Expecting application/json request body")
      }
  }

  //PUT     /api/v1/todo-lists/:listId/tasks/:taskId
  def updateListTask(listId: Long, taskId: Long) = Action { implicit request =>
    val jsonBody = request.body.asJson

    jsonBody
      .map {json =>
        repo.updateTask(listId, taskId, json.as[UpdateTask]) match {
          case Some(task) => Ok(Json.toJson(task))
          case None => BadRequest
        }
      }
      .getOrElse{
        BadRequest("Expecting application/json request body")
      }
  }

  //DELETE  /api/v1/todo-lists/:listId/tasks/:taskId
  def deleteListTask(listId: Long, taskId: Long) = Action {
    if(repo.deleteTask(listId, taskId)) Ok
    else BadRequest
  }
}
