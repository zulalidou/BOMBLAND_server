package com.BOMBLAND_server;

/**
 * This class keeps track of the state of each multiplayer room.
 */
public class Room {
  String id;
  String name;
  String player1Name;
  String player2Name;
  boolean isPlayer1InRoom;
  boolean isPlayer2InRoom;

  Room() {
    id = "";
    name = "";
    player1Name = "";
    player2Name = "N/A";
    isPlayer1InRoom = false;
    isPlayer2InRoom = false;
  }

  // Copy constructor
  Room(Room originalRoom) {
    this.id = originalRoom.getId();
    this.name = originalRoom.getName();
    this.player1Name = originalRoom.getPlayer1Name();
    this.player2Name = originalRoom.getPlayer2Name();
    this.isPlayer1InRoom = originalRoom.getPlayer1InRoom();
    this.isPlayer2InRoom = originalRoom.getPlayer2InRoom();
  }

  String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
  }

  String getName() {
    return name;
  }

  void setName(String name) {
    this.name = name;
  }

  String getPlayer1Name() {
    return player1Name;
  }

  void setPlayer1Name(String player1Name) {
    this.player1Name = player1Name;
  }

  String getPlayer2Name() {
    return player2Name;
  }

  void setPlayer2Name(String player2Name) {
    this.player2Name = player2Name;
  }

  boolean getPlayer1InRoom() {
    return isPlayer1InRoom;
  }

  void player1InRoom(boolean isPlayer1InRoom) {
    this.isPlayer1InRoom = isPlayer1InRoom;
  }

  boolean getPlayer2InRoom() {
    return isPlayer2InRoom;
  }

  void player2InRoom(boolean isPlayer2InRoom) {
    this.isPlayer2InRoom = isPlayer2InRoom;
  }
}