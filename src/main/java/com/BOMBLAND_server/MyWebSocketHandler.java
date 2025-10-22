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

public class MyWebSocketHandler extends TextWebSocketHandler {
  // Set to keep track of connected clients
  private final Set<WebSocketSession> Sessions = Collections.synchronizedSet(new HashSet<>());
  private final HashMap<String, ArrayList<WebSocketSession>> Room_Members = new HashMap<>();
  private final HashMap<String, JSONObject> Room_Info = new HashMap<>();
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

      case "UPDATE_READY_STATE_UI", "UPDATE_SETTINGS_UI":
        System.out.println("UPDATE_READY_STATE_UI || UPDATE_SETTINGS_UI");
        handleUpdateSettings(payload, session);
        break;

      case "LEAVE_ROOM":
        System.out.println("\nLEAVE_ROOM");
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

  private void handleCreateRoom(JSONObject payload, WebSocketSession session) {
    payload.remove("message_type");

    ArrayList<WebSocketSession> sessionsList = new ArrayList<>();
    sessionsList.add(session);
    
    Room_Members.put(payload.getString("id"), sessionsList);
    Room_Info.put(payload.getString("id"), payload);
  }

  private void handleCheckRoom(JSONObject payload, WebSocketSession session) throws IOException {
    if (Room_Info.get(payload.getString("id")) != null) {
      payload.put("room_exists", "True");
    } else {
      payload.put("room_exists", "False");
    }

    String responseString = payload.toString();
    TextMessage responseMsg = new TextMessage(responseString);
    session.sendMessage(responseMsg);
  }

  private void handleJoinRoom(JSONObject payload, WebSocketSession session) throws IOException {
    ArrayList<WebSocketSession> sessionsList = Room_Members.get(payload.getString("id"));
    sessionsList.add(session);

    JSONObject roomInfo = Room_Info.get(payload.getString("id"));
    roomInfo.put("player2", payload.getString("player2"));

    JSONObject player1responseObj = new JSONObject();
    player1responseObj.put("message_type", "PLAYER_JOINED_ROOM");
    player1responseObj.put("player2", payload.getString("player2"));
    TextMessage player1Msg = new TextMessage(player1responseObj.toString());

    JSONObject player2responseObj = new JSONObject(roomInfo, JSONObject.getNames(roomInfo)); // copy-by-value
    player2responseObj.put("message_type", "JOIN_ROOM");
    TextMessage player2Msg = new TextMessage(player2responseObj.toString());

    sessionsList.get(0).sendMessage(player1Msg); // sends player1 player2's username
    sessionsList.get(1).sendMessage(player2Msg); // sends player2 room info
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
      JSONObject roomInfo = Room_Info.get(payload.get("roomId"));
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
        roomInfo.remove("player2");
      }
    }
  }

  private void handleJoinGameMap(JSONObject payload, WebSocketSession session) throws IOException {
    GameMap_Info.put(payload.get("roomId").toString(), new MultiplayerGameMap());

    ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.get("roomId"));
    TextMessage gameSettingsMsg = new TextMessage(payload.toString());
    roomMembers.get(1).sendMessage(gameSettingsMsg);
  }

  private void handleFirstTileClick(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.get("roomId"));

      if (!gameMap.gameStarted) {
        gameMap.gameStarted = true;

        int tileRow = payload.getInt("row");
        int tileCol = payload.getInt("col");

        HashSet<Integer> columns = new HashSet<>();
        columns.add(tileCol);
        gameMap.coordinatesClicked.put(tileRow, columns);

        if (isPlayer1(session, payload.getString("roomId"))) {
          gameMap.player1Points++;
        } else {
          gameMap.player2Points++;
        }

        ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.get("roomId"));
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
      ArrayList<WebSocketSession> roomMembers = null;

      if (gameMap.coordinatesClicked.containsKey(tileRow)) {
        HashSet<Integer> columns = gameMap.coordinatesClicked.get(tileRow);

        if (!columns.contains(tileCol)) {
          columns.add(tileCol);

          roomMembers = Room_Members.get(payload.getString("roomId"));
          TextMessage tileClickedMsg = new TextMessage(payload.toString());

          if (isPlayer1(session, payload.getString("roomId"))) {
            gameMap.player1Points++;
            roomMembers.get(1).sendMessage(tileClickedMsg);
          } else {
            gameMap.player2Points++;
            roomMembers.get(0).sendMessage(tileClickedMsg);
          }
        }
      } else {
        HashSet<Integer> columns = new HashSet<>();
        columns.add(tileCol);
        gameMap.coordinatesClicked.put(tileRow, columns);

        roomMembers = Room_Members.get(payload.getString("roomId"));
        TextMessage tileClickedMsg = new TextMessage(payload.toString());

        if (isPlayer1(session, payload.getString("roomId"))) {
          gameMap.player1Points++;
          roomMembers.get(1).sendMessage(tileClickedMsg);
        } else {
          gameMap.player2Points++;
          roomMembers.get(0).sendMessage(tileClickedMsg);
        }
      }
    }
  }

  private void handleGameOver(JSONObject payload, WebSocketSession session) throws IOException {
    synchronized (lock) {
      JSONObject roomInfo = Room_Info.get(payload.getString("roomId"));
      MultiplayerGameMap gameMap = GameMap_Info.get(payload.get("roomId"));

      boolean playerDied = payload.getBoolean("playerDied");

      if (playerDied) {
        if (isPlayer1(session, payload.getString("roomId"))) {
          payload.put("winner", roomInfo.get("player2"));
        } else {
          payload.put("winner", roomInfo.get("player1"));
        }
      } else {
        if (gameMap.player1Points > gameMap.player2Points) {
          payload.put("winner", roomInfo.get("player1"));
          gameMap.player1Wins++;
        } else if (gameMap.player1Points < gameMap.player2Points) {
          payload.put("winner", roomInfo.get("player2"));
          gameMap.player2Wins++;
        } else {
          // It's a tie
          payload.put("winner", "N/A");
          gameMap.ties++;
        }
      }

      payload.put("player1Points", gameMap.player1Points);
      payload.put("player2Points", gameMap.player2Points);
      payload.put("player1Wins", gameMap.player1Wins);
      payload.put("player2Wins", gameMap.player2Wins);
      payload.put("ties", gameMap.ties);

      TextMessage gameOverMsg = new TextMessage(payload.toString());
      ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.getString("roomId"));
      roomMembers.get(0).sendMessage(gameOverMsg);
      roomMembers.get(1).sendMessage(gameOverMsg);
    }
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