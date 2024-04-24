# Contributions of Group Members

## Fabio Luca Rippstein

### *Week 1 (28.03.24 - 10.04.24)*

**Create Schema for Lobby [(issue 41)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/41)**: </br>
completed on Monday, 08.04.2024

- Created entity Lobby
- Created files LobbyController, LobbyService, LobbyRepository & Lobby REST files
- Created corresponding tests (tests for LobbyRepository and Lobby REST files)</br>

**Implement REST GET to get a specific lobby [(issue 40)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/40)**: </br>
completed on Monday, 08.04.2024

- Implemented GET endpoint in file LobbyController
- Implemented corresponding tests in file LobbyControllerTest

**Create REST GET endpoint to get all lobbies [(issue 71)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/71)**: </br>
completed on Sunday, 07.04.2024

- Implemented GET endpoint in file LobbyController
- Implemented corresponding test in file LobbyControllerTest

**Implement Rest POST endpoint to create a lobby [(issue 38)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/38)**: </br>
completed on Monday, 08.04.2024

- Implemented POST endpoint in file LobbyController
- Implemented corresponding tests in file LobbyControllerTest
- Implemented tests for function createLobby() in files LobbyServiceIntegrationTest & LobbyServiceTest

**Implement REST PUT endpoint to join a lobby [(issue 39)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/39)**:</br>
completed on Monday, 08.04.2024

- Implemented PUT endpoint in file LobbyController
- Implemented corresponding tests in file LobbyControllerTest
- Implemented tests for function addPlayerToLobby() in files LobbyServiceIntegrationTest & LobbyServiceTest

### *Week 2 (11.04.24 - 17.04.24)*

**Create REST POST endpoint to swap tiles [(issue 56)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/56)**:</br>
completed on Wednesday, 17.04.2024

- Implemented REST POST endpoint for swapping tiles in file GameController
- Created 3 tests for POST endpoint in file GameControllerTest

**Implement functionality to check if lobby is full and ready to start [(issue 49)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/49)**:</br>
completed on Tuesday, 16.04.2024

- Implemented check if lobby is full in method addPlayer() in entity Lobby
- Implemented tests in files LobbyServiceTest and LobbyServiceIntegrationTest

**Implement persistence of played tiles [(issue 24)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/24)**:</br>
completed on Wednesday, 17.04.2024

- Implemented functionality to save and update oldPlayfield and currentPlayfield

**Implement REST POST to send played tiles (issue 27)** (in Progress - not completed yet)

**Implement functionality to validate move (issue 28)** (in Progress - not completed yet)

**Create functionality to swap tiles from player's hand and bag (issue 54)** (in Progress - not completed yet)

### *Week 3 (18.04.24 - 24.04.24)*

**Implement REST POST to send played tiles [(issue 27)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/27)**:</br>
completed on Wednesday, 24.04.2024

- Created REST POST endpoint
- Created tests in GameController and created 1 test for GamePostDTO

**Create functionality to swap tiles from player's hand and bag [(issue 54)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/54)**:</br>
completed on Tuesday, 23.04.2024

- Created function swapTiles() in GameService
- Created tests for function swapTiles() in file GameServiceTest

**Implement functionality to validate move [(issue 28)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/28)**:</br>
completed on Tuesday, 23.04.2024

- Created and updated function validMove() in GameService
- Created tests for function validMove() in file GameServiceTest

**Implement REST POST endpoint for contesting (issue 31)** (in Progress - not completed yet)

**Other tasks carried out:**
- fixed tests in file UserServiceIntegrationTest


## Luca Stephan Christl

### *Week 1*

**Create Schema for Game [(issue 21)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/21)**: </br>
completed on Wednesday, 10.04.2024

- Created entity Game, Bag, Cell, Playfield and Tile
- Created files GameController, GameRepository and GameService
- Created Enum Multiplier for Cells and GameMode for Game

**Create GamePage [(issue 1)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/1)**: </br>
completed on Saturday 06.04.2024

- Created Game view
- Created styling for Game view
- Created the Board
- Created Buttons for saving, clearing the board, swapping, leaving the game and opening the dictionary

