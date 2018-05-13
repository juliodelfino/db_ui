# Database UI Viewer [![Build Status](https://travis-ci.org/juliodelfino/db_ui.svg?branch=master)](https://travis-ci.org/juliodelfino/db_ui) [![Coverage Status](https://coveralls.io/repos/github/juliodelfino/db_ui/badge.svg?branch=master)](https://coveralls.io/github/juliodelfino/db_ui?branch=master)

The most lightweight, high-speed, highly configurable, web-based database client using Java's Spark Web,
Velocity's HTML templating, and the Ruby on Rails framework concepts. It supports virtually all types of databases, including MySQL, PostgreSQL, Microsoft SQL, Phoenix, and Ignite Cache, among others (as long as you can provide the necessary JDBC driver jar files to /lib folder).
Take note, this is only a DATABASE VIEWER, and not a DATABASE ADMIN UI! Unless if you're an admin :)

![Home Page](https://raw.githubusercontent.com/juliodelfino/db_ui/master/wiki/db-info-page.png)

## High speed
This application boots up in around 1000 milliseconds!

## Technologies used
- Spark Web
- Velocity Template
- Gson
- Bootstrap
- jQuery

## Setup

### Unix
- Download db-ui.tar.gz file (less than 20MB).
- Extract using this command: gtar -xvzf db-ui.tar.gz
- Download the JDBC driver jar files you need and place it in /lib folder. Some of them may have already been there (actually, only MySQL library is in there).
- Update the config/application.properties. Change the data_dir to point to a writeable directory.
- Run the application via run-app.sh
- Use the default 'root' account to manage the users. Password is 'welcome'.
- Enjoy!

### Windows
- Download db-ui.tar.gz file (less than 20MB).
- Use 7zip to extract this tar.gz file.
- Download the JDBC driver jar files you need and place it in /lib folder. Some of them may have already been there (actually, only MySQL library is in there).
- Update the config/application.properties. Change the data_dir to point to a writeable directory.
- Run the application via run-app.bat
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

## Development/Extending Functionalities

### Ruby on Rails framework concepts
First of all, this is a 100% Java project. 
But I really find Rails framework so easy to use, I can build a running website in less than an hour! If you're familiar with coding in Ruby on Rails, you will easily get to know how this application is being built using their concepts.

Example: Creating a Database page. Note that the related files/resources are consistently linked with 'db' keyword.
Relevant files:
  - Route: http://website:3001/db
  - Maps to Controller: DbController
    - Maps to Route: getIndex
    - Dev must map to 'db/index.html'
  - Maps to JsonDb.getDatabases()
  - Maps to View: db/index.html
    - Automatically adds the corresponding db.css and db_index.css, if they exist
    - Also auto-adds the corresponding db.js and db_index.js, if they exist
  


