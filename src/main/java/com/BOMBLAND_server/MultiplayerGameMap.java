package com.BOMBLAND_server;

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class keeps track of the state of each game map.
 */
public class MultiplayerGameMap {
  private boolean gameStarted;
  private HashMap<Integer, HashSet<Integer>> coordinatesClicked;
  private int player1Points;
  private int player2Points;
  private int player1Wins;
  private int player2Wins;
  private int ties;
  private int currentRound;
  private boolean currentRoundProcessed;
  private boolean player1WantsToPlayAgain;
  private boolean player2WantsToPlayAgain;

  MultiplayerGameMap() {
    gameStarted = false;
    coordinatesClicked = new HashMap<>();
    player1Points = 0;
    player2Points = 0;
    player1Wins = 0;
    player2Wins = 0;
    ties = 0;
    currentRound = 1;
    currentRoundProcessed = false;
    player1WantsToPlayAgain = false;
    player2WantsToPlayAgain = false;
  }

  boolean getGameStarted() {
    return gameStarted;
  }

  void setGameStarted(boolean gameStarted) {
    this.gameStarted = gameStarted;
  }

  HashMap<Integer, HashSet<Integer>> getCoordinatesClicked() {
    return coordinatesClicked;
  }

  void setCoordinatesClicked(HashMap<Integer, HashSet<Integer>> coordinatesClicked) {
    this.coordinatesClicked = coordinatesClicked;
  }

  int getPlayer1Points() {
    return player1Points;
  }

  void incrementPlayer1Points() {
    this.player1Points++;
  }

  void resetPlayer1Points() {
    this.player1Points = 0;
  }

  int getPlayer2Points() {
    return player2Points;
  }

  void incrementPlayer2Points() {
    this.player2Points++;
  }

  void resetPlayer2Points() {
    this.player2Points = 0;
  }

  int getPlayer1Wins() {
    return player1Wins;
  }

  void incrementPlayer1Wins() {
    this.player1Wins++;
  }

  int getPlayer2Wins() {
    return player2Wins;
  }

  void incrementPlayer2Wins() {
    this.player2Wins++;
  }

  int getTies() {
    return ties;
  }

  void incrementTies() {
    this.ties++;
  }

  int getCurrentRound() {
    return currentRound;
  }

  void incrementCurrentRound() {
    this.currentRound++;
  }

  boolean getCurrentRoundProcessed() {
    return currentRoundProcessed;
  }

  void setCurrentRoundProcessed(boolean currentRoundProcessed) {
    this.currentRoundProcessed = currentRoundProcessed;
  }

  boolean getPlayer1WantsToPlayAgain() {
    return player1WantsToPlayAgain;
  }

  void setPlayer1WantsToPlayAgain(boolean player1WantsToPlayAgain) {
    this.player1WantsToPlayAgain = player1WantsToPlayAgain;
  }

  boolean getPlayer2WantsToPlayAgain() {
    return player2WantsToPlayAgain;
  }

  void setPlayer2WantsToPlayAgain(boolean player2WantsToPlayAgain) {
    this.player2WantsToPlayAgain = player2WantsToPlayAgain;
  }
}