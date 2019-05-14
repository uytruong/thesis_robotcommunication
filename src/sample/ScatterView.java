package sample;

import javafx.collections.FXCollections;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;


import java.util.ArrayList;
import java.util.List;

public class ScatterView {
    private byte[] receivedData;
    private int xTargetPoint;
    private int yTargetPoint;
    private ScatterChart<Number, Number> scatterChart;
    private List<ScatterChart.Series<Number, Number>> dataSeriesList = new ArrayList<>();


    public ScatterView(byte[] receivedData, int xTargetPoint, int yTargetPoint, ScatterChart<Number, Number> scatterChart) {
        this.receivedData = receivedData;
        this.xTargetPoint = xTargetPoint;
        this.yTargetPoint = yTargetPoint;
        this.scatterChart = scatterChart;
        initDataSeriesList();
    }

    private void initDataSeriesList() {
        for (int idx = 0; idx < 5; idx++) {
            dataSeriesList.add(new ScatterChart.Series<>());
        }
        dataSeriesList.get(SymbolViewPriority.TASK).setName("Task");
        dataSeriesList.get(SymbolViewPriority.ROBOT_UP).setName("Robot Up");
        dataSeriesList.get(SymbolViewPriority.ROBOT_DOWN).setName("Robot Down");
        dataSeriesList.get(SymbolViewPriority.ROBOT_LEFT).setName("Robot Left");
        dataSeriesList.get(SymbolViewPriority.ROBOT_RIGHT).setName("Robot Right");
    }

    private void prepareDataToDisplay() {
        final byte BEGIN = 0x21; // 0x21 = !
        final byte SIG = 0x40; // 0x40 = @
        final byte END = 0x23; // 0x23 = #
        // Prepare data for target point
        ScatterChart.Data<Number, Number> targetPoint = new ScatterChart.Data<Number, Number>(xTargetPoint, yTargetPoint);
        dataSeriesList.get(SymbolViewPriority.TASK).getData().add(targetPoint);
        if (receivedData != null) {
            if (receivedData[0] == BEGIN && receivedData[7] == SIG && receivedData[8] == END) {
                System.out.println("===DEBUG===");
                // Prepare data for robot point
                ScatterChart.Data<Number, Number> robotPoint = null;
                if (receivedData[1] == 0x11 && receivedData[2] == 0x22 && receivedData[3] == 0x33 &&receivedData[4] == 0x44 && receivedData[5] == 0x55) {
                    robotPoint = new ScatterChart.Data<Number, Number>(1, 1);
                }
                else if (receivedData[1] == 0x01 && receivedData[2] == 0x02 && receivedData[3] == 0x03 &&receivedData[4] == 0x04 && receivedData[5] == 0x05) {
                    robotPoint = new ScatterChart.Data<Number, Number>(1, 2);
                }
                else {
                    robotPoint = new ScatterChart.Data<Number, Number>(0, 0);
                }

                byte robotHeading = receivedData[6];
                switch (robotHeading) {
                    case 0x00:
                        dataSeriesList.get(SymbolViewPriority.ROBOT_UP).getData().add(robotPoint);
                        break;
                    case 0x01:
                        dataSeriesList.get(SymbolViewPriority.ROBOT_DOWN).getData().add(robotPoint);
                        break;
                    case 0x02:
                        dataSeriesList.get(SymbolViewPriority.ROBOT_LEFT).getData().add(robotPoint);
                        break;
                    case 0x03:
                        dataSeriesList.get(SymbolViewPriority.ROBOT_RIGHT).getData().add(robotPoint);
                        break;
                }
            }
        }
    }

    public void display() {
        prepareDataToDisplay();
        scatterChart.setData(FXCollections.<XYChart.Series<Number, Number>>observableArrayList());
        //Setting the data to scatter chart
        for (ScatterChart.Series<Number, Number> dataSeries : dataSeriesList) {
            scatterChart.getData().add(dataSeries);
        }
    }



    private class SymbolViewPriority {
        /**
         * The scatter plot view data from idx = 0 to dataSeriesList.size()
         * The less priority value the more priority in view
         * */
        private static final int TASK = 0;
        private static final int ROBOT_UP     = 1;
        private static final int ROBOT_DOWN   = 2;
        private static final int ROBOT_LEFT   = 3;
        private static final int ROBOT_RIGHT  = 4;

    }
}
