package jm;

import java.io.*;
import java.util.*;

class GamePlay {
	public static final String SHOOT = "Shoot";
	public static final String WAIT = "Wait";
	public static final String LOSE = "Lose";
	public static final String WIN = "Win";
	public static final String MISS = "Miss";
	public static final String READY = "Ready";
	public static final String START = "Start";
	public static final String OPPONENT_DISCONNECTED = "OpponentDisconnected";
	PrintWriter[] writers;
	BufferedReader[] readers;
	int shootingPlayer;
	int waitingPlayer;
	boolean isPlaying = true;

	public GamePlay(PrintWriter[] writers, BufferedReader[] readers) {
		this.writers = writers;
		this.readers = readers;
		setUpGame();
	}

	void setUpGame() {
		Random rand = new Random();
		shootingPlayer = rand.nextInt(2);
		if (shootingPlayer == 0) {
			waitingPlayer = 1;
		} else {
			waitingPlayer = 0;
		}

		startGame();
	}

	private void startGame() {
		write(0, START);
		write(1, START);
		while (isPlaying) {
			tellPlayerToShoot();
		}
	}

	private void write(int playerToWriteTo, String command) {
		writers[playerToWriteTo].println(command);
		writers[playerToWriteTo].flush();
	}

	private String read(int playerToReadFrom) {
		String command = null;
		try {
			command = readers[playerToReadFrom].readLine();
			if (command == null) {
			    // client disconnected
                int playerToWriteTo = (playerToReadFrom == 0) ? 1 : 0;
                write(playerToWriteTo, OPPONENT_DISCONNECTED);
                endGame();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return command;
	}

	private void tellPlayerToShoot() {
		write(shootingPlayer, SHOOT);
		write(waitingPlayer, WAIT);
		String cellLocation = read(shootingPlayer);
		String result = tellPlayerToCheckShootResult(cellLocation);
		if (result.equals(LOSE)) {
			write(shootingPlayer, WIN);
			write(waitingPlayer, LOSE);
			endGame();
		} else {
			write(shootingPlayer, result);
		}
		if (result.equals(MISS)) {
			endTurn();
		}
	}

	private String tellPlayerToCheckShootResult(String cellLocation) {
		write(waitingPlayer, cellLocation);
		return read(waitingPlayer);
	}

	private void endTurn() {
		if (shootingPlayer == 0) {
			shootingPlayer = 1;
			waitingPlayer = 0;
		} else {
			shootingPlayer = 0;
			waitingPlayer = 1;
		}
	}

	private void endGame() {
		isPlaying = false;
		if (writers[0] != null) {
			writers[0].close();
		}
		if (writers[1] != null) {
			writers[1].close();
		}
	}
}