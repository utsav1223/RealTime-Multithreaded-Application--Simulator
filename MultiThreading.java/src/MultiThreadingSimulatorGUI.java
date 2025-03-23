import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Threads execution Gantt Chart
class GanttChart {
    int threadId, startTime, endTime;

    public GanttChart(int threadId, int startTime, int endTime) {
        this.threadId = threadId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "Thread " + threadId + ": Start Time = " + startTime + " ms, End Time = " + endTime + " ms";
    }
}

// Represents the state of a thread
class ThreadState {
    int id, arrivalTime, burstTime, remainingTime;

    public ThreadState(int id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }
}

public class MultiThreadingSimulatorGUI {
    private JFrame frame;
    private JTextArea logArea;
    private int threadCount;
    private int modelChoice;
    private int executionTime;
    private int timeQuantum;
    private List<GanttChart> ganttChartEntries = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MultiThreadingSimulatorGUI::new);
    }

    public MultiThreadingSimulatorGUI() {
        setupGUI();
    }

    private void setupGUI() {
        frame = new JFrame("Multi-threading Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);

        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);

        JButton startButton = new JButton("Start Simulation");
        startButton.addActionListener(e -> startSimulation());

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(startButton, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private void logMessage(String message) {
        logArea.append(message + "\n");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("execution_logs.txt", true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startSimulation() {
        logMessage("==============================");
        logMessage("Welcome to the Simulator");
        logMessage("==============================");

        do {
            threadCount = Integer.parseInt(JOptionPane.showInputDialog(
                    frame, "Enter the number of threads (1 to 6):"));
        } while (threadCount < 1 || threadCount > 6);

        modelChoice = Integer.parseInt(JOptionPane.showInputDialog(
                frame, "Choose the threading model:\n1. Many-to-One\n2. Many-to-Many with Round-Robin\n3. One-to-One"));

        if (modelChoice == 1) {
            executionTime = Integer.parseInt(JOptionPane.showInputDialog(
                    frame, "Enter execution time for each thread (in ms, 0 to 3000):"));
            manyToOne();
        } else if (modelChoice == 2) {
            timeQuantum = Integer.parseInt(JOptionPane.showInputDialog(
                    frame, "Enter time quantum for Round-Robin scheduling (in ms, 0 to 2000):"));
            List<ThreadState> threads = new ArrayList<>();
            for (int i = 1; i <= threadCount; i++) {
                int arrivalTime = Integer.parseInt(JOptionPane.showInputDialog(
                        frame, "Enter arrival time for Thread " + i + " (in ms, 0 to 6000):"));
                int burstTime = Integer.parseInt(JOptionPane.showInputDialog(
                        frame, "Enter burst time for Thread " + i + " (in ms, 0 to 6000):"));
                threads.add(new ThreadState(i, arrivalTime, burstTime));
            }
            manyToMany(threads);
        } else if (modelChoice == 3) {
            oneToOne();
        }

        logMessage("Simulation completed. Thank you for using the simulator!");
    }

    private void manyToOne() {
        logMessage("\n[Executing Model: Many-to-One]");
        logMessage("==============================");
        int currentTime = 0;

        for (int i = 1; i <= threadCount; i++) {
            logMessage("Thread " + i + " is executing for " + executionTime + " ms.");
            ganttChartEntries.add(new GanttChart(i, currentTime, currentTime + executionTime));
            currentTime += executionTime;
        }
        saveGanttChartToFile();
    }

    private void manyToMany(List<ThreadState> threads) {
        logMessage("\n[Executing Model: Many-to-Many with Round-Robin]");
        logMessage("==============================");
        int currentTime = 0;
        boolean progressMade;

        do {
            progressMade = false;
            for (ThreadState thread : threads) {
                if (thread.remainingTime > 0 && thread.arrivalTime <= currentTime) {
                    logMessage("Thread " + thread.id + " is executing...");
                    int execTime = Math.min(timeQuantum, thread.remainingTime);
                    ganttChartEntries.add(new GanttChart(thread.id, currentTime, currentTime + execTime));
                    currentTime += execTime;
                    thread.remainingTime -= execTime;
                    if (thread.remainingTime == 0) {
                        logMessage("Thread " + thread.id + " has completed execution.");
                    }
                    progressMade = true;
                }
            }
            if (!progressMade) {
                currentTime += 100;
            }
        } while (threads.stream().anyMatch(t -> t.remainingTime > 0));
        saveGanttChartToFile();
    }

    private void oneToOne() {
        logMessage("\n[Executing Model: One-to-One]");
        logMessage("==============================");
        int currentTime = 0;
        for (int i = 1; i <= threadCount; i++) {
            logMessage("Thread " + i + " is executing independently.");
            ganttChartEntries.add(new GanttChart(i, currentTime, currentTime + 500));
            currentTime += 500;
        }
        saveGanttChartToFile();
    }

    private void saveGanttChartToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("gantt_chart.txt"))) {
            writer.write("=== Gantt Chart ===\n");
            for (GanttChart entry : ganttChartEntries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        logMessage("Gantt chart saved to gantt_chart.txt");
    }
}
