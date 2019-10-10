


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import javafx.scene.control.ChoiceBox;
import tech.tablesaw.api.NumericColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.display.Browser;
import tech.tablesaw.plotly.traces.ScatterTrace;
import tech.tablesaw.plotly.traces.Trace;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Controller {
    public static String page;
    public static ArrayList<Figure> figureList = new ArrayList<Figure>();
    public static String GraphingType;
    public static String GraphingLayout;
    public static ArrayList<String> divName = new ArrayList<String>();


    @FXML
    private Button Btn1;


    @FXML
    private ChoiceBox<String> GraphType;

    @FXML
    private ChoiceBox<String> GraphLayout;


    @FXML
    private void initialize() {
        GraphType.getItems().addAll("Line Graph", "CandleStick", "Scatter");
        GraphType.setOnAction(e -> getChoice(GraphType));

        GraphLayout.getItems().addAll("Multipleplot", "Multiline");
        GraphLayout.setOnAction(e -> getVal(GraphLayout));

    }

    public String getVal(ChoiceBox<String> GraphLayout) {
        GraphingLayout = GraphLayout.getValue();
        return GraphLayout.getValue();
    }

    public String getChoice(ChoiceBox<String> GraphType) {
        GraphingType = GraphType.getValue();
        return GraphType.getValue();
    }

    @FXML
    private Button Btn4;

    @FXML
    private ListView FileList;
    @FXML
    private ListView FileName;


    public void Button1Action() {
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showOpenDialog(null);

        FileList.getItems().add(selectedFile.getAbsolutePath());
        String name = selectedFile.getName();
        name = name.substring(0, name.indexOf('.'));
        FileName.getItems().add(name);


    }


    public void Button4Action() throws IOException {
        List<String> list = FileList.getItems();
        List<String> listName = FileName.getItems();

        Trace[] traceList = new Trace[list.size()];
        Layout layout = getLayout(listName.get(0));
        if (GraphingLayout == "Multipleplot") {
            if (GraphingType == "Line Graph") {
                GraphingType = "ohlc";
                for (int i = 0; i < list.size(); i++) {
                    getDat(Table.read().csv(list.get(i)), figureList, listName.get(i), GraphingType);
                }


            } else if (GraphingType == "CandleStick") {
                GraphingType = "candlestick";
                for (int i = 0; i < list.size(); i++) {
                    getDat(Table.read().csv(list.get(i)), figureList, listName.get(i), GraphingType);

                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    getScatter(Table.read().csv(list.get(i)), figureList, listName.get(i));
                }


            }
        } else {
            if (GraphingType == "Line Graph") {
                GraphingType = "ohlc";
                for (int i = 0; i < list.size(); i++) {
                    traceList[i] = getTrace(Table.read().csv(list.get(i)), figureList, GraphingType, listName.get(i));
                }


            } else if (GraphingType == "CandleStick") {
                GraphingType = "candlestick";
                for (int i = 0; i < list.size(); i++) {
                    traceList[i] = getTrace(Table.read().csv(list.get(i)), figureList, GraphingType, listName.get(i));

                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    traceList[i] = getTrace(Table.read().csv(list.get(i)), figureList, GraphingType, listName.get(i));
                }


            }
            Figure figure = new Figure(getLayout(GraphingType), traceList);
            figureList.add(figure);
        }


        for (int i = 0; i < figureList.size(); i++) {
            divName.addAll(Collections.singleton(listName.get(i)));
        }
        page = makePage(figureList, divName);


        File outputFile = Paths.get("multiplot.html").toFile();

        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            fileWriter.write(page);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        new Browser().browse(outputFile);


    }

    public Trace getTrace(Table newTable, ArrayList<Figure> figureList, String types, String name) throws IOException {

        Trace t = ScatterTrace.builder(newTable.dateColumn("date"), newTable.nCol("open"), newTable.nCol("high"), newTable.nCol("low"), newTable.nCol("close")).name(name).type(types).build();


        return t;
    }

    public Layout getLayout(String title) throws IOException {


        Layout layout1 = Layout.builder().title(title)
                .xAxis(Axis.builder().title("date").showGrid(false).build()).build();

        return layout1;
    }

    public static void getDat(Table newTable, ArrayList<Figure> figureList, String title, String types) {

        Layout layout1 = Layout.builder().title(title)
                .xAxis(Axis.builder().title("date").showGrid(false).build()).build();


        Trace t = ScatterTrace.builder(newTable.dateColumn("date"), newTable.nCol("open"), newTable.nCol("high"), newTable.nCol("low"), newTable.nCol("close")).type(types).build();

        figureList.add(new Figure(layout1, t));
    }

    public static void getScatter(Table newTable, ArrayList<Figure> figureList, String title) {
        Layout layout1 = Layout.builder().title(title)
                .xAxis(Axis.builder().title("date").showGrid(false).build()).yAxis(Axis.builder().title(String.valueOf(newTable.column(1))).build()).build();
        Trace t = ScatterTrace.builder(newTable.dateColumn("date"), (NumericColumn<? extends Number>) newTable.column(1)).build();
        figureList.add(new Figure(layout1, t));
    }


    private static final String pageTop = "<html>" + System.lineSeparator() +
            "<head>" + System.lineSeparator() +
            "    <title>Multi-plot test</title>" + System.lineSeparator() +
            "    <script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>" + System.lineSeparator() +
            "</head>" + System.lineSeparator() +
            "<body>" + System.lineSeparator();


    private static final String pageBottom =
            "</body>" + System.lineSeparator() + "</html>";

    @FXML
    private static String makePage(ArrayList<Figure> myFigures, ArrayList<String> divisionName) {
        StringBuilder myPage = new StringBuilder();

        myPage.append(pageTop);

        for (int i = 0; i < myFigures.size(); i++) {
            myPage.append("<div id='" + divisionName.get(i) + "'>");
        }

        for (int i = 0; i < myFigures.size(); i++) {
            myPage.append(System.lineSeparator());
            myPage.append(myFigures.get(i).asJavascript(divisionName.get(i)));
        }

        myPage.append(pageBottom);
        return myPage.toString();
    }


}


