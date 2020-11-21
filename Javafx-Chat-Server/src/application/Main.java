package application;
	
import java.awt.Button;
import java.awt.Font;
import java.awt.Insets;
import java.awt.TextArea;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
	
	public static ExecutorService threadPool;
	public static Vector<Client> clients = new Vector<Client>();
	
	ServerSocket serverSocket;
	// Wait Client's connection after Server's starting
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(IP, port));
		} catch (Exception e) {
			e.printStackTrace();
			if(!serverSocket.isClosed())
				stopServer();
			return;
		}
		
		//waiting client's connection
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket));
						System.out.println("[Client connected]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
					} catch (Exception e) {
						if(!serverSocket.isClosed()) {
							stopServer();
						}
						break;
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool();
		threadPool.submit(thread);
	}
	
	//Stop server
	public void stopServer() {
		try {
			//close Sockets
			Iterator<Client> iterator = clients.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			//close server socket
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			//shutdown threadPool
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	//create UI and operate Javafx-Chat-Server
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPadding(new Insets(5));
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("arial", 15));
		root.setCenter(textArea);
		
		Button toggleButtton = new Button("Start");
		toggleButton.setMaxwidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1, 0, 0, 0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";
		int port = 9876;
		
		toggleButton.setOnAction(event -> {
			if(toggleButton.getText().equals("start")) {
				starSever(IP, port);
				Platform.runLater(() -> {
					String message = String.format("[Server Start]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("Quit");
				});
			} else {
				stopSever();
				Platform.runLater(() -> {
					String message = String.format("[Server End]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("Start");
				});
			}
		});
		Scene scene = new Scene(root, 400,400);
		primaryStage.setTitle("[Chatting Server]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
