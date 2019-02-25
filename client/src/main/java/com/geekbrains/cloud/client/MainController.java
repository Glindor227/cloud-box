package com.geekbrains.cloud.client;

import com.geekbrains.cloud.command.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    ListView<String> filesListLocal,filesListServer;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passField;

    @FXML
    HBox authPanel,filesPanel;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthenticated(false);
        Network.start();

        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get("client_storage/" + fm.getFilename()), fm.getData(), StandardOpenOption.CREATE);
                        refreshLocalFilesList();
                    }
                    if(am  instanceof ResultOfAuto){
                        ResultOfAuto ar = (ResultOfAuto) am;
                        if (ar.getRezult()) {
                            setAuthenticated(true);
                            System.out.println("Авторизация прошла");
                            refreshLocalFilesList();
                        }else
                        {
                            System.out.println("Авторизация неудачна");
                        }

                    }
                    if(am instanceof FilesListRezult) {
                        FilesListRezult flr = (FilesListRezult) am;
                        System.out.println("Получили список файлов(количество="+flr.getFileList().size()+")");
                        Platform.runLater(() -> {
                            filesListServer.getItems().clear();
                            filesListServer.getItems().addAll(flr.getFileList());
                        });
                    }


                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Network.stop();
            }
        });
        t.setDaemon(true);
        t.start();
//        refreshLocalFilesList();
    }

    private void setAuthenticated(boolean authenticated) {
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        filesPanel.setVisible(authenticated);
        filesPanel.setManaged(authenticated);
    }



    public void sendAuth(ActionEvent actionEvent) {
        Network.sendMsg(new SetAuto(loginField.getText(),passField.getText()));
        loginField.clear();
        passField.clear();

    }

    //скачать файл
    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        String selName = filesListServer.getSelectionModel().getSelectedItem();
        if ((selName!=null)&&(selName.length() > 0)) {
            System.out.println("Запрашиваем в сервера" + selName);
            Network.sendMsg(new FileRequest(selName));
        }
    }

    public void pressOnSendBtn(ActionEvent actionEvent) {
        String selName = filesListLocal.getSelectionModel().getSelectedItem();
        if ((selName!=null)&&(selName.length() > 0)) {
            System.out.println("Отправляем на сервер" + selName);
            try {
                FileMessage fm = new FileMessage(Paths.get("client_storage/"+selName));
                Network.sendMsg(fm);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


//сжимается. но как?
    private void refreshLocalFilesList() {
        System.out.println("refreshLocalFilesList");
/*        if (Platform.isFxApplicationThread()) {
            try {
                filesListLocal.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListLocal.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
*/         {
            Platform.runLater(() -> {
                try {
                    filesListLocal.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> filesListLocal.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
