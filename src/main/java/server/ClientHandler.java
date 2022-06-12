package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String username;
	private String currentSala;

	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());

			this.username = in.readUTF();
			this.out.writeUTF("Bienvenido a Biscord!\nPor favor, seleccione una sala:");
			this.out.writeUTF(Salas.salasToString());

			do {
				Integer option = validateInput();
				if (option != null) {
					String salaSelected = Salas.getSalaName(option);

					if (Salas.addClient(salaSelected, this)) {
						this.currentSala = salaSelected;
						this.out.writeUTF("GOOD");
						this.out.writeUTF("Te has unido a la sala: " + this.currentSala);
						this.out.writeUTF(Salas.chatToString(currentSala));
						break;
					}
				}

				this.out.writeUTF("BAD");
			} while (true);
			broadcastMessage("~ " + username + " ha entrado en el chat! ~");

		} catch (IOException e) {
			close();
		}
	}

	private Integer validateInput() {
		Integer option = null;

		try {
			option = Integer.parseInt(this.in.readUTF());

			if (Salas.outOfRange(option)) {
				return null;
			}
		} catch (NumberFormatException | IOException e) {
			return null;
		}

		return option;
	}

	@Override
	public void run() {
		String messageFromClient;
		while (socket.isConnected()) {
			try {
				messageFromClient = in.readUTF();
				broadcastMessage(messageFromClient);
			} catch (IOException e) {
				close();
				break;
			}
		}
	}

	public void broadcastMessage(String messageToSend) {
		Salas.addRecord(messageToSend, currentSala);

		for (ClientHandler client : Salas.getSalaClients(currentSala)) {
			try {
				if (!client.username.equals(username)) {
					client.out.writeUTF(messageToSend);
				}
			} catch (IOException e) {
				close();
			}
		}
	}

	public String getUsername() {
		return username;
	}

	public String getCurrentSala() {
		return currentSala;
	}

	public void close() {
		Salas.removeClient(this);
		try {
			if (this.in != null) {
				this.in.close();
			}
			if (this.out != null) {
				this.out.close();
			}
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("[SERVER-INFO] - Cliente desconectado.");
	}
}
