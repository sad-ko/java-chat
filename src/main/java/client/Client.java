package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {

	private Socket socket;
	private DataInputStream in;
	private DataOutputStream out;
	private String username;
	private boolean inSala = false;

	public Client(String ip, int port, String username) throws IOException {
		this.socket = new Socket(ip, port);
		this.username = username;
		this.in = new DataInputStream(socket.getInputStream());
		this.out = new DataOutputStream(socket.getOutputStream());
	}

	public void sendMessage() {
		try (Scanner scanner = new Scanner(System.in)) {
			out.writeUTF(username); // Envia username al server.

			while (socket.isConnected()) {
				String messageToSend = scanner.nextLine();

				if (messageToSend.equals("")) {
					continue;
				}

				if (!inSala) {
					out.writeUTF(messageToSend); // Selecciona sala.
				} else if (!messageToSend.equals("/exit")) {
					String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/mm/yy hh:mm"));
					out.writeUTF(username + " - " + time + ":\n  " + messageToSend);
					System.out.println("yo" + " - " + time + ":\n  " + messageToSend);
				} else {
					System.out.println("[Biscord] - Bye bye~");
					closeEverything(socket, in, out);
					System.exit(0);
				}
			}
		} catch (IOException e) {
			closeEverything(socket, in, out);
		}
	}

	public void listenForMessage() {
		Thread listener = new Thread(() -> {
			String msg;
			while (socket.isConnected()) {
				try {
					msg = in.readUTF();
					System.out.println(msg);

					if (msg.equalsIgnoreCase("GOOD")) {
						inSala = true;
					}
				} catch (IOException e) {
					closeEverything(socket, in, out);
				}
			}
		});

		listener.start();
	}

	public void closeEverything(Socket socket, DataInputStream in, DataOutputStream out) {
		try {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.print("Ingrese su nombre: \n> ");
			String name = scanner.nextLine();
			Client client = new Client("localhost", 20000, name);
			client.listenForMessage();
			client.sendMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
