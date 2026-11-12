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

// Tower of Hanoi: the back end works out all the moves and this panel replays them one by one so you can watch the disks move.
static class HanoiPanel extends JPanel {

// Controls at the top
JSpinner diskSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 8, 1));
JComboBox<String> sourceBox = new JComboBox<>(new String[]{"A", "B", "C"});
JComboBox<String> destBox = new JComboBox<>(new String[]{"A", "B", "C"});
JComboBox<String> auxBox = new JComboBox<>(new String[]{"A", "B", "C"});
JSlider speedSlider = new JSlider(1, 10, 5);

// Things we show on screen
HanoiBoard board = new HanoiBoard(); // written in Block 5
JTextArea movesList = makeOutputArea();
JLabel counterLabel = new JLabel(" Press Reset, then Start.");

// The solution and where we are in it
Backend.Hanoi solution; // all the moves, worked out by the back end
int currentMove; // how many moves have been shown so far
int diskCount; // number of disks in the current game

// The three pegs. pegs[p][k] is the disk at position k on peg p (0 = bottom).
// pegHeights[p] is how many disks are on peg p right now.
int[][] pegs = new int[3][8];
int[] pegHeights = new int[3];

Timer timer; // calls stepOnce() again and again while the animation runs

HanoiPanel() {
super(new BorderLayout());

// Start with A -> C using B as the helper peg
destBox.setSelectedIndex(2);
auxBox.setSelectedIndex(1);

timer = new Timer(300, e -> stepOnce());
speedSlider.addChangeListener(e -> updateSpeed());

// Top bar with all the controls
JPanel topBar = new JPanel();
topBar.add(new JLabel("Disks (1-8):"));
topBar.add(diskSpinner);
topBar.add(new JLabel("Source:"));
topBar.add(sourceBox);
topBar.add(new JLabel("Destination:"));
topBar.add(destBox);
topBar.add(new JLabel("Auxiliary:"));
topBar.add(auxBox);
topBar.add(makeButton("Start", e -> start()));
topBar.add(makeButton("Pause / Step", e -> pauseOrStep()));
topBar.add(makeButton("Reset", e -> reset()));
topBar.add(new JLabel("Speed:"));
topBar.add(speedSlider);

// List of moves on the right side
JScrollPane movesScroll = new JScrollPane(movesList);
movesScroll.setPreferredSize(new Dimension(270, 100));

// Move counter at the bottom
counterLabel.setFont(counterLabel.getFont().deriveFont(Font.BOLD, 14f));

add(topBar, BorderLayout.NORTH);
add(board, BorderLayout.CENTER);
add(movesScroll, BorderLayout.EAST);
add(counterLabel, BorderLayout.SOUTH);

reset();
}

// Higher slider value = shorter delay between moves = faster animation
void updateSpeed() {
timer.setDelay(1500 / speedSlider.getValue());
}

// Sets up a fresh game with all the disks on the source peg
void reset() {
timer.stop();

int source = sourceBox.getSelectedIndex();
int dest = destBox.getSelectedIndex();
int aux = auxBox.getSelectedIndex();

// The three pegs must all be different
if (source == dest || source == aux || dest == aux) {
counterLabel.setText(" Source, destination and auxiliary must be three DIFFERENT pegs.");
solution = null;
return;
}

diskCount = (Integer) diskSpinner.getValue();
solution = new Backend.Hanoi(diskCount, source, dest, aux); // the recursion happens in the back end
currentMove = 0;

// Empty all three pegs
for (int p = 0; p < 3; p++) {
pegHeights[p] = 0;
}

// Put every disk on the source peg, biggest at the bottom
for (int k = 0; k < diskCount; k++) {
pegs[source][k] = diskCount - k;
}
pegHeights[source] = diskCount;

board.setState(pegs, pegHeights, source, dest, aux);
showMoveList();
updateCounter();
}

// Writes every move into the text box on the right
void showMoveList() {
StringBuilder text = new StringBuilder();

for (int i = 0; i < solution.count; i++) {
char fromPeg = (char) ('A' + solution.from[i]);
char toPeg = (char) ('A' + solution.to[i]);
text.append((i + 1) + ". Move disk " + solution.disk[i]
+ " from " + fromPeg + " to " + toPeg + "\n");
}

movesList.setText(text.toString());
movesList.setCaretPosition(0); // scroll back to the top
}

// "Start" button: runs the animation automatically
void start() {
// If there is no game yet, or the last one has finished, begin a new one
if (solution == null || currentMove >= solution.count) {
reset();
}
if (solution == null) {
return; // the peg choice was invalid
}

updateSpeed();
timer.start();
}

// "Pause / Step" button: pauses if running, otherwise does one single move
void pauseOrStep() {
if (timer.isRunning()) {
timer.stop();
} else {
stepOnce();
}
}

