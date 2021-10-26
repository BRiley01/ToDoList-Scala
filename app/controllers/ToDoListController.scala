package controllers

import Repository.ToDoRepoTrait
import models._
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
  implicit val todoListJson = Json.format[ToDoList]
  implicit val newToDoListJson = Json.format[NewToDoList]
  implicit val updateToDoListJson = Json.format[UpdateToDoList]
  implicit val taskItemJson = Json.format[TaskItem]
  implicit val newTaskJson = Json.format[NewTask]


  def getAllLists: Action[AnyContent] = Action {
    val todoLists = repo.getToDoLists
    if(todoLists.isEmpty) {
      NoContent
    } else {
      Ok(Json.toJson(todoLists))
    }
  }

  def getList(listId: Long): Action[AnyContent] = Action {
    val todoList = repo.getList(listId)
    todoList match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }

  def addNewToDoList = Action { implicit request =>
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

  def deleteList(listId: Long) = Action {
    if(repo.deleteToDoList(listId)) Ok
    else BadRequest
  }

  def getListTasks(listId: Long, priority: Option[Int], completed: Option[Boolean], orderby: Option[String]) = Action {
    val tasksOpt =  repo.getTasks(listId, priority, completed, orderby)
    tasksOpt match
    {
      case Some(tasks) => {
        if(tasks.isEmpty) {
          NoContent
        } else {
          Ok(Json.toJson(tasks))
        }
      }
      case None => NoContent
    }
  }

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
}
