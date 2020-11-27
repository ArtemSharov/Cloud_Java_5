import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

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
    ListView<String> clientFilesList;
    @FXML
    ListView<String> serverFilesList;
    @FXML
    TextField login;
    @FXML
    PasswordField password;
    @FXML
    Label msg;
    @FXML
    Button enter;
    @FXML
    Button logout;

    Network net = new Network();


    @Override
    public void initialize(URL location, ResourceBundle resources){
        net.start();
        System.out.println("Connect");


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
        Platform.runLater(()->{
        try {
            serverFilesList.getItems().clear();
            serverFilesList.getItems().addAll(getServerFiles());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        });
    }

    private List<String> getServerFiles() throws IOException, ClassNotFoundException {
        List<String> files = new ArrayList<>();
        String cmd = "/getFileList";
        net.getOut().writeObject(cmd);
        net.getOut().flush();

        int listSize = (int)net.getIn().readObject();
        for (int i = 0; i < listSize; i++) {
            files.add((String)net.getIn().readObject());
        }
        return files;

    }

    public void uploadFile() throws IOException {
        MultipleSelectionModel<String> selectionClientFile = clientFilesList.getSelectionModel();
        System.out.println(selectionClientFile.getSelectedItem());
        if (Files.exists(Paths.get("Client_storage/" + selectionClientFile.getSelectedItem()))) {
            FileMessage fm = new FileMessage(Paths.get("Client_storage/" + selectionClientFile.getSelectedItem()));
            net.getOut().writeObject(fm);
            net.getOut().flush();
            serverList();
        }
    }

    public void downloadFile() throws IOException, ClassNotFoundException {
        MultipleSelectionModel<String> selectionServerFile = serverFilesList.getSelectionModel();
        FileRequest fr = new FileRequest(selectionServerFile.getSelectedItem());
        System.out.println(fr.getName());
        net.getOut().writeObject(fr);
        net.getOut().flush();
        AbstractMessage am = net.readObject();
        if (am instanceof FileMessage) {
            FileMessage fm = (FileMessage) am;
            Files.write(Paths.get("Client_storage/" + fm.getFileName()), fm.getData(),
                    StandardOpenOption.CREATE);
            refreshLocalFilesList();
        }
    }

    public void loginUser() throws IOException, ClassNotFoundException {
        LoginMessage lm = new LoginMessage(login.getText(), password.getText());
        net.getOut().writeObject(lm);
        net.getOut().flush();
        lm = (LoginMessage) net.getIn().readObject();
        if(lm.getAuthorized()) {
            msg.setText("Hi! " + lm.getLogin());
            refreshLocalFilesList();
            serverList();
            enter.setDisable(true);
            logout.setDisable(false);

        } else {
            msg.setText("Login or password incorrect");
        }

    }

    public void logOut(){
        msg.setText("");
        login.clear();
        password.clear();
        serverFilesList.getItems().clear();
        clientFilesList.getItems().clear();
        enter.setDisable(false);
        logout.setDisable(true);

    }

}
