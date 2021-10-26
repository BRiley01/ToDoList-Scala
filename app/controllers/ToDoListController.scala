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
}
