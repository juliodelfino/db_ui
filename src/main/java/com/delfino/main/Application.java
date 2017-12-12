package com.delfino.main;

import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.delfino.controller.DbController;
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

		staticFiles.location(Constants.STATIC_FILES);
		before(new SkipAuthFilter(), new RequestDataFilter());
		setupRoutes(UserController.class, DbController.class);

	}

	private static void setupRoutes(Class... controllers) throws ReflectiveOperationException {

		for (Class controllerClass : controllers) {

			Object instance = controllerClass.newInstance();
			String module = controllerClass.getSimpleName().replace("Controller", "").toLowerCase();
			module = module.equals("main") ? "" : "/" + module;
			for (Field f : controllerClass.getDeclaredFields()) {
				if (f.getType().equals(Route.class)) {

					f.setAccessible(true);
					 String name = f.getName().toLowerCase();
					 final String fName = name;
					if (!Constants.HTTP_METHODS.stream().anyMatch(m -> fName.startsWith(m))) {
					    throw new IllegalStateException(controllerClass + "." + f.getName()
                        + ": invalid route name. "
                        + "route names must start with either 'get', 'post' or 'delete'");
					}

					registerRoute(name, module, (Route)f.get(instance));
				}
			}
		}
	}

	private static void registerRoute(String name, String module, Route route) {

		String prefix = name.startsWith("get") ? "get" :
			name.startsWith("post") ? "post" : "delete";

		String fname = name.replace(prefix, "");
		fname = fname.equals("index") ? "" : "/" + fname;
		String path = module + fname;
		
        LOGGER.info("Added route " + prefix.toUpperCase() + " " + path);
        
		if (name.startsWith("get")) {
			get(path, route);
		}
		else if (name.startsWith("post")) {
			post(path, route);
		}
		else if (name.startsWith("delete")) {
			delete(path, route);
		}
	}
}
