package arden;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import arden.runtime.ArdenDuration;
import arden.runtime.ArdenEvent;
import arden.runtime.ExecutionContext;

/**
 * Listens for events on a Socket. Calls
 * {@link ExecutionContext#callEvent(ArdenEvent)} on the given
 * {@link ExecutionContext}. <br/>
 * To send an event to the server (in bash):
 * 
 * <pre>
 *   <code>echo "Patient admission" > /dev/tcp/127.0.0.1/9701</code>
 * </pre>
 */
public class EventServer implements Runnable {
	private static final int MAX_CONNECTIONS = 10;
	private boolean verbose;
	private int port;
	private ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CONNECTIONS);
	private ExecutionContext context;

	public EventServer(ExecutionContext context, boolean verbose, int port) {
		this.verbose = verbose;
		this.port = port;
		this.context = context;
	}

	public void startServer() {
		new Thread(this).start();
	}

	@Override
	public void run() {
		// print port info
		if (verbose) {
			System.out.println("Listening for events on port " + port);
		}

		// listen for events in a thread
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(port);
			while (!Thread.currentThread().isInterrupted()) {
				Socket connection = socket.accept();
				threadPool.execute(new ClientHandler(connection, context, verbose));
			}
		} catch (IOException e) {
			System.err.println("Could not listen for events");
			e.printStackTrace();
		} finally {
			threadPool.shutdownNow();
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static class ClientHandler implements Runnable {
		private Socket connection;
		private ExecutionContext context;
		private boolean verbose;

		ClientHandler(Socket connection, ExecutionContext context, boolean verbose) {
			this.connection = connection;
			this.context = context;
			this.verbose = verbose;
		}

		@Override
		public void run() {
			try {
				InputStream eventStream = connection.getInputStream();
				Scanner scanner = new Scanner(eventStream);
				while (scanner.hasNext() && !Thread.currentThread().isInterrupted()) {
					String eventName = scanner.nextLine();
					if (verbose) {
						System.out.println("Received event: " + eventName);
					}
					// send event to context
					ArdenEvent event = new ArdenEvent(eventName, context.getCurrentTime().value);
					context.call(event, ArdenDuration.ZERO, 50);
				}
				scanner.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