// Does ONE move: takes the top disk off one peg and puts it on another
void stepOnce() {
if (solution == null) {
reset();
}
if (solution == null) {
return;
}

// Nothing left to do
if (currentMove >= solution.count) {
timer.stop();
return;
}

int fromPeg = solution.from[currentMove];
int toPeg = solution.to[currentMove];

// Lift the top disk off the "from" peg...
pegHeights[fromPeg]--;
int disk = pegs[fromPeg][pegHeights[fromPeg]];

// ...and drop it on the "to" peg
pegs[toPeg][pegHeights[toPeg]] = disk;
pegHeights[toPeg]++;

currentMove++;
if (currentMove == solution.count) {
timer.stop();
}

updateCounter();
board.repaint();
}

// Updates the text at the bottom, e.g. " Moves: 3 / 7 (2^3 - 1 = 7)"
void updateCounter() {
int total = (1 << diskCount) - 1; // 2^n - 1

String text = " Moves: " + currentMove + " / " + total
+ " (2^" + diskCount + " - 1 = " + total + ")";

if (currentMove == solution.count) {
if (currentMove == total) {
text += " DONE: total moves = 2^n - 1 \u2713";
} else {
text += " (mismatch!)";
}
}

counterLabel.setText(text);
}
}

// ===== BLOCK 5: Module A - Tower of Hanoi drawing board =====

    // Draws the three pegs and the disks. It only DRAWS what HanoiPanel tells it;
    // it never moves any disks itself.
    static class HanoiBoard extends JPanel {

        int[][] pegs;                       // same arrays that HanoiPanel uses
        int[] pegHeights;
        String[] roles = new String[3];     // "source", "destination" or "auxiliary" for each peg

        // Called by HanoiPanel.reset() to give the board the new game
        void setState(int[][] pegs, int[] pegHeights, int source, int dest, int aux) {
            this.pegs = pegs;
            this.pegHeights = pegHeights;

            roles[source] = "source";
            roles[dest] = "destination";
            roles[aux] = "auxiliary";

            repaint();
        }

        // Swing calls this automatically whenever the board needs to be redrawn
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Nothing to draw until the first game has been set up
            if (pegs == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   // smooth edges

            int width = getWidth();
            int baseY = getHeight() - 50;     // the y position of the ground
            int pegWidth = width / 3;         // each peg gets one third of the width

            for (int p = 0; p < 3; p++) {
                int centerX = pegWidth * p + pegWidth / 2;

                // The pole, the base plate and the label under it
                g2.setColor(Color.DARK_GRAY);
                g2.fillRect(centerX - 4, baseY - 230, 8, 230);
                g2.fillRect(centerX - pegWidth / 2 + 10, baseY, pegWidth - 20, 6);

                String label = (char) ('A' + p) + " (" + roles[p] + ")";
                g2.drawString(label, centerX - 40, baseY + 28);

                // The disks on this peg, from the bottom one upwards
                for (int k = 0; k < pegHeights[p]; k++) {
                    int disk = pegs[p][k];

                    // Bigger disk number = wider disk
                    int diskWidth = 30 + disk * (pegWidth - 60) / 9;
                    int y = baseY - (k + 1) * 24;     // every disk is 24 pixels tall

                    // Each disk gets its own colour, filled first and then outlined
                    g2.setColor(Color.getHSBColor(disk / 9f, 0.55f, 0.95f));
                    g2.fillRoundRect(centerX - diskWidth / 2, y, diskWidth, 22, 10, 10);

                    g2.setColor(Color.BLACK);
                    g2.drawRoundRect(centerX - diskWidth / 2, y, diskWidth, 22, 10, 10);

                    // The disk number written in the middle
                    g2.drawString(String.valueOf(disk), centerX - 3, y + 16);
                }
            }
        }
    }

    // DATA STRUCTURES TAB AND THE SHARED BASE PANEL

    //DATE STRUCTURES
    static class DataStructuresTab extends JTabbedPane {
        DataStructuresTab(){
            addTab("Stack", new StackPanel());
            addTab("Linear Queue", new QueuePanel());
            addTab("Circular Queue", new QueuePanel());
            addTab("Stringly Linked List", new ListPanel());
            addTab("Doubly Linked List", new ListPanel());
            addTab("Bracket Checker", new BracketPanel());
        }
    }
    static abstract class StructPanel extends JPanel {

        JLabel messageLabel = new JLable(" Ready.");
        JPanel controls;
        JComponent centerView;

        //view - the drawing for this structure
        //columns - how many columns the controls row has

        StructPanel(JComponent view, int columns) {
            super(new BorderLayout());
            centerView = view;

            controls = new JPanel(new GridLayout(0, columns, 4, 4));
            
            messageLabel.setFont(messageLabel.getFont().deriveFont(Font.BOLD, 14f));
            messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

            add(controls, BorderLayout.NORTH);
            add(view, BorderLayout.CENTER);
            add(messageLabel, BorderLayout.SOUTH);
        }

        abstract void refresh(); //each panel redraws its own structure here

        void say(String text) {
            messageLabel.setText("" + text); //shows a message on the bottom line
        }

        void addOperation(String label, Runnable action) {
            controls.add(makeButton(label, e -> {
                try {
                    action.run();
                } catch (RunTImeException ex) {
                    messageLabel.setText("\ u26a0 " + ex.getMessage());
                }
                refresh();
            }));
        
        }          
    }
}
