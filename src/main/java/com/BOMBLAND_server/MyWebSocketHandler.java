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
        broadcastMessage(session, message.getPayload());
        return;

      case "CREATE_ROOM":
        payload.remove("message_type");
        System.out.println("CREATE_ROOM");

        ArrayList<WebSocketSession> sessionsList = new ArrayList<>();
        sessionsList.add(session);
        Room_Members.put(payload.getString("id"), sessionsList);
        Room_Info.put(payload.getString("id"), payload);

        return;

      case "CHECK_ROOM":
        System.out.println("CHECK_ROOM");

        if (Room_Info.get(payload.getString("id")) != null) {
          payload.put("room_exists", "True");
        } else {
          payload.put("room_exists", "False");
        }

        String responseString = payload.toString();
        TextMessage responseMsg = new TextMessage(responseString);
        session.sendMessage(responseMsg);

        return;

      case "JOIN_ROOM":
        System.out.println("JOIN_ROOM");

        ArrayList<WebSocketSession> updatedSessionsList = Room_Members.get(payload.getString("id"));
        updatedSessionsList.add(session);
        Room_Members.put(payload.getString("id"), updatedSessionsList);

        JSONObject updatedRoomInfo = Room_Info.get(payload.getString("id"));
        updatedRoomInfo.put("player2", payload.getString("player2"));
        Room_Info.put(payload.getString("id"), updatedRoomInfo);

        JSONObject player1responseObj = new JSONObject();
        player1responseObj.put("message_type", "PLAYER_JOINED_ROOM");
        player1responseObj.put("player2", payload.getString("player2"));
        TextMessage player1Msg = new TextMessage(player1responseObj.toString());

        JSONObject player2responseObj = new JSONObject(updatedRoomInfo, JSONObject.getNames(updatedRoomInfo)); // copy-by-value
        player2responseObj.put("message_type", "JOIN_ROOM");
        TextMessage player2Msg = new TextMessage(player2responseObj.toString());

        updatedSessionsList.get(0).sendMessage(player1Msg); // sends player1 player2's username
        updatedSessionsList.get(1).sendMessage(player2Msg); // sends player2 room info

        return;

      case "UPDATE_READY_STATE_UI", "UPDATE_SETTINGS_UI":
        System.out.println("UPDATE_READY_STATE_UI || UPDATE_SETTINGS_UI");

        ArrayList<WebSocketSession> roomMembers = Room_Members.get(payload.get("roomId"));
        TextMessage payloadMsg = new TextMessage(payload.toString());

        if (roomMembers.get(0) == session) {
          roomMembers.get(1).sendMessage(payloadMsg);
        } else {
          roomMembers.get(0).sendMessage(payloadMsg);
        }

        return;

      default:
        // do nothing
        System.out.println("DEFAULT case called");
        break;
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