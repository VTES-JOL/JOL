# Requirements
* Java 11+
* Maven 3+

## Java
Install Java 11+ as per your operating system

## Maven
### Build
`./mvnw.cmd clean package`

### Run
#### Environment Variables
* JOL_DATA ( required ) - points to the game data, example data is available at src/test/resources/data
* ENABLE_CAPTCHA - disable captcha for local development if required, default: true
* JOL_RECAPTCHA_KEY - required for captcha - developer key
* JOL_RECAPTCHA_SECRET - required for captcha - developer secret
* DISCORD_API_VERSION - nuff said, default: 6
* DISCORD_BOT_TOKEN - required for discord integration, developer api token
* DISCORD_PING_CHANNEL_ID - required for discord integration, channel id to post pings
* TYPE - some visual changes to distinguish environments, default: dev

Run the `tomcat9:run` goal after setting up 1 or more environment variables
`JOL_DATA=src/test/resources/data ./mvnw.cmd tomcat9:run`

You can also run unit tests with
`JOL_DATA=src/test/resources/data ./mvnw.cmd test`

Note that if you run with example/test data (from `src/test`), the app will launch with 6 pre-registered players named "Player1" through "Player6" respectively, each with the password "password".

### Data Structure
* games.json - hashmap of Game Name -> `GameInfo.java`
* pastGames.json - hashmap of Timestamp -> `GameHistory.java`
* players.json - hashmap of Player Name -> `PlayerInfo.java`
* chats.json - list of global chat entry `ChatEntryBean.java`
* timestamps.json - maps to `Timestamps.java`
* message.json - banner message
* decks.json - hashmap of Player Name -> Deck Name -> `DeckInfo.java`
* registrations.json - hashmap of Game Name -> Player Name -> `RegistrationStatus.java`
* tournament.json - maps to `TournamentData.java`
* cards - card source directory
  * vtescrypt.csv - official csv for crypt cards ( latest )
  * vteslib.csv - official csv for library cards ( latest )
* decks - decks directory
  * *.json - modern format decks
  * *.txt - legacy format decks
* games - games directory, by game uuid
  * *.json - registered decks
  * game.xml - game state - maps to `GameState.java`
  * actions.xml - chat history - maps to `GameActions.java`

### Technology Stack
* DWR - legacy pre-ajax java library for handling client-side callbacks
  * Generates `dwr/interface/DS.js` file that is used at runtime via `js/ds.js` based off the functions listed in `DeckServerRemote.java`
  * Requires complex objects to be registered as converters in `WEB-INF/dwr.xml` in order to serialize as JSON
  * `net.deckserver.dwr` - root folder for classes related to DWR functions
    * `bean` - objects that turned into JSON by DWR and used by the frontend `ds.js` script
    * `creators` - holds responsibility for populating JSON responses by types
      * `getFunction` - maps to JS callback located in `ds.js` file
      * `createData` - generates `bean` objects to use for rendering JSON data
    * `model` - misc classes for handling game state / command handling
      * `ChatParser.java` - builds card links, parses deck lines, builds disc icons
      * `CommandParser.java` - used to pull info out of users submitted commands
      * `DoCommand.java` - main logic that turns command line into game/chat actions
      * `GameModel.java` - holds the individual player views of a particular game
      * `PlayerModel.java` - holds the state of the logged in player, current view, chats to render
      * `GameView.java` - player-centric view of a game, holds toggle state, and state changed flags for players
      * `JolAdmin.java` - main responsibility class for pretty much everything - singleton
      * `JolGame.java` - main responsibility for modifying game state, or actions
      * `ModelLoader.java` - handles converting between UI and XML data objects
    * `DeckServerRemote` - main link between JS and JAVA, gets converted by DWR to `/dwr/interface/DS.js` file to be consumed
* JSP - basic servlet rendering library
  * `MainServlet.java` - maps to `/` and `/main.jsp`
  * `LoginServlet.java` - maps to `/login`
  * `LogoutServlet.java` - maps to `/logout`
  * `RegisterServlet.java` - maps to `/register`
  * JSP templates - located under `/WEB-INF/jsps`
    * grouped by function
    * `layout.jsp` for each JOL 'page' is loaded first, then it loads subsequent components
    * `/WEB-INF/main.jsp` holds all the references to the functional layout sections
* Jersey - Reference implementation for JAX-RS for restful services
  * `JolApplication` configures jersey for path and package scanning
  * `ApiResource` internal rest endpoints ( not used currently ) to eventually replace DWR
  * `CorsFilter` configure CORS settings for rest endpoints
  * `SecurityFilter` prototype to try and populate Servlet spec `Principal` and `isUserInRole` type functionality
    * commands - experiment to try and replace DWR / DeckServerRemote ( not used currently )
* JaxB - XMl to Java serialization - legacy JOL format
  * `net.deckserver.game.storage.jaxb` - root folder for XML file definitions
    * `actions` - files to serialize `actions.xml` file
    * `state` - files to serialize `game.xml` file
    * `XmlFileUtils` - wrapper to simplify loading/saving JOL xml files
* Jackson - JSON Serialization library
  * Internally used to read/write JSON based data files
* Jquery - UI javascript functionality
  * Used for DOM selectors, building / clearing dynamic elements based on JSON data returned by DWR
* Caffeine - caching library
  * Used to make expiring in memory caches for things like active users, or `JolGame` to speed up performance
* Nginx - static file server - hosted at https://static.deckserver.net/
  * `CardDatabaseBuilder.java` parses the VEKN supplied crypt and library CSV files and generates static JSON / HTML files to be used by tooltips / modals
  * `html` - html output of parsed card text, tooltip use only
  * `json` - json output of parsed text, used by modals to populate info
  * `images` - card images
* Docker - used to run application server on deckserver.net
  * `docker-compose.yml` - configuration for deckserver.net ( minus Traefik )
  * `local-docker-compose.yml` - local config for static data and/or application server