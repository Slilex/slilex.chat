package client;

import java.io.IOException;
import java.net.Socket;

import server.*;


public class Client {
    
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("участник " + userName + " присоединился к чату");
        }

        protected void informAboutDeletingNewUser(String userName) {

         ConsoleHelper.writeMessage("участник " + userName + " покинул чат");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException{
            Message mes = null;
            while (true){
                mes = connection.receive();
                if(mes.getType()==MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                }
                else if(mes.getType() == MessageType.NAME_ACCEPTED){
                    notifyConnectionStatusChanged(true);
                    break;
                }else{
                    throw new IOException("Unexpected MessageType");
                }
            }

        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            Message mes = null;
            while (true){
                mes = connection.receive();
                if(mes.getType()==MessageType.TEXT){
                    processIncomingMessage(mes.getData());
                }
                else if(mes.getType() == MessageType.USER_ADDED){
                    informAboutAddingNewUser(mes.getData());
                }else if(mes.getType() == MessageType.USER_REMOVED){
                    informAboutDeletingNewUser(mes.getData());
                }else{
                    throw new IOException("Unexpected MessageType");

                }
            }

        }

        public void run(){
            try {
                connection = new Connection(new Socket(getServerAddress(), getServerPort()));
                clientHandshake();
                clientMainLoop();

            }catch (IOException e){
                //e.printStackTrace();
                notifyConnectionStatusChanged(false);

            } catch (ClassNotFoundException e) {
                //e.printStackTrace();
                notifyConnectionStatusChanged(false);
            }
        }


    }

    protected String getServerAddress(){

        return ConsoleHelper.readString();
    }

    protected int getServerPort(){

        return ConsoleHelper.readInt();
    }

    protected String getUserName(){
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole(){

        return true;
    }

    protected SocketThread getSocketThread(){

        return new SocketThread();
    }

    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        }catch (IOException e){
            e.printStackTrace();
            clientConnected = false;
            e.getStackTrace();
        }

    }

    public void run(){

        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this){
            try {
                this.wait();
                if(clientConnected){
                    ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
                    String mes = null;
                    while (clientConnected){
                        if((mes = ConsoleHelper.readString()).equals("exit"))break;
                        if(shouldSendTextFromConsole()){
                            sendTextMessage(mes);
                        }
                    }

                }else{
                    ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
                }


            } catch (InterruptedException e) {
                ConsoleHelper.writeMessage("Socket thread is interrupted!");
            }
        }



    }

    public static void main(String[] args){

        Client client = new Client();
        client.run();

    }

}
