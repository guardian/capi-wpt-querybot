package controllers

import java.io.File
import java.nio.file.Paths

import com.amazonaws.services.s3.model.{ObjectMetadata, PutObjectRequest}
import config.Config
import models._
import play.api.Logger
import play.api.libs.Files
import play.api.libs.json.{Json, OWrites}
import play.api.libs.ws.WSClient
import play.api.mvc._
import services.S3
import services.Querybot

class App(val wsClient: WSClient,
  val controllerComponents: ControllerComponents,
  val config: Config) extends BaseController {

  def index(): Action[AnyContent] = Action {
    Logger.info(s"I am the ${config.appName}")

      Ok(views.html.index())
  }

  def runBot(): Action[AnyContent] = Action {
    Querybot.run(Array())
    Ok("Running the bot")
  }



}
