package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.MsgType;
import cn.edu.sustech.cs209.chatting.common.IOs;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;


import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
  public Socket socket;
  private String username;
  public IOs.MyObjectInputStream ois;
  public IOs.MyObjectOutputStream os;
  private Controller controller;


  Client(String username, Controller controller) throws IOException {
    socket = new Socket("localhost", 6666);
    this.username = username;

    this.controller = controller;
    ois = new IOs.MyObjectInputStream(this.socket.getInputStream());
    os = new IOs.MyObjectOutputStream(this.socket.getOutputStream());
    Thread cw = new Thread(new ClientWriter(socket, username, os, this.controller));
    Thread cr = new Thread(new ClientReader(socket, ois, os, this.controller));
    cw.start();
    cr.start();
  }

}

class ClientReader implements Runnable {
  private Socket socket;
  IOs.MyObjectInputStream ois;
  IOs.MyObjectOutputStream os;
  Controller controller;


  public ClientReader(Socket socket, IOs.MyObjectInputStream ois, IOs.MyObjectOutputStream os, Controller controller) {
    this.socket = socket;
    this.ois = ois;
    this.os = os;
    this.controller = controller;
  }

  @Override
  @FXML
  public void run() {
    try {
      while (true) {
        Message message = (Message) ois.readObject();
        //判断message的type
        switch (message.getType()) {
          case COMMAND:
            break;

          case TALK:
            System.out.println("message：" + message.getData());
            controller.setMsgLV(message);
            break;

          case REQ:

            String[] reqString = message.getData().split("~");
            String a = reqString[reqString.length - 1];
            String[] reqStrin = Arrays.copyOf(reqString, reqString.length - 1);
            controller.userSet.addAll(Arrays.asList(reqStrin));
            controller.setLeftLV(reqStrin);
            controller.setCuNum(a);
            break;

          case EXIT:
            controller.setCuNum(message.getData());
            controller.exit();
            break;

          case EXIT_NO_KEEP:
            String[] reqString1 = message.getData().split("~");
            String a1 = reqString1[reqString1.length - 1];
            String[] reqStrin1 = Arrays.copyOf(reqString1, reqString1.length - 1);
            controller.userSet.remove(message.getSentBy());
            controller.setLeftLV(reqStrin1);
            controller.setCuNum(a1);
            break;

          case SERVER_EXIT:
            Platform.runLater(() -> {
              Alert alert1 = new Alert(Alert.AlertType.INFORMATION);
              alert1.setTitle("Information Dialog");
              alert1.setHeaderText("Server exit");
              alert1.setContentText("Server exit! You will exit in several seconds···");
              alert1.showAndWait();
              try {
                os.writeObject(new Message(System.currentTimeMillis(), controller.username, "Server", "confirm server exit", MsgType.SERVER_EXIT));
                os.flush();
                System.exit(0);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
            break;
          case NOT_ALLOW_LOGIN:
            controller.ntAllowLoginFeedBk();
            break;
          case R_FIAL:
            controller.r_fail();
            break;
          case EXIT_FROM_GROUP:
            String group = message.getSendTo().split(":")[0];
            String deleteUsername = message.getSendTo().split(":")[1];
            controller.deleteChatOb(message.getSendTo(), message.getData());
            break;

          default:
            break;
        }

      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

}

class ClientWriter implements Runnable {
  private Socket socket;
  String username;
  IOs.MyObjectOutputStream os;
  Controller controller;


  public ClientWriter(Socket socket, String username, IOs.MyObjectOutputStream os, Controller controller) {
    this.socket = socket;
    this.username = username;

    this.os = os;
    this.controller = controller;
  }

  @Override
  public void run() {
    try {
      int controlNum = 0;
      while (true) {
        if (controlNum == 0) {
          Message message = new Message(System.currentTimeMillis(),
                  username, "Server", "login", MsgType.COMMAND);
          os.writeObject(message);
          socket.getOutputStream().flush();
          controlNum++;
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
