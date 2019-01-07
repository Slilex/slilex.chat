package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class Server {

    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    private static class Handler extends Thread{

        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException{
            String name = null;
            while(true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message message = connection.receive();
                if(message.getType() == MessageType.USER_NAME && message.getData()!=null && message.getData()!=""){
                    if(!connectionMap.containsKey(message.getData())){
                        name = message.getData();
                        connectionMap.put(name,connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        break;
                    }
                }
            }
            return name;
        }

        private void sendListOfUsers(Connection connection, String userName) throws IOException{

            for (String name: connectionMap.keySet() ) {
                if (!name.equals(userName)) {
                    connection.send(new Message(MessageType.USER_ADDED, name));
                }
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();

                if(message.getType() == MessageType.TEXT){

                    sendBroadcastMessage(new Message(MessageType.TEXT, userName+ ": " + message.getData()));
                }else{
                    ConsoleHelper.writeMessage("what the F....???");
                }

            }
        }

        public void run()  {
            String userName = null;
            ConsoleHelper.writeMessage("Установлено соединение с " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)){


                if( (userName = serverHandshake(connection))!=null) {

                    sendListOfUsers(connection, userName);

                    sendBroadcastMessage(new Message(MessageType.USER_ADDED, userName));

                    serverMainLoop(connection, userName);



                }

            }catch(ClassNotFoundException e) {
                ConsoleHelper.writeMessage("blya  class not////");
            }catch(IOException e){
                ConsoleHelper.writeMessage("snova blya....");
            }

            if (userName != null){
                connectionMap.remove(userName);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,userName));
            }

            ConsoleHelper.writeMessage("yce");

        }

    }

    public static void main(String[] args) throws IOException{

    int portServer = ConsoleHelper.readInt();
    try {
        ServerSocket serverSocket = new ServerSocket(portServer);
        ConsoleHelper.writeMessage("Сервер запущен");
        try {

            while (true) {
                new Handler(serverSocket.accept()).start();
            }
        }catch (IOException e){
            serverSocket.close();
        }
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public static void sendBroadcastMessage(Message message){
        try {
            for (Connection connection : connectionMap.values()) {
                connection.send(message);
            }
        }catch (IOException e){
            ConsoleHelper.writeMessage("Хрень произошла...  и никому ничего не отправилось .....");
        }

    }

}
