package com.geekbrains.cloud.client;

import com.geekbrains.cloud.command.*;
import com.geekbrains.cloud.command.Error;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @FXML
    VBox topElement;

    private Path localStorage;

//    private static int SIZEBLOCK = 1024*1024*5;//5 метров - размер для оставного файла

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        localStorage =  Paths.get("client_storage");
        setAuthenticated(false);
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage am = Network.readObject();
                    if (am instanceof FileMessage) {
                        System.out.println("Пришло FileMessage");
                        FileMessage fm = (FileMessage) am;
                        Files.write(Paths.get(localStorage.toString() + "/" + fm.getFilename()), fm.getData(),
                                fm.getFirstPart() ? StandardOpenOption.CREATE : StandardOpenOption.APPEND);
                        if(fm.getEndPart())
                            refreshLocalFilesList();
                    }
                    if(am  instanceof ResultOfAuto){
                        System.out.println("Пришло ResultOfAuto");
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
                        System.out.println("Пришло FilesListRezult");

                        FilesListRezult flr = (FilesListRezult) am;
                        System.out.println("Получили список файлов(количество="+flr.getFileList().size()+")");
                        Platform.runLater(() -> {
                            filesListServer.getItems().clear();
                            filesListServer.getItems().addAll(flr.getFileList());
                        });
                    }
                    if(am instanceof Error) {
                        System.out.println("Пришло Error");
                        Platform.runLater(() -> {

                            Error err = (Error) am;
                            Alert errAlert = new Alert(Alert.AlertType.ERROR);
                            errAlert.setTitle("Ошибка сервера");
                            errAlert.setContentText(err.getMessage());
                            errAlert.showAndWait();
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
        Platform.runLater(() -> {
            authPanel.setVisible(!authenticated);
            authPanel.setManaged(!authenticated);
            filesPanel.setVisible(authenticated);
            filesPanel.setManaged(authenticated);
            ((Stage) topElement.getScene().getWindow()).setTitle(authenticated ? "Box Client" : "Box Client - нет подключения");
        });
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
            System.out.println();
            System.out.println("Отправляем на сервер " + selName);
            try {
                Path path = Paths.get(localStorage.toString() +"/"+selName);
                FileMessage fm = new FileMessage(path);
                while(fm.next()) {
                    Network.sendMsg(fm);
                }
            }


            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void pressOnChangedBtn(ActionEvent actionEvent) {
        final DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите новую директорию для локального хранилища");
        directoryChooser.setInitialDirectory(localStorage.toFile());
        localStorage =  directoryChooser.showDialog(topElement.getScene().getWindow()).toPath();
        System.out.println(localStorage.toString());
        refreshLocalFilesList();

    }


    private void refreshLocalFilesList() {
        System.out.println("refreshLocalFilesList");
        Platform.runLater(() -> {
            try {
                filesListLocal.getItems().clear();
                Files.list(localStorage)
                        .filter(n -> !Files.isDirectory(n))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> filesListLocal.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void pressOnDeleteBtn(ActionEvent actionEvent) {
        String selName = filesListServer.getSelectionModel().getSelectedItem();
        if ((selName!=null)&&(selName.length() > 0)) {
            System.out.println("Запрашиваем в сервера" + selName);
            Network.sendMsg(new FileDelete(selName));
        }

    }
}
