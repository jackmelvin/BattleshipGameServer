package jm;

import java.io.*;
import java.net.*;

class GameServer {

	public static void main(String[] args) {
		new GameServer().start();
	}

	void start() {
		try {
			ServerSocket serverSock = new ServerSocket(5228);
			int connectedPlayerNum = 0;
			Socket[] players = new Socket[2];
			PrintWriter[] writers = new PrintWriter[2];
			BufferedReader[] readers = new BufferedReader[2];
			while (true) {
				players[connectedPlayerNum] = serverSock.accept();
				writers[connectedPlayerNum] = new PrintWriter(players[connectedPlayerNum].getOutputStream());
				readers[connectedPlayerNum] = new BufferedReader(new InputStreamReader(players[connectedPlayerNum].getInputStream()));
				System.out.println("P" + (connectedPlayerNum + 1) + ": " + players[connectedPlayerNum] + " connected");
				connectedPlayerNum++;
				if (connectedPlayerNum == 2) {
					boolean clientsConnectedSuccessfully = true;
					String s0 = null;
					String s1 = null;
					// try to write and read from both players
					try {
						writers[0].println(GamePlay.READY);
						writers[0].flush();
						writers[1].println(GamePlay.READY);
						writers[1].flush();
						s0 = readers[0].readLine();
						s1 = readers[1].readLine();
					} catch (IOException e) {
						clientsConnectedSuccessfully = false;
						e.printStackTrace();
					}
					// if player 0 disconnected and player 1 is ready, match player 1 with another player
					if (s0 == null && s1 != null && s1.equals(GamePlay.READY)) {
						System.out.println(players[0] + " disconnected");
						players[0] = players[1];
						writers[0] = writers[1];
						readers[0] = readers[1];
						connectedPlayerNum = 1;
						clientsConnectedSuccessfully = false;
					}
					// if player 1 disconnected and player 0 is ready, match player 0 with another player
					if (s1 == null && s0 != null && s0.equals(GamePlay.READY)) {
						System.out.println(players[1] + " disconnected");
						connectedPlayerNum = 1;
						clientsConnectedSuccessfully = false;
					}
					if (clientsConnectedSuccessfully) {
						Thread t = new Thread(new GameHandler(writers, readers));
						t.start();
						System.out.println("Started a new game");
						players = new Socket[2];
						writers = new PrintWriter[2];
						readers = new BufferedReader[2];
						connectedPlayerNum = 0;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class GameHandler implements Runnable {
		PrintWriter[] writers;
		BufferedReader[] readers;
		GameHandler(PrintWriter[] writers, BufferedReader[] readers) {
			this.writers = writers;
			this.readers = readers;
		}

		@Override
		public void run() {
			new GamePlay(writers, readers);
		}
	}
}