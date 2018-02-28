# Database UI Viewer

The most lightweight, high-speed, highly configurable, web-based database viewer using Java's Spark Web,
Velocity's HTML templating, and the Ruby on Rails framework concepts.
Take note, this is only a DATABASE VIEWER, and not a DATABASE ADMIN UI!

## High speed
This application boots up in around 1000 milliseconds!

## Ruby on Rails framework concepts
Rails framework is so easy to use, you can build a running website in less than an hour! If you're familiar with coding in Ruby on Rails, you will easily get to know how this application is being built using their concepts.

## Technologies used
- Spark Web
- Velocity Template
- Gson
- Bootstrap
- jQuery

## Setup
- Download db-ui.tar.gz file (8MB).
- Extract using this command: gtar -xvzf db-ui.tar.gz
- Download the JDBC driver jar files you need and place it in /lib folder. Some of them may have already been there (actually, just MySQL library).
- Update the config/application.properties. Change the data_dir to point to a writeable directory.
- Run the application via run-app.sh
- Use the default 'root' account to manage the users. Password is 'welcome'.
- Enjoy!

## Configuration
- The following can be changed via config/application.properties
  - Web application name
  - Port
  - Data directory (for storing data.json)
  - Controllers to expose (must correspond to the available ControllerBase classes)
- HTML, JS and CSS files can also be modified. These are located under config/public.
- Log configuration file is log4j.xml.