**Create Drag and Drop [(issue 2)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/2)**: </br>
completed on Saturday 06.04.2024

- Created Cell Component and Draggable Component
- Implemented dragging and dropping for the Board and Hand
- Created Styling for Cell and Draggable

**Create Undo [(issue 3)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/3)**: </br>
completed on Saturday 06.04.2024

- Implemented the client side undo functionality for the undo button

### *Week 2*

**Create LobbyPage[(issue 13)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/13)**: </br>
completed on Wednesday 17.04.2024

- created LobbyPage
- created styling for LobbyPage
- implemented regular updates via getrequests
- implemented users automatically joining a lobby for M3

**Create Gameobject in Frontend [(issue 37)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/37)**: </br>
completed on Monday 15.04.2024

- created Gameobject in frontend

**Implement functionality to regularly update Gameobject [(issue 38)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/38)**: </br>
in Progress

-implemented regular updates for gameobject

-created necessary states and assign everything

### *Week 3*

**Implement functionality to regularly update Gameobject [(issue 38)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/38)**: </br>
completed on Monday 22.04.2024

-implemented display of scores and players

-overhauled updates to update every 2 seconds


**Implemented get Game endpoint[(issue 17)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/26)**: </br>
completed on Monday 22.04.2024

-implemented get Endpoint

-created gamegetdto

**Create functionality to update game screen after succesful swap of tiles[(issue 14)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/14)**: </br>
completed on Wednesday 24.04.2024

-implemented updating game state after swap


**Create functionality to select tiles and highlight them[(issue 15)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/15)**: </br>
completed on Wednesday 24.04.2024

- implemented custom popup to select tiles

- implemented function to manage selected tiles


**Implement pop up window and sound for unsuccessful swap notification[(issue 16)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/16)**: </br>
completed on Wednesday 24.04.2024

-implemented notification if swap didn't work


**Implement swap button and confirmation button in game page[(issue 17)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/17)**: </br>
completed on Wednesday 24.04.2024

- implemented swap button to open pop up
- implemented api call to swap selected tiles
- implemented confirm button to make api call


## Michael Kevin Barry

### *Week 0 and 1*

**Create function to authenticate user [(issue #82)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/82)**: </br>

**Implement register and login page with username and password input fields and commit button [(issue #30)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/30)**: </br>

**Create function to persist user in database [(issue #81)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/81)**: </br>

**Implement routing to main page after succesful login/registration [(issue #29)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/29)**: </br>

**Create REST POST endpoint for registered users to log in  [(issue #85)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/85)**: </br>

**Create user schema [(issue #57)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/57)**: </br>

**Create REST GET endpoints to get user information [(issue #61)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/61)**: </br>

**Create REST POST endpoints to register user [(issue #80)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/80)**: </br>

**Multiple bug fixes**: </br>

**Make sure validity of token can be verified by all controllers [(issue #89)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/89)**: </br>

**Initialise game [(issue #90)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/90)**: </br>

## Florian Lanz

### *Week 1*

**Create REST PUT endpoint to update user information [(issue #62)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/62)** </br>

**Implement profile page with user information and edit button [(issue #20)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/20)** </br>

**Create functionality to replace user information fields with input fields and replace edit button with save and abort button [(issue #18)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/18)** </br>

**Create REST GET endpoint to get the friendslist of the user [(issue #72)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/72)** </br>

**Create Post REST endpoint to log out a user [(issue #86)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/86)** </br>

**Create a logout button [(issue #32)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/32)** </br>

### *Week 2*

**Implement skip turn button in game screen [(issue #23)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/23)** </br>

**Create functionality to validate word with API [(issue #32)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/32)** </br>

**Create functionality to assign a new player as the current player [(issue #66)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/66)** </br>

**Create REST POST endpoint to skip turn [(issue #67)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/67)** </br>

### *Week 3*

**Create functionality to compute score changes for contesting [(issue #34)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/34)** >/br>

**Save current score in game object [(issue #94)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/94)** </br>