package com.BOMBLAND_server;

import java.util.HashMap;
import java.util.HashSet;

public class MultiplayerGameMap {
  boolean gameStarted;
  HashMap<Integer, HashSet<Integer>> coordinatesClicked;
  int player1Points;
  int player2Points;
  int player1Wins;
  int player2Wins;
  int ties;

  MultiplayerGameMap() {
    gameStarted = false;
    coordinatesClicked = new HashMap<>();
    player1Points = 0;
    player2Points = 0;
    player1Wins = 0;
    player2Wins = 0;
    ties = 0;
  }
}