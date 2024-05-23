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

### *Week 4 (25.04.24 - 01.05.24)*

**Implement REST POST endpoint for contesting [(issue 31)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/31)**:</br>
completed on Wednesday, 01.05.2024

- Created/Updated REST POST endpoint for contesting
- Created tests in file GameControllerTest
- Created/Updated method contestWord() in file GameService
- Created tests for method contestWord()

**Create functionality to reset state of board to beginning of players move in server [(issue 33)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/33)**:</br>
completed on Wednesday, 01.05.2024

- Implemented functionality to update oldPlayfield and currentPlayfield in database in method contestWord()
- Created tests which check whether the correct versions of oldPlayfield and currentPlayfield are saved in the database
- Updated method getOldPlayfield in entity Game

**Implement pop up window for player to contest word in game page [(issue 4)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/4)**:</br>
completed on Wednesday, 01.05.2024

- Implemented contest pop up window in game page

### *Week 5 (02.05.24 - 08.05.24)*

**Create button to join lobby with code [(issue 58)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/58)**:</br>
completed on Tuesday, 07.05.2024

- Created a button on the main page to join a private lobby with a code

**Create pop up window to enter lobby code [(issue 59)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/59)**:</br>
completed on Wednesday, 08.05.2024

- Created pop up window on main page to enter lobby code

**Implement routing from profile page to main page [(issue 19)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/19)**:</br>
completed on Wednesday, 08.05.2024

- Created back-to-main-menu button and implemented routing from profile page to main page

**Implement pop up window to display validity of contested words [(issue 5)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/5)**:</br>
completed on Wednesday, 08.05.2024

- Implemented multiple functions in Game.tsx
- Implemented additional function which fetches data from the server once
- Updated function notifyValidationResult in Game.tsx
- Added additional attributes to the Game entity in the back-end

**Implement countdown before a game starts [(issue 56)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/56)**:</br>
completed on Tuesday, 07.05.2024

- Implemented 5 seconds countdown before a game starts in Lobby.tsx 

**Other tasks carried out:**
- Updated setPlayers method of GameGetDTO and LobbyGetDTO (issues 40 and 48)
- Added attribute wordsToBeContested in back-end in order to display those words in front-end (issue 4)
- Bug fixes (issues 24 and 28)

### *Week 6 (09.05.24 - 15.05.24)*

**Implement functionality so that when all but one player left the game, 
the remaining player is correctly displayed on the gameover-screen [(issue 70)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/70)**:</br>
completed on Tuesday, 14.05.2024

- Changed return statement of Gameover-Component
- Changed function of Game-Component which fetches data from server each second

**Implement Pop-Up Window which informs the last player that all other players
have left the game early [(issue 71)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/71)**:</br>
completed on Tuesday, 14.05.2024

- Created useEffect in Gameover-Component which checks if all but one Player left the game early
- Changed return statement of the Gameover-Component
- Created corresponding scss file

**Create functionality to end game if only one player remains in the game [(issue 126)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/126)**:</br>
completed on Tuesday, 14.05.2024

- Created functionality in LobbyService to end game if only one player remains in the game

**Only send information users are allowed to see [(issue 140)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/140)**:</br>
completed on Wednesday, 15.05.2024

- Implemented Methods in GameService and implemented corresponding tests
- Implemented Methods in LobbyService and implemented corresponding tests

**Create confirm message pop-up window when leaving the game [(issue 55)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/55)**:</br>
completed on Tuesday, 14.05.2024

- Extended return statement of Game-Component

**Remove timer for contesting in frontend [(issue 64)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/64)**:</br>
completed on Wednesday, 15.05.2024

- Removed timers of contesting pop-up
- Added Waiting-Pop-Up
- Added attributes to the Game entity in back-end
- Added functionalities to the button of the Contest-Pop-Up
- Implemented function in Game-Component which fetches data from the server once

**Other tasks carried out:**
- Bug fixes (issue 28 - server)
- Fixed IntegrationTests of Contest-Function (issue 31)

### *Week 7 (16.05.24 - 23.05.24)*

**This week the Joker "skip continuous progress" was used.**

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

### *Week 4*

**Implement main page screen [(issue 26)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/26)**: </br>
completed on Monday 1.05.2024

- implemented fetching lobbies regularly
- made the lobbies clickable
- added styling for the home page


**Implement routing from main page to profile screen  [(issue 24)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/24)**: </br>
completed on Monday 1.05.2024

- implemented clickable username in home page

**Implement pop up window for deciding the size of the lobby and who can join [(issue 9)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/9)**: </br>
completed on Monday 1.05.2024

- created new LobbyCreator component which is a popup
- added styling to the pop up
- implemented choosing lobbySize and GameMode
- implemented create button to make api call

**Implement create lobby button on main page [(issue 10)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/10)**: </br>
completed on Monday 1.05.2024

- created createLobby Button
- added functionality to display popup

**Implement routing from main page to lobby page[(issue 7)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/7)**: </br>
completed on Monday 1.05.2024

- implemented clicking on lobbies makes api call to join lobby
- implemented routing to lobbypage

### *Week 5*

**Add textfield to give lobby custom name when creating [(issue 62)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/62)**: </br>
completed on Tuesday 7.05.2024

- created input field in create lobby pop up
- created title attribute in lobby object
- display lobby title in homepage
- display lobby title in lobbypage

