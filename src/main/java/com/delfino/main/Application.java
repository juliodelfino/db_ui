package com.delfino.main;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.internalServerError;
import static spark.Spark.notFound;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.io.File;
import java.lang.reflect.Field;
import java.net.NoRouteToHostException;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.annotation.AppRoute;
import com.delfino.controller.ErrorController;
import com.delfino.filter.RequestDataFilter;
import com.delfino.filter.RequireAdminFilter;
import com.delfino.filter.SkipAuthFilter;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

import spark.Route;
import spark.Spark;
import spark.utils.IOUtils;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws ReflectiveOperationException {
		
		port(AppProperties.getInt("port"));
		initExceptionHandler(e -> {
			LOGGER.error(e.getMessage(), e);
			System.exit(100);
		});
		ErrorController errorHandlers = new ErrorController();
		internalServerError(errorHandlers.internalServerError);
		notFound(errorHandlers.notFound);
		exception(NoRouteToHostException.class, (ex, req, res) -> {
			try {
				LOGGER.error(ex.getMessage(), ex);
				res.body(errorHandlers.notFound.handle(req, res).toString());
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
		});
		exception(Exception.class, (ex, req, res) -> {
			try {
				LOGGER.error(ex.getMessage(), ex);
				req.attribute("exception", ex);
				res.body(errorHandlers.internalServerError.handle(req, res).toString());
			} catch (Exception e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
		});

		staticFiles.location(Constants.STATIC_FILES);

		List<Class> controllerClasses = Stream.of(AppProperties.get("controllers").split(","))
			.map(ctrlr ->  {
				try {
				    return Class.forName("com.delfino.controller." + ctrlr + "Controller");
				} catch (Exception ex) {
					return null;
				}
			}).filter(cl -> cl != null).collect(Collectors.toList());
		List<RouteInfo> routes = scanRoutes(controllerClasses);
		List<String> skipAuthPaths = routes.stream()
				.filter(r -> r.appRoute != null && r.appRoute.skipAuthentication())
				.map(r -> r.path).collect(Collectors.toList());
		List<String> adminPaths = routes.stream()
				.filter(r -> r.appRoute != null && r.appRoute.requireAdmin())
				.map(r -> r.path).collect(Collectors.toList());
		before(new SkipAuthFilter(skipAuthPaths), new RequireAdminFilter(adminPaths),
				new RequestDataFilter());
		
		registerRoutes(routes);
		registerRoute("get", "/", (req, res) -> { 
			res.redirect("/db"); return null; });
		get("/favicon.ico", "image/x-icon", 
				(req, res) -> IOUtils.toString(Spark.class.getResourceAsStream("/public/assets/images/favicon.ico")));
	
		String dataDir = new File(AppProperties.get("data_dir")).getAbsolutePath();
		AppProperties.getInstance().put("data_dir_absolute", dataDir);
		
		Reflections reflections = new Reflections();
		Set<Class<? extends Driver>> drivers = reflections.getSubTypesOf(Driver.class);
		AppProperties.getInstance().put("jdbc_drivers", drivers);
	}

	private static void registerRoutes(List<RouteInfo> routes) {
		for (RouteInfo route : routes) {

			registerRoute(route.name, route.path, route.route);
		}
	}

	private static List<RouteInfo> scanRoutes(List<Class> controllers) throws ReflectiveOperationException {

		List<RouteInfo> routes = new ArrayList<>();
		for (Class controllerClass : controllers) {

			Object instance = controllerClass.newInstance();
			String module = controllerClass.getSimpleName().replace("Controller", "").toLowerCase();
			module = module.equals("main") ? "" : "/" + module;
			for (Field f : controllerClass.getDeclaredFields()) {
				if (f.getType().equals(Route.class)) {
					
					 String name = f.getName().toLowerCase();
					 final String fName = name;
					if (!Constants.HTTP_METHODS.stream().anyMatch(m -> fName.startsWith(m))) {
						throw new IllegalStateException(controllerClass + "." 
								+ f.getName() + ": invalid route name. "
								+ "route names must start with either 'get', 'post' or 'delete'");
					}
					AppRoute rInfo = f.getDeclaredAnnotation(AppRoute.class);
					String path = createPath(module, name);
					RouteInfo routeInfo = new RouteInfo(name, path, (Route)f.get(instance), rInfo);
					routes.add(routeInfo);
				}
			}
		}
		return routes;
	}
	
	private static String createPath(String module, String methodName) {
		
		String prefix = methodName.startsWith("get") ? "get" :
			methodName.startsWith("post") ? "post" : "delete";

		String fname = methodName.replace(prefix, "");
		fname = fname.equals("index") ? "" : "/" + fname;
		return module + fname;
	}

	private static void registerRoute(String name, String path, Route route) {

		String prefix = name.startsWith("get") ? "get" :
			name.startsWith("post") ? "post" : "delete";
        
		if (name.startsWith("get")) {
			get(path, route);
		}
		else if (name.startsWith("post")) {
			post(path, route);
		}
		else if (name.startsWith("delete")) {
			delete(path, route);
		}
        LOGGER.info("Added route " + prefix.toUpperCase() + " " + path);
	}
	
	static class RouteInfo {
		String name;
		String path;
		Route route;
		AppRoute appRoute;
		
		public RouteInfo(String name, String path, Route route, AppRoute appRoute) {
			this.name = name;
			this.path = path;
			this.route = route;
			this.appRoute = appRoute;
		}
	}
}
