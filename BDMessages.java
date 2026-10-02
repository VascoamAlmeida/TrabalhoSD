import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BDMessages {
    private static final String MESSAGE_FILE = "messages.txt";
    private static List<Message> messageDB = new ArrayList<>();

    public static synchronized void saveMessage(Message message){
        messageDB.add(message);
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(MESSAGE_FILE, true))){
            writer.write(formatMessage(message));
            writer.newLine();
        }catch (IOException e){
            System.err.println("Erro ao salvar mensagem: " + e.getMessage());
        }
    }

    public static synchronized List<Message> loadMessageForGroup(int groupId){
        loadMessagesFromFile();
        List<Message> groupMessages = new ArrayList<>();
        for(Message message: messageDB){
            if(message.getGroupId() == groupId){
                groupMessages.add(message);
            }
        }
        return groupMessages;
    }

    private static synchronized void loadMessagesFromFile(){
        if(!messageDB.isEmpty()) return;

        try(BufferedReader reader = new BufferedReader(new FileReader(MESSAGE_FILE))){
            String line;
            while ((line = reader.readLine()) != null){
                Message message = parseMessage(line);
                if(message != null){
                    messageDB.add(message);
                }
            }
        }catch (FileNotFoundException e){
            System.out.println("Arquivo de mensagens não encontrado. Criando um novo...");
        }catch (IOException e){
            System.err.println("Erro ao ler mensagens do arquivo: " + e.getMessage());
        }
    }

    private static String formatMessage(Message message){
        return message.getGroupId() + ";" + message.getSenderEmail() + ";" +
                message.getContent().replace(";", "\\;") + ";" +
                message.getTimestamp();
    }

    private static Message parseMessage(String line){
        try {
            String[] parts = line.split("(?<!\\\\);", 4);
            int groupId = Integer.parseInt(parts[0]);
            String senderEmail = parts[1];
            String content = parts[2].replace("\\;", ";");
            LocalDateTime timestamp = LocalDateTime.parse(parts[3]);
            return new Message(groupId, senderEmail, content, timestamp);
        }catch (Exception e){
            System.err.println("Erro ao processar mensagem: "+ line);
            return null;
        }
    }
}
