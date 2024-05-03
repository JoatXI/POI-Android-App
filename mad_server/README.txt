Please install with:

npm install

The package.json contains the two dependencies, Express and better-sqlite3.
POIs are stored on a server-side database.

Start with:

node app.mjs

The server contains two endpoints:

- /pois/all : returns JSON of all pois. (GET)

- /poi/create : creates a new poi using the POST data sent to it. Returns plain text containing the allocated ID.

The use of IDs might help avoid duplicate POIs within your app.