**create routing from main page to tutorial page and vice versa [(issue 61)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/61)**: </br>
completed on Tuesday 7.05.2024

- created a button in home page for routing
- created tutorialpage component
- implemented routing to tutorialpage
- created back button in tutorialpage

**create a tutorial page that explains how the game works  [(issue 60)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/60)**: </br>
completed on Wednesday 8.05.2024

- created tutorial page styling
- wrote tutorial texts

**create leave game button  [(issue 53)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/53)**: </br>
completed on Wednesday 8.05.2024

- created leave game button
- implemented api call to leave game and display error

**implement "pop up window" (now toast notifiction) to display messages in gamepage  [(issue 22)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/22)**: </br>
completed on Sunday 5.05.2024

- created toast notifications
- implemented toast notification to show who is playing

### *Week 6*

**Filter lobbies so that private, full or lobbies with a game running arent displayed [(issue 66)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/66)**: </br>
completed on Monday 13.05.2024

- created filter for private, full and running lobbies

**Adjust design of game page for smaller screens [(issue 65)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/65)**: </br>
completed on Monday 13.05.2024

- fixed bug where scoreboard got cut off

**added checkbox to make lobbies private [(issue 57)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/57)**: </br>
completed on Thursday 8.05.2024

- added checkbox and attribute postdto for lobby

**Create search bar in profile page and search button [(issue 42)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/42)**: </br>
completed on Wednesday 15.05.2024

- created search field
- created a search button with a svg
- implemented functionality to make api call when clicking on button

**Create box in profile to display search results [(issue 43)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/43)**: </br>
completed on Wednesday 15.05.2024

- created mapping from response to players
- added styling 
- made the list scrollable
- added custom scrollbar

**Create routing between different profiles [(issue 44)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/44)**: </br>
completed on Wednesday 15.05.2024

- made the results of searching clickable
- implemented redirecting to other profiles
- added back to my profile button when viewing other profiles

### *Week 7*

**Create box with received friend requests [(issue 51)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/51)**: </br>
completed on Sunday 19.05.2024

- Created box where friendrequests are displayed
- created buttons to accept and decline friendrequests

**Create box to display friendslist [(issue 52)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/52)**: </br>
completed on Sunday 19.05.2024

- Created box where you can see friend and their online status
- Created routing to friends profile pages

**Create box or pop up window to display word information [(issue 46)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/46)**: </br>
completed on Wednesday 22.05.2024

- Created notification to display word definitions after unsuccessful contesting
- created function to make the call to our endpoint to get the definitions

**Created README.md for Server and Client**: </br>
completed on Tuesday 21.05.2024

- Created README.md for client with screenshots
- Created README.md for server

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

### *Week 2*

**Multiple bug fixes**: </br>

**Make sure validity of token can be verified by all controllers [(issue #89)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/89)**: </br>

**Initialise game [(issue #90)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/90)**: </br>

### *Week 3*

**Implement end of game screen [(issue #28)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/28)**: </br>

**Create functionality to persist highscore of winner [(issue #75)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/75)**: </br>

### *Week 4*

**Create functionality to end game and compute final scores [(issue #76)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/76)**: </br>

**Implement routing from game page to end game screen client [(issue #21)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/21)**: </br>

### *Week 5*

**Change lobby put endpoint to check if player can join with game pin [(issue #84)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/84)** </br>

**Update lobby REST endpoint to remove player from lobby and game [(issue #124)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/124)**: </br>

**Create routing from end game page to main page client [(issue #27)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/27)**: </br>

**Implement pop up window for leave lobby confirmation client [(issue #12)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/12)**: </br>

### *Week 6*

**Authenticate all interactions with game controller [(issue #138)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/138)** </br>

**Updated/fixed tests for #138 [(issue #138)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/138)** </br>

**Authenticate all interactions with lobby controller [(issue #137)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/137)**: </br>

**Updated/fixed tests for #137 [(issue #137)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/137)** </br>

**Delete game and lobby after leaving end game screen [(issue #145)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/145)**: </br>

**Redesign/Improvement of issue 77 [(issue #77)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/77)**: </br>

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

**Create functionality to compute score changes for contesting [(issue #34)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/34)** </br>

**Save current score in game object [(issue #94)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/94)** </br>

### *Week 4*

**Create functional add friend button [(issue #49)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/49)** </br>

**Create REST endpoint to add a friend [(issue #117)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/117)** </br>

**Create REST endpoint to remove a friend [(issue #118)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/118)** </br>

**Create REST enpoint to accept a friend request [(issue #119)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/119)** </br>

### *Week 5*

**Create REST endpoint to get users with RegEx [(issue #105)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/105)** </br>

**Create functionality to get word definition from external API [(issue #109)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/109)** </br>

**Generate and save game pin for private lobbies [(issue #130)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/130)** </br>

### *Week 6*

**Create button to remove friend [(issue #50)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/50)** </br>

**Remove players from lobby when closing window [(issue #63)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/63)** </br>

**Create guards for lobby, game and profile [(issue #67)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/67) [(issue #68)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/68) [(issue #69)](https://github.com/sopra-fs24-group-24/scrabble-client/issues/69)** </br>

**Create REST endpoint to get word information [(issue #108)](https://github.com/sopra-fs24-group-24/scrabble-server/issues/108)** </br>