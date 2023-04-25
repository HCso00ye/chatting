package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MsgType;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));

        stage.setScene(new Scene(fxmlLoader.load()));
        Controller controller = fxmlLoader.getController();
        stage.setTitle("Chatting Client");
        stage.setOnCloseRequest(windowEvent -> {
            try {
                controller.moos.writeObject(new Message(System.currentTimeMillis(),controller.username,"Server","exit", MsgType.EXIT));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        stage.show();


    }
}
