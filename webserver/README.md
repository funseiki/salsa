# Salsa Web Server
The web server and client code for the salsa project

##Usage:

    Start the web server. Listens on port 8080 (daemon should be running at this point)
    > node server.js

##Build/update:

    - Do the following once
        > npm install bower -g (if bower is not installed already)

        - create a config/development.js file (should match config/template.js)
            > cp config/template.js config/development.js

    - To install front end packages run
        > bower install
    - These packages will install to ui/assets/dependencies/
    - To update the packages modify the bower.json file

    - To install node.js packages run
        > npm install
    - These packages will install to node_modules
    - To update the server side packages modify the package.json file
