package com.delfino.main;

import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.delfino.controller.DbController;
import com.delfino.controller.MainController;
import com.delfino.util.ViewUtil;

import spark.Route;

public class Application {
	
	public static void main(String[] args) throws ReflectiveOperationException {
		port(3001);
		initExceptionHandler((e) -> {
			System.out.println("uh-oh");
			e.printStackTrace();
			System.exit(100);
		});

		staticFiles.location(ViewUtil.STATIC_FILES);

		setupRoutes(MainController.class, DbController.class);

	}

	private static void setupRoutes(Class... controllers) throws ReflectiveOperationException {

		for (Class controllerClass : controllers) {

			Object instance = controllerClass.newInstance();
			String module = controllerClass.getSimpleName().replace("Controller", "").toLowerCase();
			module = module.equals("main") ? "" : "/" + module;
			for (Field f : controllerClass.getDeclaredFields()) {
				if (f.getType().equals(Route.class)) {
					f.setAccessible(true);
					String name = f.getName().equals("index") ? "" : "/" + f.getName();
					get(module + name, (Route) f.get(instance));
				}
			}
		}
	}
}
