package controllers

import Repository.ToDoRepoTrait
import models._

import javax.inject._
import play.api.mvc._
import play.api.libs.json._

import scala.collection.mutable

@Singleton
class ToDoListController @Inject()(val controllerComponents: ControllerComponents, repo: ToDoRepoTrait) extends BaseController {
  implicit val todoListJson = Json.format[ToDoList]
  implicit val newToDoListJson = Json.format[NewToDoList]
  implicit val updateToDoListJson = Json.format[UpdateToDoList]

  def getAll: Action[AnyContent] = Action {
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
}
