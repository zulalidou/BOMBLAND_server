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
  int round;
  boolean roundProcessed;
  boolean player1WantsToPlayAgain;
  boolean player2WantsToPlayAgain;
  boolean aPlayerHasLeftTheGameMap;
  boolean player1ClickedStartGame;
  boolean player2ClickedStartGame;

  MultiplayerGameMap() {
    gameStarted = false;
    coordinatesClicked = new HashMap<>();
    player1Points = 0;
    player2Points = 0;
    player1Wins = 0;
    player2Wins = 0;
    ties = 0;
    round = 1;
    roundProcessed = false;
    player1WantsToPlayAgain = false;
    player2WantsToPlayAgain = false;
    aPlayerHasLeftTheGameMap = false;
    player1ClickedStartGame = false;
    player2ClickedStartGame = false;
  }
}