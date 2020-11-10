import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    TextField tfFileName;

    @FXML
    ListView<String> clientFilesList;
    @FXML
    ListView<String> serverFilesList;

    Network net = new Network();

    @Override
    public void initialize(URL location, ResourceBundle resources){
        net.start();

        System.out.println("Connect");
        Thread t = new Thread(()->{
            try{
                while (true){
                    AbstractMessage am = Network.readObject();
                    if(am instanceof  FileMessage){
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("Client_storage/" + fm.getFileName()), fm.getData(),
                        StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                        serverList();
                    }
                }
            } catch (ClassNotFoundException | IOException e){
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
        refreshLocalFilesList();
    }

    public void refreshLocalFilesList(){
        Platform.runLater(()->{
            try {
                clientFilesList.getItems().clear();
                Files.list(Paths.get("Client_storage"))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p->p.getFileName().toString())
                        .forEach(o->clientFilesList.getItems().add(o));
            } catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    public void serverList() {
        try {
            serverFilesList.getItems().clear();
            serverFilesList.getItems().addAll(getServerFiles());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getServerFiles() throws IOException {
        List<String> files = new ArrayList<>();

        net.getOut().writeUTF("./getFilesList");
        net.getOut().flush();
        int listSize = net.getIn().readInt();
        for (int i = 0; i < listSize; i++) {
            files.add(net.getIn().readUTF());
        }
        return files;
    }
}
