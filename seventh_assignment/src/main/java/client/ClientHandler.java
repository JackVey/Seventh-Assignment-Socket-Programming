package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String userName;
    public ClientHandler(Socket socket){
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //TODO -> have to get the clients name somehow
            this.userName = bufferedReader.readLine();
            addClientHandler(this);
            //TODO -> have to broad cast that this user has entered the chat
            broadcastMessage(userName + " has joined the chat!");
        } catch (Exception e) {
            closeEverything();
        }
    }

    public synchronized void addClientHandler(ClientHandler clientHandler){
        clientHandlers.add(this);
    }

    public void broadcastMessage(String messageToSend){
        for (ClientHandler clientHandler : clientHandlers){
            try {
                if (!clientHandler.equals(this)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }
            catch (IOException e){
                closeEverything();
            }
        }
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage(userName + " has left the chat!");
    }

    public void closeEverything(){
        removeClientHandler();
        try {
            if (bufferedWriter != null){
                bufferedWriter.close();
            }
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (socket != null){
                socket.close();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()){
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }
            catch (IOException e){
                closeEverything();
                break;
            }
        }
    }
}
