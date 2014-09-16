/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jssc_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import jssc.SerialPortList;

/**
 * FXML Controller class
 *
 * @author Admin
 */
public class FXMLDocumentController implements Initializable {

    private class RunAVR
            implements Runnable {

        @Override
        public void run() {
            try {
                Runtime rt = Runtime.getRuntime();
                String cmd;
                cmd = "avrdude -p m328p -c stk500v1 -v -U flash:w:blink.hex:i -b 57600";
                cmd = cmd + " -P ";
                cmd = cmd + portBox.getValue();
                Process proc;
                proc = rt.exec(cmd);
                InputStream stderr = proc.getErrorStream();
                InputStreamReader isr = new InputStreamReader(stderr);
                BufferedReader br = new BufferedReader(isr);
                String line;

                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    if (line.contains("stk500_getsync()")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                feedbackTxt.setText("LiProX Not connected");
                            }
                        });
                    }
                    if (line.contains("avrdude: ser_open()")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                feedbackTxt.setText("LiProX Not connected");
                            }
                        });
                    }
                    if (line.contains("bytes of flash verified")) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                feedbackTxt.setText("LiProX Updated");
                            }
                        });
                    }
                }
                int exitVal;

                exitVal = proc.waitFor();

                System.out.println("Process exitValue: " + exitVal);
            } catch (InterruptedException | IOException e) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }

    @FXML
    private Label feedbackTxt;

    ObservableList<String> portList;
    @FXML
    private ComboBox<String> portBox;

    private void detectPort() {

        portList = FXCollections.observableArrayList();

        String[] serialPortNames = SerialPortList.getPortNames(); //SerialPortList.getPortNames(Pattern.compile("tty.(COM|serial|usbmodem|LIPRO).*"));
        for (String name : serialPortNames) {
            System.out.println(name);
            portList.add(name);
        }
    }

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        detectPort();
        portBox.setItems(portList);
    }

    @FXML
    private void handleUpload(ActionEvent event) {
        try {
            feedbackTxt.setText("Uploading...");
            //new Thread(task).start();
            Thread t = new Thread(new RunAVR());
            t.start();
        } catch (Throwable tt) {
            tt.printStackTrace();
        }
    }
}