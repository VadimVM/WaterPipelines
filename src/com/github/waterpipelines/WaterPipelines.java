package com.github.waterpipelines;

import com.github.waterpipelines.models.PipeModel;
import com.github.waterpipelines.models.TestDataModel;
import com.github.waterpipelines.processor.PipelineGraph;
import com.github.waterpipelines.processor.PipelineNode;
import com.github.waterpipelines.services.GraphInitializer;
import com.github.waterpipelines.storage.DBManager;
import com.github.waterpipelines.utils.CSVUtils;
import com.github.waterpipelines.utils.CsvPipeLoader;
import com.github.waterpipelines.utils.TestDataLoader;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WaterPipelines {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите путь к .csv файлу с исходными данными");
        String input = reader.readLine();
        CsvPipeLoader pipeLoader = new CsvPipeLoader(input);
        System.out.println("Введите путь к .csv файлу с данными для проверки");
        String testPut = reader.readLine();
        TestDataLoader testDataLoader = new TestDataLoader(testPut);
        reader.close();
        try {
            ArrayList<PipeModel> pipes = pipeLoader.Load();
            ArrayList<TestDataModel> testData = testDataLoader.Load();
            Class.forName("org.h2.Driver").getDeclaredConstructor().newInstance();
            Connection conn = DriverManager.getConnection("jdbc:h2:./test", "user", "");
            DBManager db = new DBManager(conn);
            db.Init();
            for (PipeModel pipe:pipes) {
                db.Insert(pipe);
            }

            File file = new File("output.csv");
            FileWriter outputfile = new FileWriter(file);
            for (TestDataModel t : testData) {
                List<PipelineNode> nodes = GraphInitializer.SetUpNodes(pipes);
                PipelineGraph graph = GraphInitializer.SetUpGraph(nodes);
                int distance = graph.getDistance(t.getIdA(), t.getIdB());
                boolean exists = distance != Integer.MAX_VALUE;
                String[] result = new String[2];
                result[0] = String.valueOf(exists);
                if (exists) {
                    result[1] = String.valueOf(distance);
                } else {
                    result[1] = "";
                }
                CSVUtils.writeLine(outputfile, Arrays.asList(result), ';');
            }
            outputfile.flush();
            outputfile.close();
        } catch (IOException | SQLException | ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


}
