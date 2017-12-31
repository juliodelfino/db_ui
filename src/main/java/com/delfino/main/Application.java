package com.delfino.main;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.internalServerError;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.annotation.AppRoute;
import com.delfino.controller.AdminController;
import com.delfino.controller.DbController;
import com.delfino.controller.ErrorController;
import com.delfino.controller.UserController;
import com.delfino.filter.RequestDataFilter;
import com.delfino.filter.SkipAuthFilter;
import com.delfino.util.AppProperties;
import com.delfino.util.Constants;

import spark.Route;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws ReflectiveOperationException {
		
		port(AppProperties.getInt("port"));
		initExceptionHandler(e -> {
			LOGGER.error(e.getMessage(), e);
			System.exit(100);
		});
		internalServerError(new ErrorController().get500);

		staticFiles.location(Constants.STATIC_FILES);

		List<RouteInfo> routes = scanRoutes(UserController.class, DbController.class, AdminController.class);
		List<String> skipAuthPaths = routes.stream()
				.filter(r -> r.appRoute != null && r.appRoute.skipAuthentication())
				.map(r -> r.path).collect(Collectors.toList());
		before(new SkipAuthFilter(skipAuthPaths), new RequestDataFilter());
		
		registerRoutes(routes);

	}

	private static void registerRoutes(List<RouteInfo> routes) {
		for (RouteInfo route : routes) {

			String name = route.name;
			registerRoute(name, route.path, route.route);
		}
	}

	private static List<RouteInfo> scanRoutes(Class... controllers) throws ReflectiveOperationException {

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
