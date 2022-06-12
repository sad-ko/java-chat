package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Salas {

	private static Map<String, List<ClientHandler>> clients = new HashMap<>();
	private static Map<String, List<String>> records = new HashMap<>();

	private Salas() {
	}

	public static boolean createSala(String salaName) {
		boolean alreadyExits = !clients.containsKey(salaName);
		if (alreadyExits) {
			clients.put(salaName, new ArrayList<>());
			records.put(salaName, new ArrayList<>());
		}

		return alreadyExits;
	}

	public static boolean removeSala(String salaName) {
		boolean alreadyExits = clients.containsKey(salaName);
		if (alreadyExits) {
			clients.get(salaName).forEach(ClientHandler::close);
			clients.get(salaName).clear();
			records.get(salaName).clear();

			clients.remove(salaName);
			records.remove(salaName);
		}

		return alreadyExits;
	}

	public static boolean addClient(String sala, ClientHandler client) {
		if (clients.get(sala) != null) {
			clients.get(sala).add(client);
			return true;
		}

		return false;
	}

	public static void removeClient(ClientHandler client) {
		if (clients.get(client.getCurrentSala()) != null) {
			clients.get(client.getCurrentSala()).remove(client);
			client.broadcastMessage("~ " + client.getUsername() + " ha dejado en el chat! ~");
		}
	}

	public static void addRecord(String message, String sala) {
		records.get(sala).add(message);
	}

	public static List<ClientHandler> getSalaClients(String sala) {
		return clients.get(sala);
	}

	public static String getSalaName(int index) {
		Set<String> salasSet = clients.keySet();
		String[] salas = salasSet.toArray(new String[salasSet.size()]);
		return salas[index];
	}

	public static String salasToString() {
		StringBuilder str = new StringBuilder();
		int i = 0;

		for (String sala : clients.keySet()) {
			str.append(i++ + ") " + sala + "\n");
		}

		return str.toString();
	}

	public static String chatToString(String sala) {
		StringBuilder str = new StringBuilder();

		for (String msg : records.get(sala)) {
			str.append(msg + "\n");
		}

		return str.toString();
	}

	public static boolean outOfRange(int index) {
		return (clients.size() <= index || index < 0);
	}

}
