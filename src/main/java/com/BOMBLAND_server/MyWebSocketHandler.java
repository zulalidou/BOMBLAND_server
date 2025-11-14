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
    System.out.println("\n== handleTextMessage() ==");

    JSONObject payload = new JSONObject(message.getPayload());
    System.out.println("payload: " + payload + "\n");

    switch (payload.getString("message_type")) {
      case "HIGH_SCORE_INFO":
        System.out.println("HIGH_SCORE_INFO");
        payload.remove("message_type");
        handleHighScoreInfo(message, session);
        break;

      case "CREATE_ROOM":
        System.out.println("CREATE_ROOM");
        handleCreateRoom(payload, session);
        break;

      case "CHECK_ROOM":
        System.out.println("CHECK_ROOM");
        handleCheckRoom(payload, session);
        break;

      case "JOIN_ROOM":
        System.out.println("JOIN_ROOM");
        handleJoinRoom(payload, session);
        break;

      case "UPDATE_SETTINGS_UI":
        System.out.println("UPDATE_SETTINGS_UI");
        handleUpdateSettings(payload, session);
        break;

      case "LEAVE_ROOM":
        System.out.println("\nLEAVE_ROOM");
        handleLeaveRoom(payload, session);
        break;

      case "UPDATE_GAME_READY":
        System.out.println("\nUPDATE_GAME_READY");
        handleLeaveRoom(payload, session);
        break;

      case "JOIN_GAME_MAP":
        // Player1 has started the game, and a msg needs to be sent to Player2 to let them know to go to the Game map
        System.out.println("\nJOIN_GAME_MAP");
        handleJoinGameMap(payload, session);
        break;

      case "FIRST_TILE_CLICK":
        // Both Player1 and Player2 attempt to set the coordinates of the first tile clicked.
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
   * This function broadcasts a new high score that has been set by a client to all the other active clients.
   *
   * @param currentSession An object that represents the client's connection, and allows the server to send a reply back to
   *                       that specific client.
   * @param message The high score set by the client.
   */
  private void broadcastMessage(WebSocketSession currentSession, String message) {
    System.out.println("\n== broadcastHighScore() ==\n");

    // Broadcast the new high score to all connected clients (except the current one)
    synchronized (Sessions) {
      for (WebSocketSession session: Sessions) {
        if (currentSession == session) {
          continue;
        }

        try {
          session.sendMessage(new TextMessage(message)); // Send the message to each client
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void handleHighScoreInfo(TextMessage message, WebSocketSession session) {
    broadcastMessage(session, message.getPayload());
  }

  private void handleCreateRoom(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "CREATE_ROOM"
     * - id: room id
     * - name: room name
     * - player1: player1 name
     */

    ArrayList<WebSocketSession> sessionsList = new ArrayList<>();
    sessionsList.add(session);
    Room_Members.put(payload.getString("id"), sessionsList);

    Room newRoom = new Room();
    newRoom.setId(payload.getString("id"));
    newRoom.setName(payload.getString("name"));
    newRoom.setPlayer1Name(payload.getString("player1"));
    newRoom.player1InRoom(true);
    Room_Info.put(newRoom.getId(), newRoom);

    JSONObject responseObj = createCopy(newRoom);
    responseObj.put("message_type", "ROOM_CREATED");
    TextMessage responseMsg = new TextMessage(responseObj.toString());
    session.sendMessage(responseMsg);
  }

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

  private void handleCheckRoom(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "CHECK_ROOM"
     * - id: room id
     * - player2: player2 name
     */

    if (Room_Info.get(payload.getString("id")) != null) {
      payload.put("room_exists", true);
    } else {
      payload.put("room_exists", false);
    }

    String responseString = payload.toString();
    TextMessage responseMsg = new TextMessage(responseString);
    session.sendMessage(responseMsg);
  }


  // Gets called when:
  // - A player joins the room for the first time, OR,
  // - When a player leaves the game map and the other players clicks the OK button on a popup that redirects
  //   them to the room page
  private void handleJoinRoom(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "JOIN_ROOM"
     * - id: room id
     * - player2: player2 name
     */

    ArrayList<WebSocketSession> sessionsList = Room_Members.get(payload.getString("id"));

    if (sessionsList.size() < 2) { // Player 2 joins the room for the first time
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
    } else { // Either player 1 or 2 rejoins the room after the other player leaves the game map
      Room roomInfo = Room_Info.get(payload.getString("id"));

      JSONObject existingPlayerResponseObj = new JSONObject();
      existingPlayerResponseObj.put("message_type", "PLAYER_JOINED_ROOM");
      TextMessage existingPlayerMsg = new TextMessage(existingPlayerResponseObj.toString());

      JSONObject playerJoiningResponseObj = new JSONObject(roomInfo, JSONObject.getNames(roomInfo)); // copy-by-value
      playerJoiningResponseObj.put("message_type", "JOIN_ROOM");
      TextMessage playerJoiningMsg = new TextMessage(playerJoiningResponseObj.toString());

      if (isPlayer1(session, payload.getString("roomId"))) {
        // Player 1 rejoining room
        sessionsList.get(1).sendMessage(existingPlayerMsg);
        sessionsList.get(0).sendMessage(playerJoiningMsg);
      } else {
        // Player 2 rejoining room
        sessionsList.get(0).sendMessage(existingPlayerMsg);
        sessionsList.get(1).sendMessage(playerJoiningMsg);
      }
    }
  }

  private void handleUpdateSettings(JSONObject payload, WebSocketSession session) throws IOException {
    ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.get("roomId"));
    TextMessage payloadMsg = new TextMessage(payload.toString());

    if (roomMembers.get(0) == session) {
      roomMembers.get(1).sendMessage(payloadMsg);
    } else {
      roomMembers.get(0).sendMessage(payloadMsg);
    }
  }

  private void handleLeaveRoom(JSONObject payload, WebSocketSession session) throws IOException {
    if (Room_Info.get(payload.get("roomId")) != null) {
      Room roomInfo = Room_Info.get(payload.get("roomId"));
      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.get("roomId"));

      // Player1 is leaving the room
      if (isPlayer1(session, payload.getString("roomId"))) {
        if (roomMembers.size() == 2) {
          TextMessage leaveRoomMsg = new TextMessage(payload.toString());
          roomMembers.get(1).sendMessage(leaveRoomMsg);
        }

        Room_Members.remove(payload.get("roomId"));
        Room_Info.remove(payload.get("roomId"));
      } else { // Player2 is leaving the room
        TextMessage leaveRoomMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(leaveRoomMsg);
        roomMembers.remove(1);
        roomInfo.setPlayer2Name("N/A");
      }
    }
  }

  private void handleJoinGameMap(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "JOIN_GAME_MAP"
     * - roomId: room id
     * - map: map name
     * - difficulty: difficulty name
     */

    Room room = Room_Info.get(payload.getString("roomId"));
    room.player1InRoom(false);
    room.player2InRoom(false);

    GameMap_Info.put(payload.getString("roomId"), new MultiplayerGameMap());

    ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
    TextMessage gameSettingsMsg = new TextMessage(payload.toString());
    roomMembers.get(1).sendMessage(gameSettingsMsg);
  }

  private void handleFirstTileClick(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "FIRST_TILE_CLICK"
     * - roomId: room id
     * - playerName: player name
     * - row: row number of tile clicked
     * - col: col number of tile clicked
     * - bombCoordinates: row & col of each bomb tile
     */

    synchronized (lock) {
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.getString("roomId"));

      if (!gameMap.getGameStarted()) {
        gameMap.setGameStarted(true);

        int tileRow = payload.getInt("row");
        int tileCol = payload.getInt("col");

        HashSet<Integer> columns = new HashSet<>();
        columns.add(tileCol);
        gameMap.getCoordinatesClicked().put(tileRow, columns);

        if (isPlayer1(session, payload.getString("roomId"))) {
          gameMap.incrementPlayer1Points();
          payload.put("player1Points", gameMap.getPlayer1Points());
          System.out.println("XXX - row = " + tileRow + ", col = " + tileCol + ", playerName = " + payload.get("playerName"));
        } else {
          gameMap.incrementPlayer2Points();
          payload.put("player2Points", gameMap.getPlayer2Points());
          System.out.println("OOO - row = " + tileRow + ", col = " + tileCol + ", playerName = " + payload.get("playerName"));
        }

        ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
        TextMessage initialGameMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(initialGameMsg);
        roomMembers.get(1).sendMessage(initialGameMsg);
      }
    }
  }

  private void handleTileClicked(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      int tileRow = payload.getInt("row");
      int tileCol = payload.getInt("col");

      MultiplayerGameMap gameMap = GameMap_Info.get(payload.get("roomId"));
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
          System.out.println("XXX - row = " + tileRow + ", col = " + tileCol + ", playerName = " + payload.get("playerName"));
        } else {
          gameMap.incrementPlayer2Points();
          payload.put("player2Points", gameMap.getPlayer2Points());
          System.out.println("OOO - row = " + tileRow + ", col = " + tileCol + ", playerName = " + payload.get("playerName"));
        }

        TextMessage tileClickedMsg = new TextMessage(payload.toString());
        roomMembers.get(0).sendMessage(tileClickedMsg);
        roomMembers.get(1).sendMessage(tileClickedMsg);
      }
    }
  }

  private void handleGameOver(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      Room roomInfo = Room_Info.get(payload.getString("roomId"));
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.get("roomId"));

      if (gameMap.getCurrentRoundProcessed()) {
        return;
      }

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

  private void handleLeaveGame(JSONObject payload, WebSocketSession session) throws IOException {
    /**
     * payload:
     * - message_type: "LEAVE_GAME"
     * - roomId: room id
     * - playerName: player name
     */

    Room roomInfo = Room_Info.get(payload.getString("roomId"));

    if (isPlayer1(session, payload.getString("roomId"))) {
      roomInfo.player1InRoom(true);
    } else {
      roomInfo.player2InRoom(true);
    }

    JSONObject responseObj = createCopy(roomInfo);
    responseObj.put("message_type", "LEAVE_GAME");
    responseObj.put("playerName", payload.getString("playerName"));
    TextMessage textMsg = new TextMessage(responseObj.toString());

    ArrayList<WebSocketSession> sessionsList = Room_Members.get(payload.getString("roomId"));
    sessionsList.get(0).sendMessage(textMsg);
    sessionsList.get(1).sendMessage(textMsg);
  }

  private boolean isPlayer1(WebSocketSession session, String roomId) {
    ArrayList<WebSocketSession> roomMembers = Room_Members.get(roomId);
    return session.equals(roomMembers.get(0));
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
    System.out.println("\n\n== handleTransportError() ==\n");
    System.err.println("Error occurred with WebSocket connection: " + session.getId());
    exception.printStackTrace();
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
    System.out.println("\n\n== afterConnectionClosed() ==\n");

    // ITERATE OVER THE ROOMS MAP TO MAKE SURE TO REMOVE THIS SESSION IF IT STILL EXISTS IN ANY MULTIPLAYER ROOM

    Sessions.remove(session);
    System.out.println("afterConnectionClosed()");
    System.out.println("Connection closed: " + session.getId());
  }
}