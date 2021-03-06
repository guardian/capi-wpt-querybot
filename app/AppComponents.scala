
import config.{Config, LogConfig}
import play.api.ApplicationLoader.Context
import play.api.libs.ws.ahc.AhcWSComponents
import play.api.BuiltInComponentsFromContext
import controllers.AssetsComponents
import play.api.routing.Router
import router.Routes
import play.filters.HttpFiltersComponents

class AppComponents(context: Context)
  extends BuiltInComponentsFromContext(context) with AhcWSComponents with AssetsComponents with HttpFiltersComponents {


  val config = new Config(context.initialConfiguration)

  val logger = new LogConfig(config)
  lazy val router: Router = new Routes(httpErrorHandler, appController, healthcheckController)
  lazy val appController = new controllers.App(wsClient, controllerComponents, config)
  lazy val healthcheckController = new controllers.Healthcheck(controllerComponents)
}


