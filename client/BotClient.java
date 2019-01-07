package client;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Math.random;

import server.*;

public class BotClient extends Client {

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
                if (message.split(":").length == 2) {

                    SimpleDateFormat dateFormat;
                    switch (message.substring(message.indexOf(":") + 2)) {
                        case "дата":
                            dateFormat = new SimpleDateFormat("d.MM.YYYY");
                            break;
                        case "день":
                            dateFormat = new SimpleDateFormat("d");
                            break;
                        case "месяц":
                            dateFormat = new SimpleDateFormat("MMMM");
                            break;
                        case "год":
                            dateFormat = new SimpleDateFormat("YYYY");
                            break;
                        case "время":
                            dateFormat = new SimpleDateFormat("H:mm:ss");
                            break;
                        case "час":
                            dateFormat = new SimpleDateFormat("H");
                            break;
                        case "минуты":
                            dateFormat = new SimpleDateFormat("m");
                            break;
                        case "секунды":
                            dateFormat = new SimpleDateFormat("s");
                            break;
                        default:
                            dateFormat = null;
                    }
                    if (dateFormat != null) {
                        sendTextMessage("Информация для " + message.substring(0, message.indexOf(":")) + ": " + dateFormat.format(Calendar.getInstance().getTime()));
                    }
                }

        }

    }

    @Override
    protected String getUserName() {
        int random = (int)(Math.random()*100);
        return "date_bot_"+random;
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    public static void main(String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

}
