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

  /*def getById(itemId: Long): Action[AnyContent] = Action {
    val itemOption = todoList.find(i => i.id == itemId)
    itemOption match {
      case Some(item) => Ok(Json.toJson(item))
      case None => NotFound
    }
  }*/
}
