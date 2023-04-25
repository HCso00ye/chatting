package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller implements Initializable {

  public IOs.MyObjectOutputStream moos;

  public Set<String> userSet = new HashSet<>();
  ObservableList<String> stringObservableList;
  ObservableList<Message> mesObservableList = FXCollections.observableArrayList();

  @FXML
  private TextArea inputArea;

  @FXML
  ListView<Message> chatContentList;
  String username;

  @FXML
  private Label currentUsername;
  @FXML
  private Label talkWith;
  private String talkTo = null;

  @FXML
  public ListView<String> chatList;

  @FXML
  public Label currentOnlineCnt;


  String reg;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Dialog<String> dialog1 = new TextInputDialog();
    dialog1.setTitle("Login-username");
    dialog1.setHeaderText(null);
    dialog1.setContentText("Username:");
    Optional<String> input1 = dialog1.showAndWait();

    if (input1.isPresent() && !input1.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
      RLStageOperate();
      username = input1.get();
      setCurrentUsername(username);
      try {
        Client client = new Client(username, this);
        moos = client.os;

      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println("usm=:" + Users.user_socket_map);


    } else {
      System.out.println("Empty username");
      Platform.exit();
    }
    String displayTalkTo = "talking to: " + talkTo;
    talkWith.setText(displayTalkTo);

    chatList.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getClickCount() == 2) {
        System.out.println(chatList.getSelectionModel().getSelectedItem().getClass());
        System.out.println(chatList.getItems().getClass());
        talkTo = chatList.getSelectionModel().getSelectedItem();
        privateChatHelper();
      }
    });

    chatContentList.setCellFactory(new MessageCellFactory());
    chatContentList.setItems(mesObservableList);
  }

  @FXML
  public void createPrivateChat() {

    AtomicReference<String> user = new AtomicReference<>();

    Stage stage = new Stage();
    ComboBox<String> userSel = new ComboBox<>();

    for (String s : userSet) {
      if (!s.equals(username)) userSel.getItems().add(s);
    }

    Button okBtn = new Button("OK");
    okBtn.setOnAction(e -> {
      user.set(userSel.getSelectionModel().getSelectedItem());
      //将选中的聊天对象设置为 talkto
      talkTo = userSel.getSelectionModel().getSelectedItem();
      privateChatHelper();
      stage.close();
    });

    HBox box = new HBox(10);
    box.setAlignment(Pos.CENTER);
    box.setPadding(new Insets(20, 20, 20, 20));
    box.getChildren().addAll(userSel, okBtn);
    stage.setScene(new Scene(box));
    stage.showAndWait();

  }

  /**
   * A new dialog should contain a multi-select list, showing all user's name.
   * You can select several users that will be joined in the group chat, including yourself.
   * <p>
   * The naming rule for group chats is similar to WeChat:
   * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
   * UserA, UserB, UserC... (10)
   * If there are <= 3 users: do not display the ellipsis, for example:
   * UserA, UserB (2)
   */
  @FXML
  public void createGroupChat() {
  }


  /**
   * Sends the message to the <b>currently selected</b> chat.
   * <p>
   * Blank messages are not allowed.
   * After sending the message, you should clear the text input field.
   */
  @FXML
  public void doSendMessage() throws IOException {

    if (inputArea.getText() != null) {
      String inputFromKeyBoard = inputArea.getText();
      inputArea.setText("");
      Message message = new Message(System.currentTimeMillis(), username, talkTo, inputFromKeyBoard, MsgType.TALK);
      moos.writeObject(message);
      moos.flush();

      Platform.runLater(() -> {
        mesObservableList.add(message);
        System.out.println(mesObservableList);
        chatContentList.setItems(mesObservableList);
      });
    }

  }

  public void createNewGcontroller(String s, String sentBy, String data) {
  }

  public void deleteChatOb(String sendTo, String data) {
  }

  /**
   * You may change the cell factory if you changed the design of {@code Message} model.
   * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
   */
  private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
    @Override
    public ListCell<Message> call(ListView<Message> param) {
      return new ListCell<Message>() {

        @Override
        public void updateItem(Message msg, boolean empty) {
          super.updateItem(msg, empty);
          if (empty || Objects.isNull(msg)) {
            setText(null);
            setGraphic(null);
            return;
          }

          HBox wrapper = new HBox();
          Label nameLabel = new Label(msg.getSentBy());
          Label msgLabel = new Label(msg.getData());

          nameLabel.setPrefSize(50, 20);
          nameLabel.setWrapText(true);
          nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

          if (username.equals(msg.getSentBy())) {
            wrapper.setAlignment(Pos.TOP_RIGHT);
            wrapper.getChildren().addAll(msgLabel, nameLabel);
            msgLabel.setPadding(new Insets(0, 20, 0, 0));
          } else {
            wrapper.setAlignment(Pos.TOP_LEFT);
            wrapper.getChildren().addAll(nameLabel, msgLabel);
            msgLabel.setPadding(new Insets(0, 0, 0, 20));
          }

          setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
          setGraphic(wrapper);
        }
      };
    }
  }

  public void ntAllowLoginFeedBk() {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("only 2 people");
      alert.setHeaderText("Invalid message");
      alert.setContentText("You entered an invalid username\nplease enter again later");
      alert.showAndWait();
      System.exit(0);
    });
  }

  public void r_fail() {
    Platform.runLater(() -> {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle("repetitive username");
      alert.setHeaderText("repetitive username");
      alert.setContentText("repetitive username : try another username!");
      alert.showAndWait();
      System.exit(0);
    });
  }


  public void setCurrentUsername(String name) {
    currentUsername.setText("Current User: " + name);
  }


  public void setCuNum(String a) {
    Platform.runLater(() -> currentOnlineCnt.setText("Online:" + a));
  }


  public void setLeftLV(String[] string) {
    Platform.runLater(() -> {
      ArrayList<String> str = new ArrayList<>();
      for (String s : string) {
        if (!s.equals(username)) {
          str.add(s);
        }
      }
      String[] sss = new String[str.size()];
      for (int i = 0; i < str.size(); i++) {
        sss[i] = str.get(i);
      }
      stringObservableList = FXCollections.observableArrayList(Arrays.asList(sss));
      chatList.setItems(stringObservableList);
    });
  }

  //用于更新聊天内容
  public void setMsgLV(Message message) {
    Platform.runLater(() -> {
      mesObservableList.add(message);
      chatContentList.setItems(mesObservableList);
      System.out.println("更新聊天");
    });
  }

  //用于在切换聊天对象时重新刷新聊天
  public void reWriteMsgLV() {
    Platform.runLater(() -> {
      mesObservableList = FXCollections.observableArrayList();
      chatContentList.setItems(mesObservableList);
    });
  }

  public void privateChatHelper() {

    try {
      moos.writeObject(new Message(System.currentTimeMillis(), username, talkTo, "talkingTo", MsgType.TALKINGTO));
      System.out.println("talking to success");
      reWriteMsgLV();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    talkWith.setText("talking to: " + talkTo);
  }


  public void exit() {

  }

  public void RLStageOperate() {
    Platform.runLater(() -> {
      Stage stage = new Stage();
      VBox vbox = new VBox();
      HBox hbox = new HBox();
      Button rb1 = new Button("Register");
      rb1.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
          reg = "register";
          stage.close();
        }
      });

      Button rb2 = new Button("Login");
      rb2.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent actionEvent) {
          reg = "login";
          stage.close();
        }
      });
      hbox.getChildren().addAll(rb1, rb2);
      ToggleGroup group = new ToggleGroup();
      Label label = new Label("第一次进入请选择“Register”");
      label.setWrapText(true);
      vbox.getChildren().addAll(label, hbox);
      stage.setScene(new Scene(vbox));
    });

  }
}
