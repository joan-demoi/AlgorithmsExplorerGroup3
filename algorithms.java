import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

//--MAIN WINDOW AND HELPER METHODS BLOCK--

public class AlgorithmExplorer extends JFrame{

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            AlgorithmExplorer window = new AlgorithmExplorer();
            window.setVisible(true);
        });
    }

    public AlgorithmExplorer(){
        super("Algorithm Explorer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null); //open in the middle of the screen

        //ONE TAB FOR EACH ALGORITHM
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("A: Recursion Lab", new RecursionTab());
        tabs.addTab("B: Data Structures Lab", new DataStructuresTab());
        tabs.addTab("C: Sorting Lab", new SortingTab());
        tabs.addTab("D: Complexity Analysis", new AnalysisTab());
        add(tabs);
}

        //Helper methods used ba all the tabs

        //makes an active button on click
        static JButton makeButton(String text, ActionListener action){
            JButton button = new JButton(text);
            button.addActionListener(action);
            return button;
        }

        //makes a text box with a fixed widthused to show results
        static JTextArea makeOutputArea(){
            JTextArea area = new JTextArea();
            area.setEditable(false);
            area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
            return area;
        }

        static int readInt(JTextField field, String label, int min, int max){
            String text = field.getText().trim();

            if (text.isEmpty()) {
                throw new IllegalArgumentException(label + "is empty.");
            }

            int value;
            try{
                value = Integer.parseInt(text);
            } catch (NumberFormatException e){
                throw new IllegalArgumentException(label + "must be a whole number (you typed '" + text + "').");
            }

            if (value < min || value > max) {
                throw new IllegalArgumentException(label + "must be between " + min + " and " + max + ".")

            }

            return value;
        }

        //turns nanso seconds into a readable string in milliseconds
        static String formatTime(long nanos) {
            return String.format("%.4f ms", nanos / 1e6);
        }

        //Runs a task on a thread with a very larger stack, then waits for it to finish
        //it is needed because recursive sorts can go very deep

        static void runWithBigStack(Runnable task) {
            long stackSize = 512L * 1024 * 1024; //this is a 512MB
            Thread worker = new Thread(null, task, "big-stack", stackSize);
            worker.start();

            try{
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        //--RECURSION TAB AND FIBONACCI PANEL--

        //RECURSION LAB

        static class RecursionTab extends JTabbedPane {
            RecursionTab() {
                addTab("Fibonacci", new FibPanel());
                addTab("Factorial", new FactPanel());
                addTab("Tower of Hanoi", new HanoiPanel());
            }
        }

        //FIBONACCI - runs the 3 methods side by side and compares them
        static class Fibonacci extends JPanel {

            JTextField nField = new JTextField("10", 6);
            JTextArea output = makeOutputArea();

            FibPanel() {
                super(new BorderLayout());

                //tope panel with label, input box and the button
                JPanel topBar = new JPanel();
                topBar.add(new JLabel("n (0 - 90):"));
                topBar.add(makeButton("Compute all three", e -> computeAll()));

                add(topBar, BorderLayout.NORTH);
                add(new JScrollPane(output), BorderLayout.CENTER);
            }

            //this runs when the button is clicked
            void coputeAll() {
                try {
                    int n = readInt(nField, "n", 0, 90);

                    String[] methodNames = {"Naive recursion", "Memoised recursion", "Bottom-up DP"};
                    StringBuilder text = new StringBuilder();

                    //table heading
                    text.append(String.format("%-22s %-22s %-14s %s%n",
                            "Method", "fib(" + n + ")", "Calls/iters", "Time"
                    ));

                    //one row for each method
                    for (int method = 0; method < 3; method++) {

                        //naive recusrion is far too slow for n = 30 so we skip it
                        if (method == 0 && n > 30) {
                            text.append(String.format("%-22s skipped because n > 30 (see explanation below)%n",
                                methodNames[method]));
                            continue;
                        }

                        Backend.Run run = Backend.fibonacci(method, n);
                        text.append(String.format("%-22s %-22s %-14d %s%n",
                            methodName[method], run.result, run.count, formatTime(run.nanos)
                        ));
                    }

                    //Explanation shown under the table
                    text.append("\nWhy naive recursion is limited to n <= 30:\n");
                    text.append("fib(n) calls fib(n-1) AND fib(n-2), so the same sub-problems are solved again\n");
                    text.append("and again. The number of calls roughly doubles every time n grows by 1 (O(2^n)).\n");
                    text.append("fib(30) already needs 2,692,537 calls; fib(50) would need about 40 billion\n");
                    text.append("and freeze the program.\n");
                    text.append("Memoised recursion remembers each answer (O(n) calls).\n");
                    text.append("Bottom-up DP uses a simple loop (O(n), no recursion).\n");

                    output.setText(text.toString());
                    
                } catch (IllegalArgumentException ex) {
                    output.setText("Input problem: " + ex.getMessage());
                }
            }
        }

        //--FACTORIAL PANEL--
        //factorial compares the recursive and the interative version
        static class FactPanel extends JPanel {

            JTextField nField = new JTextField("5", 6);
            JTextArea output = makeOutputArea();

            FactPanel() {
                super(new BorderLayout());

                //top bar with label, input box and the button
                JPanel topzBar = new JPanel();
                topBar.add(new JLabel("n (0 - 20):"));
                topBar.add(nField);
                topBar.add(makeButton("Compute n!", e -> compute()));

                add(topBar, BorderLayout.NORTH);
                add(new JScrollPane(output), BorderLayout.CENTER);
            }

            //runs when the button is clicked
            void compute() {
                try {
                    // We allow any whole number here and check the range ourselves,
                    //so that the user gets a clear message for negative numbers and for numbers 
                    //that are too big.

                    int n = readInt(nField, "n", Integer.MIN_VALUE, Integer.MAX_VALUE);

                    if (n < 0) {
                        throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
                    }
                    if (n > 20) {
                        throw new IllegalArgumentException("n must be at most 20 (21! no longer fits in a 64-bit long).");

                    }

                    //Run oth versions in the back end
                    Backend.Run recursive = Backend.factorial(true, n);
                    Backend.Run iterative = Backend.factorial(false, n);

                    StringBuilder text = new StringBuilder();

                    //table heading and one row for each version
                    text.append(String.format("%-12s %-22s %-14s %s%n",
                        "Method", n + "!", "Calls/iters", "Time"));
                    text.append(String.format("%-12s %-22s %-14d %s%n",
                            "Recursive", recursive.result, recursive.count, formatTime(recursive.nanos)));
                    text.append(String.format("%-12s %-22s %-14d %s%n",
                            "Iterative", iterative.result, iterative.count, formatTime(iterative.nanos)));

                    output.setText(text.toString());

                } catch (IllegalArgumentException ex) {
                    output.setText("Input problem: " + ex.getMessage());
            }
        }
        //Precious you'll start your code block from here, the one of Tower of Hanoi
        

    
}
