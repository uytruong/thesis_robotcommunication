package sample;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    public TextField txtPortName;
    public TextField txtNumberOfMotion;
    public TextField txtLoraAddress;
    public TextField txtLoraChannel;
    public TextField txtXTargetPoint;
    public TextField txtYTargetPoint;

    public TableView<TableBean> tableViewMotionList;
    public TableColumn<TableBean, ComboBox> tableColumnMotion;
    public TableColumn<TableBean, String> tableColumnID;
    public Label lblConnectionStatus;
    ObservableList<TableBean> motionList = FXCollections.observableArrayList();

    int numberOfMotion = 0;
    SerialPort mySerialPort;
    byte[] receivedData;
    Timeline timeline;

    /**
     * Scatter chart
     */
    public NumberAxis xAxis;
    public NumberAxis yAxis;
    public ScatterChart<Number, Number> scatterChart;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTimeline();
        timeline.play();

        xAxis.setAutoRanging(false);
        yAxis.setAutoRanging(false);

        final DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setColor(Color.GREY);
        scatterChart.setEffect(shadow);
    }

    public void btnConnectClick(ActionEvent event) {
        mySerialPort = SerialPort.getCommPort(txtPortName.getText()); // device name TODO: must be changed
        mySerialPort.setComPortParameters(9600, 8, 1, 0); // default connection settings for Arduino
        mySerialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // block until bytes can be written

        if (mySerialPort.openPort()) {
            System.out.println("Port is open :)");
            lblConnectionStatus.setText("Connect successfully!");
        } else {
            System.out.println("Failed to open port :(");
            lblConnectionStatus.setText("Connect failed!");
            return;
        }

        addReceivedDataListener();
    }

    public void btnCreateClick(ActionEvent event) {
        numberOfMotion = Integer.parseInt(txtNumberOfMotion.getText());
        for (int i = 0; i < numberOfMotion; i++) {
            ComboBox<String> motion = new ComboBox<>();
            motion.getItems().add("None");
            motion.getItems().add("Rotate Right");
            motion.getItems().add("Rotate Left");
            motion.getItems().add("Speed Up");
            motion.getItems().add("Move Constant");
            motion.getItems().add("Speed Down");
            motion.getItems().add("Jump");
            motion.getItems().add("Rotate Backward");
            motionList.add(new TableBean(Integer.toString(i + 1), motion));
        }
        tableViewMotionList.setItems(motionList);
        tableColumnID.setCellValueFactory(new PropertyValueFactory<TableBean, String>("id"));
        tableColumnMotion.setCellValueFactory(new PropertyValueFactory<TableBean, ComboBox>("motion"));
    }

    public void btnRunClick(ActionEvent event) {
        final byte BEGIN = 0x21; // 0x21 = !
        final byte SIG = 0x40; // 0x40 = @
        final byte END = 0x23; // 0x23 = #
        byte[] loraAddress = new byte[2];
        byte[] loraChannel = new byte[1];
        byte cmdType = 0x00;
        byte cmdOffset = 0x00;
        int frameLength = numberOfMotion + 8;
        byte[] completeFrame = new byte[frameLength];

        loraAddress = txtLoraAddress.getText().getBytes();
        loraChannel = txtLoraChannel.getText().getBytes();
        completeFrame[0] = (byte) (loraAddress[0] - 0x30);
        completeFrame[1] = (byte) (loraAddress[1] - 0x30);
        completeFrame[2] = (byte) (loraChannel[0] - 0x30);
        completeFrame[3] = BEGIN;
        completeFrame[4] = cmdType;
        completeFrame[5] = cmdOffset;
        for (int idx = 0; idx < numberOfMotion; idx++) {
            String motionType = tableViewMotionList.getItems().get(idx).getMotion().getSelectionModel().getSelectedItem();
            switch (motionType) {
                case "None":
                    completeFrame[idx + 6] = 0x00;
                    break;
                case "Rotate Right":
                    completeFrame[idx + 6] = 0x01;
                    break;
                case "Rotate Left":
                    completeFrame[idx + 6] = 0x02;
                    break;
                case "Speed Up":
                    completeFrame[idx + 6] = 0x03;
                    break;
                case "Move Constant":
                    completeFrame[idx + 6] = 0x04;
                    break;
                case "Speed Down":
                    completeFrame[idx + 6] = 0x05;
                    break;
                case "Jump":
                    completeFrame[idx + 6] = 0x06;
                    break;
                case "Rotate Backward":
                    completeFrame[idx + 6] = 0x07;
                    break;
                default:
                    break;
            }
        }
        completeFrame[frameLength - 2] = SIG;
        completeFrame[frameLength - 1] = END;

//        mySerialPort.writeBytes(completeFrame, completeFrame.length);
//        System.out.println("Write bytes " + "\n");

        for (int i = 0; i < 20; i++) {
            mySerialPort.writeBytes(completeFrame, completeFrame.length);
            System.out.println("Write bytes " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnDeleteClick(ActionEvent event) {
    }

    public void addReceivedDataListener() {
        mySerialPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_RECEIVED; }
            @Override
            public void serialEvent(SerialPortEvent event)
            {
                long startTime = System.nanoTime();
                receivedData = event.getReceivedData();
                System.out.println("Received data of size: " + receivedData.length);
//                String str = new String(receivedData);
//                System.out.println(str);
                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
//                System.out.println("Elapsed time: " + elapsedTime + " ns");
//                Platform.runLater(new Runnable() {
//                    updateAndViewMapToScatter();
//                });
            }
        });
    }

    private void updateAndViewMapToScatter() {
        int xTargetPoint = Integer.parseInt(txtXTargetPoint.getText());
        int yTargetPoint = Integer.parseInt(txtYTargetPoint.getText());
        ScatterView scatterView = new ScatterView(receivedData, xTargetPoint, yTargetPoint, scatterChart);
        scatterView.display();
    }

    private void initializeTimeline(){
        timeline = new Timeline(new KeyFrame(Duration.millis(1000), ev -> {
            updateAndViewMapToScatter();
        }));
        timeline.setCycleCount(400);
    }
}
