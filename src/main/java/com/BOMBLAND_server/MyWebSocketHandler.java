package com.BOMBLAND_server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * This class sets up different routes for the server.
 */
public class MyWebSocketHandler extends TextWebSocketHandler {
  // Set to keep track of connected clients
  private final Set<WebSocketSession> Sessions = Collections.synchronizedSet(new HashSet<>());
  private final HashMap<String, ArrayList<WebSocketSession>> Room_Members = new HashMap<>();
  private final HashMap<String, Room> Room_Info = new HashMap<>();
  private final HashMap<String, MultiplayerGameMap> GameMap_Info = new HashMap<>();

  private final Object lock = new Object(); // The lock object

  /**
   * A callback method that's invoked immediately after a new WebSocket connection is successfully opened.
   * It provides a way to execute custom logic when a client first connects to the server via a WebSocket.
   *
   * @param session An object that represents the newly opened connection.
   * @throws Exception An exception gets thrown when an error occurs.
   */
  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    System.out.println("\n== afterConnectionEstablished() ==\n");
    Sessions.add(session);

    JSONObject responseObj = new JSONObject();
    responseObj.put("message_type", "CONNECTION_ESTABLISHED");
    responseObj.put("message", "You have successfully established a connection with the WebSocket server!");

    TextMessage responseMsg = new TextMessage(responseObj.toString());
    session.sendMessage(responseMsg);
  }

  /**
   * This method processes incoming text messages from connected WebSocket clients.
   *
   * @param session An object that represents the client's connection, and allows the server to send a reply back to
   *                that specific client.
   * @param message The actual message sent by the client.
   */
  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
    System.out.println("\n\n== handleTextMessage() ==");

    JSONObject payload = new JSONObject(message.getPayload());
    System.out.println("\npayload: " + payload);

    switch (payload.getString("message_type")) {
      case "HIGH_SCORE_INFO":
        System.out.println("\nHIGH_SCORE_INFO");
        payload.remove("message_type");
        handleHighScoreInfo(message, session);
        break;

      case "CREATE_ROOM":
        System.out.println("\nCREATE_ROOM");
        handleCreateRoom(payload, session);
        break;

      case "CHECK_ROOM":
        System.out.println("\nCHECK_ROOM");
        handleCheckRoom(payload, session);
        break;

      case "JOIN_ROOM":
        System.out.println("\nJOIN_ROOM");
        handleJoinRoom(payload, session);
        break;

      case "UPDATE_SETTINGS_UI":
        System.out.println("\nUPDATE_SETTINGS_UI");
        handleUpdateSettings(payload);
        break;

      case "LEAVE_ROOM":
        System.out.println("\nLEAVE_ROOM");
        handleLeaveRoom(payload, session);
        break;

      case "JOIN_GAME_MAP":
        System.out.println("\nJOIN_GAME_MAP");
        handleJoinGameMap(payload);
        break;

      case "FIRST_TILE_CLICK":
        System.out.println("\nFIRST_TILE_CLICK");
        handleFirstTileClick(payload, session);
        break;

      case "TILE_CLICKED":
        System.out.println("\nTILE_CLICKED");
        handleTileClicked(payload, session);
        break;

      case "GAME_OVER":
        System.out.println("\nGAME_OVER");
        handleGameOver(payload, session);
        break;

      case "PLAY_AGAIN":
        System.out.println("\nPLAY_AGAIN");
        handlePlayAgain(payload, session);
        break;

      case "LEAVE_GAME":
        System.out.println("\nLEAVE_GAME");
        handleLeaveGame(payload, session);
        break;

      default:
        // do nothing
        System.out.println("DEFAULT case called");
    }
  }

  /**
   * When a user sets a high score, the server receives the info pertaining to it, and then broadcasts it to
   * other players that are online.
   *
   * @param message A JSONObject containing the info about the new high score set.
   *                    - message_type: "HIGH_SCORE_INFO"
   *                    - id: room id
   *                    - difficulty: game difficulty
   *                    - map: map name
   *                    - name: player name
   *                    - score: player score
   *                    - time: time player set high score
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   */
  private void handleHighScoreInfo(TextMessage message, WebSocketSession session) throws IOException {
    broadcastMessage(session, message.getPayload());
  }

  /**
   * This function broadcasts a new high score that has been set by a player to all the other online players.
   *
   * @param currentSession An object that represents the player's connection, and allows the server to send a reply back to
   *                       that specific player.
   * @param message The high score set by the player.
   */
  private void broadcastMessage(WebSocketSession currentSession, String message) throws IOException {
    // Broadcasts the new high score to all players online (except the current one)
    synchronized (Sessions) {
      for (WebSocketSession session: Sessions) {
        if (currentSession == session) {
          continue;
        }

        try {
          session.sendMessage(new TextMessage(message)); // Send the message to each client
        } catch (IOException e) {
          System.out.println("\n====================================================================");
          System.out.println("ERROR - broadcastMessage(): Could not broadcast the new high score to other players online.");
          System.out.println("---");
          System.out.println("Cause: " + e.getCause());
          System.out.println("---");
          System.out.println("Message: " + e.getMessage());
          System.out.println("====================================================================\n");
          throw e;
        }
      }
    }
  }

  /**
   * Creates a multiplayer room.
   *
   * @param payload A JSONObject containing some info to use when creating the multiplayer room.
   *                    - message_type: "CREATE_ROOM"
   *                    - id: room id
   *                    - name: room name
   *                    - player1: player1 name
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleCreateRoom(JSONObject payload, WebSocketSession session) throws IOException {
    // Keeps track of the players/sessions of the specific room
    ArrayList<WebSocketSession> sessionsList = new ArrayList<>();
    sessionsList.add(session);
    Room_Members.put(payload.getString("id"), sessionsList);

    // Keeps track of the state of the room
    Room newRoom = new Room();
    newRoom.setId(payload.getString("id"));
    newRoom.setName(payload.getString("name"));
    newRoom.setPlayer1Name(payload.getString("player1"));
    newRoom.player1InRoom(true);
    Room_Info.put(newRoom.getId(), newRoom);

    // Sends a confirmation message to the player that the room was created
    JSONObject responseObj = createCopy(newRoom);
    responseObj.put("message_type", "ROOM_CREATED");
    TextMessage responseMsg = new TextMessage(responseObj.toString());
    session.sendMessage(responseMsg);
  }

  /**
   * Creates a (JSONObject) copy of the Room object provided.
   *
   * @param room The Room object to copy.
   * @return A JSONObject copy of the Room object provided.
   */
  private JSONObject createCopy(Room room) {
    JSONObject obj = new JSONObject();
    obj.put("id", room.getId());
    obj.put("name", room.getName());
    obj.put("player1Name", room.getPlayer1Name());
    obj.put("player2Name", room.getPlayer2Name());
    obj.put("isPlayer1InRoom", room.getPlayer1InRoom());
    obj.put("isPlayer2InRoom", room.getPlayer2InRoom());
    return obj;
  }

  /**
   * Checks whether a multiplayer room exists, and lets the player know.
   *
   * @param payload A JSONObject containing some info about the room that's being checked to see if it exists.
   *                    - message_type: "CHECK_ROOM"
   *                    - id: room id
   *                    - player2: player2 name
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleCheckRoom(JSONObject payload, WebSocketSession session) throws IOException {
    if (Room_Info.get(payload.getString("id")) != null) {
      payload.put("room_exists", true);
    } else {
      payload.put("room_exists", false);
    }

    // Lets the player know whether the room exists
    String responseString = payload.toString();
    TextMessage responseMsg = new TextMessage(responseString);
    session.sendMessage(responseMsg);
  }

  /**
   * This function gets called when player2 joins the room.
   *
   * @param payload A JSONObject containing some info about the room player2 is about to join.
   *                    - message_type: "JOIN_ROOM"
   *                    - id: room id
   *                    - player2: player2 name
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleJoinRoom(JSONObject payload, WebSocketSession session) throws IOException {
    ArrayList<WebSocketSession> sessionsList = Room_Members.get(payload.getString("id"));
    sessionsList.add(session);

    Room roomInfo = Room_Info.get(payload.getString("id"));
    roomInfo.setPlayer2Name(payload.getString("player2"));
    roomInfo.player2InRoom(true);

    JSONObject player1responseObj = createCopy(roomInfo);
    player1responseObj.put("message_type", "PLAYER_JOINED_ROOM");
    TextMessage player1Msg = new TextMessage(player1responseObj.toString());

    JSONObject player2responseObj = createCopy(roomInfo);
    player2responseObj.put("message_type", "JOIN_ROOM");
    TextMessage player2Msg = new TextMessage(player2responseObj.toString());

    sessionsList.get(0).sendMessage(player1Msg); // sends player1 the updated room info
    sessionsList.get(1).sendMessage(player2Msg); // sends player2 the updated room info
  }

  /**
   * When player1 changes the difficulty or map (in the multiplayer room), this info gets sent to player2 so that they can
   * see the change made in real-time on their screen.
   *
   * @param payload A JSONObject containing some info about the setting changed.
   *                    - message_type: "UPDATE_SETTINGS_UI"
   *                    - roomId: room id
   *                    - setting: will be set to either "map" or "difficulty"
   *                    - value: will be set to either a map name or difficulty mode
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleUpdateSettings(JSONObject payload) throws IOException {
    ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
    TextMessage payloadMsg = new TextMessage(payload.toString());
    roomMembers.get(1).sendMessage(payloadMsg);
  }

  /**
   * When either player leaves the multiplayer room they're in, this function gets called to do whatever clean up
   * that needs to be done, and notifies the other player that the current player has left.
   *
   * @param payload A JSONObject containing some info about the room being left.
   *            - message_type: LEAVE_ROOM
   *            - id: room id
   *            - playerName: name of player leaving the room
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleLeaveRoom(JSONObject payload, WebSocketSession session) throws IOException {
    if (Room_Info.get(payload.getString("id")) != null) {
      Room roomInfo = Room_Info.get(payload.getString("id"));
      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("id"));

      // Player1 is leaving the room
      if (isPlayer1(session, payload.getString("id"))) {
        if (roomMembers.size() == 2) {
          TextMessage leaveRoomMsg = new TextMessage(payload.toString());
          roomMembers.get(1).sendMessage(leaveRoomMsg);
        }

        Room_Members.remove(payload.getString("id"));
        Room_Info.remove(payload.getString("id"));
      } else { // Player2 is leaving the room
        TextMessage leaveRoomMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(leaveRoomMsg);
        roomMembers.remove(1);
        roomInfo.setPlayer2Name("N/A");
        roomInfo.player2InRoom(false);
      }
    }
  }

  /**
   * This function determines whether the current player is Player1.
   *
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @param roomId The id of the multiplayer room.
   * @return A boolean that represents whether the current player is Player1.
   */
  private boolean isPlayer1(WebSocketSession session, String roomId) {
    ArrayList<WebSocketSession> roomMembers = Room_Members.get(roomId);
    return session.equals(roomMembers.get(0));
  }

  /**
   * When Player1 starts a game from the room and heads to the game map, this function sends a msg to Player2
   * to let them know of this.
   *
   * @param payload A JSONObject containing some info about the room being left.
   *                    - message_type: "JOIN_GAME_MAP"
   *                    - roomId: room id
   *                    - map: map name
   *                    - difficulty: difficulty name
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleJoinGameMap(JSONObject payload) throws IOException {
    Room room = Room_Info.get(payload.getString("roomId"));
    room.player1InRoom(false);
    room.player2InRoom(false);

    GameMap_Info.put(payload.getString("roomId"), new MultiplayerGameMap());

    ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
    TextMessage gameSettingsMsg = new TextMessage(payload.toString());
    roomMembers.get(1).sendMessage(gameSettingsMsg);
  }

  /**
   * On a new game map, if Player1 makes the first click on the map, then the info from Player1's map
   * (first tile clicked coordinates, bombCoordinates, etc) get sent to Player2, this way both players have an identical game map.
   * The same thing applies if Player2's the first one to click on the map.
   *
   * @param payload A JSONObject containing some info about the game map being played on.
   *                    - message_type: "FIRST_TILE_CLICK"
   *                    - roomId: room id
   *                    - playerName: player name
   *                    - row: row number of tile clicked
   *                    - col: col number of tile clicked
   *                    - bombCoordinates: row & col of each bomb tile
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleFirstTileClick(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.getString("roomId"));

      if (!gameMap.getGameStarted()) {
        gameMap.setGameStarted(true);

        int tileRow = payload.getInt("row");
        int tileCol = payload.getInt("col");

        HashSet<Integer> columns = new HashSet<>();
        columns.add(tileCol);
        gameMap.getCoordinatesClicked().put(tileRow, columns);

        // Player1 made the first click
        if (isPlayer1(session, payload.getString("roomId"))) {
          gameMap.incrementPlayer1Points();
          payload.put("player1Points", gameMap.getPlayer1Points());
        } else { // Player2 made the first click
          gameMap.incrementPlayer2Points();
          payload.put("player2Points", gameMap.getPlayer2Points());
        }

        ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
        TextMessage initialGameMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(initialGameMsg);
        roomMembers.get(1).sendMessage(initialGameMsg);
      }
    }
  }

  /**
   * This function gets called whenever a tile is clicked (or uncovered as a result of a neighbor tile being clicked
   * or uncovered). If the tile hadn't been clicked/uncovered, whichever player clicked/uncovered it gets awarded a
   * point.
   *
   * @param payload A JSONObject containing some info about the tile clicked.
   *                    - message_type: "TILE_CLICKED"
   *                    - roomId: room id
   *                    - playerName: player name
   *                    - row: row number of tile clicked
   *                    - col: col number of tile clicked
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleTileClicked(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      int tileRow = payload.getInt("row");
      int tileCol = payload.getInt("col");

      MultiplayerGameMap gameMap = GameMap_Info.get(payload.getString("roomId"));
      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));

      if (gameMap.getCoordinatesClicked().containsKey(tileRow)) {
        HashSet<Integer> columns = gameMap.getCoordinatesClicked().get(tileRow);

        if (!columns.contains(tileCol)) {
          columns.add(tileCol);

          if (isPlayer1(session, payload.getString("roomId"))) {
            gameMap.incrementPlayer1Points();
            payload.put("player1Points", gameMap.getPlayer1Points());
          } else {
            gameMap.incrementPlayer2Points();
            payload.put("player2Points", gameMap.getPlayer2Points());
          }

          TextMessage tileClickedMsg = new TextMessage(payload.toString());
          roomMembers.get(0).sendMessage(tileClickedMsg);
          roomMembers.get(1).sendMessage(tileClickedMsg);
        }
      } else {
        HashSet<Integer> columns = new HashSet<>();
        columns.add(tileCol);
        gameMap.getCoordinatesClicked().put(tileRow, columns);

        if (isPlayer1(session, payload.getString("roomId"))) {
          gameMap.incrementPlayer1Points();
          payload.put("player1Points", gameMap.getPlayer1Points());
        } else {
          gameMap.incrementPlayer2Points();
          payload.put("player2Points", gameMap.getPlayer2Points());
        }

        TextMessage tileClickedMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(tileClickedMsg);
        roomMembers.get(1).sendMessage(tileClickedMsg);
      }
    }
  }

  /**
   * This function gets called when the game has ended, which happens when:
   *    1) A player clicked on a bomb tile, OR,
   *    2) All non-bomb tiles have been uncovered
   *
   * @param payload A JSONObject containing some info about the game that just ended.
   *                    - message_type: "GAME_OVER"
   *                    - roomId: room id
   *                    - playerName: player name
   *                    - playerDied: true (someone clicked a bomb tile) or false (all non-bomb tiles uncovered)
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleGameOver(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.getString("roomId"));

      if (gameMap.getCurrentRoundProcessed()) {
        return;
      }

      Room roomInfo = Room_Info.get(payload.getString("roomId"));

      boolean playerDied = payload.getBoolean("playerDied");

      if (playerDied) {
        if (isPlayer1(session, payload.getString("roomId"))) {
          payload.put("winner", roomInfo.getPlayer2Name());
          gameMap.incrementPlayer2Wins();
        } else {
          payload.put("winner", roomInfo.getPlayer1Name());
          gameMap.incrementPlayer1Wins();
        }
      } else {
        if (gameMap.getPlayer1Points() > gameMap.getPlayer2Points()) {
          payload.put("winner", roomInfo.getPlayer1Name());
          gameMap.incrementPlayer1Wins();
        } else if (gameMap.getPlayer1Points() < gameMap.getPlayer2Points()) {
          payload.put("winner", roomInfo.getPlayer2Name());
          gameMap.incrementPlayer2Wins();
        } else {
          // It's a tie
          payload.put("winner", "N/A");
          gameMap.incrementTies();
        }
      }

      payload.put("player1Points", gameMap.getPlayer1Points());
      payload.put("player2Points", gameMap.getPlayer2Points());
      payload.put("player1Wins", gameMap.getPlayer1Wins());
      payload.put("player2Wins", gameMap.getPlayer2Wins());
      payload.put("player1Name", roomInfo.getPlayer1Name());
      payload.put("player2Name", roomInfo.getPlayer2Name());
      payload.put("ties", gameMap.getTies());
      payload.put("round", gameMap.getCurrentRound());

      TextMessage gameOverMsg = new TextMessage(payload.toString());
      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
      roomMembers.get(0).sendMessage(gameOverMsg);
      roomMembers.get(1).sendMessage(gameOverMsg);

      gameMap.setCurrentRoundProcessed(true);
    }
  }

  /**
   * This function gets called whenever a player clicks on the Play Again button on the Game Over popup.
   *
   * @param payload A JSONObject containing some info about the current state of both players.
   *                    - message_type: "PLAY_AGAIN"
   *                    - roomId: room id
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handlePlayAgain(JSONObject payload, WebSocketSession session) throws IOException {
    MultiplayerGameMap gameMap = GameMap_Info.get(payload.getString("roomId"));

    synchronized (lock) {
      if (isPlayer1(session, payload.getString("roomId"))) {
        gameMap.setPlayer1WantsToPlayAgain(true);
      } else {
        gameMap.setPlayer2WantsToPlayAgain(true);
      }

      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));

      if (gameMap.getPlayer1WantsToPlayAgain() && gameMap.getPlayer2WantsToPlayAgain()) {
        gameMap.setGameStarted(false);
        gameMap.setCoordinatesClicked(new HashMap<>());
        gameMap.resetPlayer1Points();
        gameMap.resetPlayer2Points();
        gameMap.incrementCurrentRound();
        gameMap.setCurrentRoundProcessed(false);
        gameMap.setPlayer1WantsToPlayAgain(false);
        gameMap.setPlayer2WantsToPlayAgain(false);

        payload.put("player1Ready", true);
        payload.put("player2Ready", true);

        TextMessage playAgainMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(playAgainMsg);
        roomMembers.get(1).sendMessage(playAgainMsg);
      } else {
        if (gameMap.getPlayer1WantsToPlayAgain()) {
          payload.put("player1Ready", "yes");
          TextMessage playAgainMsg = new TextMessage(payload.toString());
          roomMembers.get(1).sendMessage(playAgainMsg);
        } else {
          payload.put("player2Ready", "yes");
          TextMessage playAgainMsg = new TextMessage(payload.toString());
          roomMembers.get(0).sendMessage(playAgainMsg);
        }
      }
    }
  }

  /**
   * This function gets called whenever a player leaves the game map to return to the multiplayer room.
   *
   * @param payload A JSONObject containing some info about the current state of both players.
   *                    - message_type: "LEAVE_GAME"
   *                    - roomId: room id
   *                    - playerName: player name
   * @param session An object that represents the player's connection, and allows the server to send a reply back to
   *                that specific player.
   * @throws IOException An exception that gets thrown when an error occurs.
   */
  private void handleLeaveGame(JSONObject payload, WebSocketSession session) throws IOException {
    Room roomInfo = Room_Info.get(payload.getString("roomId"));

    if (isPlayer1(session, payload.getString("roomId"))) {
      roomInfo.player1InRoom(true);
    } else {
      roomInfo.player2InRoom(true);
    }

    if (GameMap_Info.get(payload.getString("roomId")) != null) {
      GameMap_Info.remove(payload.getString("roomId"));
    }

    JSONObject responseObj = createCopy(roomInfo);
    responseObj.put("message_type", "LEAVE_GAME");
    responseObj.put("playerName", payload.getString("playerName"));
    TextMessage textMsg = new TextMessage(responseObj.toString());

    ArrayList<WebSocketSession> sessionsList = Room_Members.get(payload.getString("roomId"));
    sessionsList.get(0).sendMessage(textMsg);
    sessionsList.get(1).sendMessage(textMsg);
  }

  /**
   * This method handles exceptions that occur at the transport layer of a WebSocket connection. This provides a way to
   * gracefully handle errors that aren't tied to a specific incoming message, but rather to the connection itself.
   *
   * @param session An object that represents the client's connection, and allows the server to send a reply back to
   *                that specific client.
   */
  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) {
    System.out.println("\n== handleTransportError() ==");
    System.out.println("====================================================================");
    System.out.println("ERROR - handleTransportError(): Error occurred with WebSocket connection: " + session.getId());
    System.out.println("---");
    System.out.println("Cause: " + exception.getCause());
    System.out.println("---");
    System.out.println("Message: " + exception.getMessage());
    System.out.println("====================================================================\n");
  }

  /**
   * A callback method that's automatically invoked after a WebSocket connection has been closed.
   *
   * @param session An object that represents the client's connection, and allows the server to send a reply back to
   *                that specific client.
   * @param status An object that provides details about why the connection was closed.
   */
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    System.out.println("\n== afterConnectionClosed() ==");
    System.out.println("Connection closed: " + session.getId());
    Sessions.remove(session);
  }
}