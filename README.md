# Scrabble - Online Version of the Popular Boardgame
## Introduction
This Project is part of the module Software Engineering Praktikum (Sopra) at University Zurich. The goal is to make a Scrabble clone to play online with friends. Scrabble is a word game in which two to four players score points by placing tiles, each bearing a single letter, onto a game board divided into a 15×15 grid of squares. The tiles must form words that, in crossword fashion, read left to right in rows or downward in columns and are included in a standard dictionary or lexicon. The motivation for the project stems from the group members passion for the game and interest in developing an online version to play as a party game with custom rules that fit their expectations. This is the <b>Server</b> part of the Project. The Client part can be found [here](https://github.com/sopra-fs24-group-24/scrabble-client).

## Technologies
- Frontend ([Client](https://github.com/sopra-fs24-group-24/scrabble-client))
    - [Node.js](https://nodejs.org/docs/latest/api/)

    - [React.js](https://react.dev/)

- Backend ([Server](https://github.com/sopra-fs24-group-24/scrabble-server))
    - [Springboot](https://docs.spring.io/spring-boot/documentation.html)

    - [H2 Database](https://www.h2database.com/html/quickstart.html)

    - [Gradle](https://gradle.org/)

    - [Wordnik API](https://developer.wordnik.com/) (Dictionary)

- Github (Version Control)

## High-level Components
The Server consists of the following main Components:

- The [Controller](https://github.com/sopra-fs24-group-24/scrabble-server/tree/main/src/main/java/ch/uzh/ifi/hase/soprafs24/controller) Package
holding the Rest Controllers used for communication via HTTP Requests with the Client.

- The [Service](https://github.com/sopra-fs24-group-24/scrabble-server/tree/main/src/main/java/ch/uzh/ifi/hase/soprafs24/service) Package
holding all the files implementing the main logic and persistence of objects.

The main classes are held in the [entity](https://github.com/sopra-fs24-group-24/scrabble-server/tree/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity) Package:

- The [User](https://github.com/sopra-fs24-group-24/scrabble-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/User.java) class which handles and defines attributes of the User.

- The [Lobby](https://github.com/sopra-fs24-group-24/scrabble-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Lobby.java) class which defines attributes for lobbies and handles the start of a game.

- The [Game](https://github.com/sopra-fs24-group-24/scrabble-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs24/entity/Game.java) class which defines the attributes for games and initialises them. I.e. Tiles, Bag and Hands.

## Getting Started
Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

### IntelliJ
If you consider to use IntelliJ as your IDE of choice, you can make use of your free educational license [here](https://www.jetbrains.com/community/education/#students).
1. File -> Open... -> SoPra server template
2. Accept to import the project as a `gradle project`
3. To build right click the `build.gradle` file and choose `Run Build`

### VS Code
The following extensions can help you get started more easily:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs24` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

### Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

You can verify that the server is running by visiting `localhost:8080` in your browser.

### Test

```bash
./gradlew test
```

### Development Mode
You can start the backend in development mode, this will automatically trigger a new build and reload the application
once the content of a file has been changed.

Start two terminal windows and run:

`./gradlew build --continuous`

and in the other one:

`./gradlew bootRun`

If you want to avoid running all tests with every change, use the following command instead:

`./gradlew build --continuous -xtest`

### API Endpoint Testing with Postman
We recommend using [Postman](https://www.getpostman.com) to test your API Endpoints.

### Debugging
If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and name it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

### Testing
Have a look here: https://www.baeldung.com/spring-boot-testing


### Deployment

The app will automatically deploy to Google Cloud when it detects changes 
to the main branch. To view the deployment progress use the Github <b>[Actions](https://github.com/sopra-fs24-group-24/scrabble-server/actions)</b> tab. 
After successful deployment you can use the app following this link: https://sopra-fs24-group-24-client.oa.r.appspot.com/login. Or access the Server by using this one: https://sopra-fs24-group-24-server.oa.r.appspot.com/

### How to Contribute
1. Clone the repository to your local machine and create a new branch.

2. Make changes and test them.

3. Submit a Pull Request with a well written description which changes have been made and why.


## Roadmap

The following features are planned to be implemented in the future:

- Textchat (with private and public chats)

- Persistance of Highscores

- Different Gamemodes i.e. TimeAttack with a move timer.


## Contributing / Authors
Working on this Project:

- <b>Fabio Luca Rippstein</b> - Github: [frippstein](https://github.com/frippstein)

- <b>Florian Lanz</b> - Github: [flolanz](https://github.com/flolanz)

- <b>Michael Kevin Barry</b> - Github: [dandynstuff](https://github.com/dandynstuff)

- <b>Luca Stephan Christl</b> - Github: [LucaStephan-Christl](https://github.com/LucaStephan-Christl)

see also the document [contributions.md](https://github.com/sopra-fs24-group-24/scrabble-server/blob/main/contributions.md)

## Acknowledgements
- [Template Server](https://github.com/HASEL-UZH/sopra-fs24-template-server)

- [Template Client](https://github.com/HASEL-UZH/sopra-fs24-template-client)


## License
This project is licensed under the Apache License - see the [LICENSE](https://github.com/sopra-fs24-group-24/scrabble-server/blob/main/LICENSE) file for details.
