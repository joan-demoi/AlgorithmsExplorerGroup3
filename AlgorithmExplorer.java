import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;

//--MAIN WINDOW AND HELPER METHODS BLOCK--

public class AlgorithmExplorer extends JFrame {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            AlgorithmExplorer window = new AlgorithmExplorer();
            window.setVisible(true);
        });
    }

    public AlgorithmExplorer() {
        super("Algorithm Explorer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1150, 780);
        setLocationRelativeTo(null); // open in the middle of the screen

        // ONE TAB FOR EACH ALGORITHM
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("A: Recursion Lab", new RecursionTab());
        tabs.addTab("B: Data Structures Lab", new DataStructuresTab());
        tabs.addTab("C: Sorting Lab", new SortingTab());
        tabs.addTab("D: Complexity Analysis", new AnalysisTab());
        add(tabs);
    }

    // Helper methods used by all the tabs

    // makes an active button on click
    static JButton makeButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.addActionListener(action);
        return button;
    }

    // makes a text box with a fixed width used to show results
    static JTextArea makeOutputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        return area;
    }

    static int readInt(JTextField field, String label, int min, int max) {
        String text = field.getText().trim();

        if (text.isEmpty()) {
            throw new IllegalArgumentException(label + " is empty.");
        }

        int value;
        try {
            value = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a whole number (you typed '" + text + "').");
        }

        if (value < min || value > max) {
            throw new IllegalArgumentException(label + " must be between " + min + " and " + max + ".");
        }

        return value;
    }

    // turns nanoseconds into a readable string in milliseconds
    static String formatTime(long nanos) {
        return String.format("%.4f ms", nanos / 1e6);
    }

    // Runs a task on a thread with a very large stack, then waits for it to finish.
    // It is needed because recursive sorts can go very deep.
    static void runWithBigStack(Runnable task) {
        long stackSize = 512L * 1024 * 1024; // this is a 512MB stack
        Thread worker = new Thread(null, task, "big-stack", stackSize);
        worker.start();

        try {
            worker.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    //--RECURSION TAB AND FIBONACCI PANEL--

    // RECURSION LAB
    static class RecursionTab extends JTabbedPane {
        RecursionTab() {
            addTab("Fibonacci", new FibPanel());
            addTab("Factorial", new FactPanel());
            addTab("Tower of Hanoi", new HanoiPanel());
        }
    }

    // FIBONACCI - runs the 3 methods side by side and compares them
    static class FibPanel extends JPanel {

        JTextField nField = new JTextField("10", 6);
        JTextArea output = makeOutputArea();

        FibPanel() {
            super(new BorderLayout());

            // top panel with label, input box and the button
            JPanel topBar = new JPanel();
            topBar.add(new JLabel("n (0 - 90):"));
            topBar.add(nField);
            topBar.add(makeButton("Compute all three", e -> computeAll()));

            add(topBar, BorderLayout.NORTH);
            add(new JScrollPane(output), BorderLayout.CENTER);
        }

        // this runs when the button is clicked
        void computeAll() {
            try {
                int n = readInt(nField, "n", 0, 90);

                String[] methodNames = {"Naive recursion", "Memoised recursion", "Bottom-up DP"};
                StringBuilder text = new StringBuilder();

                // table heading
                text.append(String.format("%-22s %-22s %-14s %s%n",
                        "Method", "fib(" + n + ")", "Calls/iters", "Time"
                ));

                // one row for each method
                for (int method = 0; method < 3; method++) {

                    // naive recursion is far too slow for n = 30 so we skip it
                    if (method == 0 && n > 30) {
                        text.append(String.format("%-22s skipped because n > 30 (see explanation below)%n",
                                methodNames[method]));
                        continue;
                    }

                    Backend.Run run = Backend.fibonacci(method, n);
                    text.append(String.format("%-22s %-22s %-14d %s%n",
                            methodNames[method], run.result, run.count, formatTime(run.nanos)
                    ));
                }

                // Explanation shown under the table
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
    // factorial compares the recursive and the iterative version
    static class FactPanel extends JPanel {

        JTextField nField = new JTextField("5", 6);
        JTextArea output = makeOutputArea();

        FactPanel() {
            super(new BorderLayout());

            // top bar with label, input box and the button
            JPanel topBar = new JPanel();
            topBar.add(new JLabel("n (0 - 20):"));
            topBar.add(nField);
            topBar.add(makeButton("Compute n!", e -> compute()));

            add(topBar, BorderLayout.NORTH);
            add(new JScrollPane(output), BorderLayout.CENTER);
        }

        // runs when the button is clicked
        void compute() {
            try {
                // We allow any whole number here and check the range ourselves,
                // so that the user gets a clear message for negative numbers and for numbers
                // that are too big.

                int n = readInt(nField, "n", Integer.MIN_VALUE, Integer.MAX_VALUE);

                if (n < 0) {
                    throw new IllegalArgumentException("Factorial is not defined for negative numbers.");
                }
                if (n > 20) {
                    throw new IllegalArgumentException("n must be at most 20 (21! no longer fits in a 64-bit long).");
                }

                // Run both versions in the back end
                Backend.Run recursive = Backend.factorial(true, n);
                Backend.Run iterative = Backend.factorial(false, n);

                StringBuilder text = new StringBuilder();

                // table heading and one row for each version
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // smooth edges

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

    // DATA STRUCTURES
    static class DataStructuresTab extends JTabbedPane {
        DataStructuresTab() {
            addTab("Stack", new StackPanel());
            addTab("Linear Queue", new QueuePanel());
            addTab("Circular Queue", new QueuePanel());
            addTab("Stringly Linked List", new ListPanel());
            addTab("Doubly Linked List", new ListPanel());
            addTab("Bracket Checker", new BracketPanel());
        }
    }

    static abstract class StructPanel extends JPanel {

        JLabel messageLabel = new JLabel(" Ready.");
        JPanel controls;
        JComponent centerView;

        // view - the drawing for this structure
        // columns - how many columns the controls row has
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

        abstract void refresh(); // each panel redraws its own structure here

        void say(String text) {
            messageLabel.setText("" + text); // shows a message on the bottom line
        }

        void addOperation(String label, Runnable action) {
            controls.add(makeButton(label, e -> {
                try {
                    action.run();
                } catch (RuntimeException ex) {
                    messageLabel.setText("\u26a0 " + ex.getMessage());
                }
                refresh();
            }));
        }
    }
    //your code block is ArrayView - the row of boxes that the stack and queues are drawn
    //with.

    // Draws an array as a row of boxes: one box for each slot.
    // Filled slots are yellow and show their value; empty slots are white.
    // It only DRAWS what it is given; the stack/queue panels decide what to show.
    static class ArrayView extends JPanel {

        int[] values;          // the number in each slot
        boolean[] filled;      // true if the slot currently holds a value
        String[] tags;         // small red label above a slot (can be null or empty)
        String caption = "";   // a line of text drawn under the boxes

        // Called by the panels whenever the structure changes
        void setState(int[] values, boolean[] filled, String[] tags, String caption) {
            this.values = values;
            this.filled = filled;
            this.tags = tags;
            this.caption = caption;
            repaint();
        }

        // Swing calls this automatically whenever the view needs to be redrawn
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Nothing to draw until a structure has been created
            if (values == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   // smooth edges

            int slots = values.length;
            int cellWidth = Math.min(80, (getWidth() - 40) / slots);    // boxes shrink if there are many slots
            int cellHeight = 50;
            int startX = (getWidth() - cellWidth * slots) / 2;          // centres the whole row
            int y = getHeight() / 2 - 25;

            for (int i = 0; i < slots; i++) {
                int x = startX + i * cellWidth;

                // The box: yellow if it holds a value, white if empty
                if (filled[i]) {
                    g2.setColor(new Color(255, 224, 130));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRect(x, y, cellWidth, cellHeight);

                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, y, cellWidth, cellHeight);

                // The value inside the box (only for filled slots)
                if (filled[i]) {
                    g2.drawString(String.valueOf(values[i]), x + cellWidth / 2 - 8, y + 30);
                }

                // The index under the box, e.g. [0], [1], [2]
                g2.setColor(Color.GRAY);
                g2.drawString("[" + i + "]", x + cellWidth / 2 - 10, y + 68);

                // The red tag above the box, if this slot has one
                if (tags[i] != null && !tags[i].isEmpty()) {
                    g2.setColor(new Color(180, 30, 30));
                    g2.drawString(tags[i], x + 3, y - 8);
                }
            }

            // The caption under the whole row
            g2.setColor(Color.BLACK);
            g2.drawString(caption, startX, y + 105);
        }
    }

    //THE STACK PANEL
    static class StackPanel extends StructPanel {

        Backend.ArrayStack stack;                              
        JTextField capacityField = new JTextField("6", 4);
        JTextField valueField = new JTextField("", 4);
        ArrayView view;                                         

        StackPanel() {
            super(new ArrayView(), 7);
            view = (ArrayView) centerView;    

            controls.add(new JLabel(" Capacity (1-12):"));
            controls.add(capacityField);
            addOperation("Create stack", () -> createStack());

            controls.add(new JLabel(" Value:"));
            controls.add(valueField);
            addOperation("Push", () -> push());
            addOperation("Pop", () -> say("Popped " + stack.pop()));
            addOperation("Peek", () -> say("Top element is " + stack.peek() + " (not removed)"));
            addOperation("Size", () -> say("Size = " + stack.size()));
            addOperation("is_empty", () -> say("is_empty = " + stack.isEmpty()));
            addOperation("is_full", () -> say("is_full = " + stack.isFull()));
            addOperation("Display", () -> say("Stack (bottom to top): " + stack.show(false)));

            stack = new Backend.ArrayStack(6);
            refresh();
        }

        void createStack() {
            int capacity = readInt(capacityField, "Capacity", 1, 12);
            stack = new Backend.ArrayStack(capacity);
            say("Created an empty stack with capacity " + stack.data.length);
        }

        void push() {
            int value = readInt(valueField, "Value", -999999, 999999);
            stack.push(value);     
            say("Pushed " + value);
        }

        void refresh() {
            int slots = stack.data.length;
            boolean[] filled = new boolean[slots];
            String[] tags = new String[slots];

            for (int i = 0; i < slots; i++) {
                filled[i] = (i <= stack.top);

                if (i == stack.top) {
                    tags[i] = "top";
                } else {
                    tags[i] = "";
                }
            }

            String caption = "size = " + stack.size() + " / " + slots + "   top index = " + stack.top;
            view.setState(stack.data, filled, tags, caption);
        }
    }
    //Precious, place your code here, it is about linked lists



    

    //Joan, Place your code here






    
    //The SortingTab
    //It is a very big class so we split it into two blocks
    //FIRST HALF - its fields and the constructor which lays out the screen

    static class SortingTab extends JPanel {

        //inputs at the top
      JTextField inputField = new JTextField("64, 34, 25, 12, 22, 11, 90, 5", 32);
      JTextField randomCountField = new JTextField("10", 5);
      JComboBox<String> algorithmBox = new JComboBox<>(Backend.NAMES); 

      //whats seen on screen
        SortView view = new SortView();
        JLabel noteLabel = new JLabel("");     //describes the current step
        JLabel metricsLabel = new JLabel("");    //comparisons, swaps and time

        
         // The recorded steps of the current sort. Each step is called a "frame".
        int[][] frames;               // frames[i] = how the array looks at step i
        int[] highlightStarts;        // the green range at step i (from...)
        int[] highlightEnds;          // (...to)
        String[] notes;               // a sentence describing step i
        int frameCount;               // how many frames there are
        int currentFrame;             // which frame is on screen now

        String preparedKey = "";      // remembers which input and algorithm the frames were made for
        Timer autoTimer;              // moves to the next frame while auto-play is running

        // The table for "Compare all"
        DefaultTableModel compareModel = new DefaultTableModel(
                new String[]{"Algorithm", "Comparisons", "Swaps / moves", "Time (ms)", "Sorted correctly?"}, 0);

        SortingTab() {
            super(new BorderLayout());

            // Auto-play: every 600 ms show the next frame, and stop at the last one
            autoTimer = new Timer(600, e -> {
                if (currentFrame < frameCount - 1) {
                    currentFrame++;
                    showFrame();
                } else {
                    autoTimer.stop();
                }
            });

            // ---- First row: the numbers, random generator and algorithm choice ----
            JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row1.add(new JLabel("Numbers:"));
            row1.add(inputField);
            row1.add(new JLabel("Random n:"));
            row1.add(randomCountField);
            row1.add(makeButton("Generate random", e -> guard(() -> generateRandom())));
            row1.add(new JLabel("Algorithm:"));
            row1.add(algorithmBox);

            // ---- Second row: the control buttons ----
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
            row2.add(makeButton("Run (show result)", e -> guard(() -> runAndShowResult())));
            row2.add(makeButton("Step >", e -> guard(() -> stepForward())));
            row2.add(makeButton("Auto-play / Stop", e -> guard(() -> toggleAutoPlay())));
            row2.add(makeButton("Reset", e -> resetPlayback()));
            row2.add(makeButton("Compare all", e -> guard(() -> compareAll())));

            // Put the two rows on top of each other at the top of the tab
            JPanel topArea = new JPanel(new GridLayout(2, 1));
            topArea.add(row1);
            topArea.add(row2);
            add(topArea, BorderLayout.NORTH);

            // ---- Middle: the bars, with the step note and the metrics under them ----
            noteLabel.setFont(noteLabel.getFont().deriveFont(Font.BOLD, 13f));

            JPanel statusArea = new JPanel(new GridLayout(2, 1));
            statusArea.add(noteLabel);
            statusArea.add(metricsLabel);

            JPanel middleArea = new JPanel(new BorderLayout());
            middleArea.add(view, BorderLayout.CENTER);
            middleArea.add(statusArea, BorderLayout.SOUTH);

            // ---- Bottom: the compare table on the left, the complexity table on the right ----
            JTable compareTable = new JTable(compareModel);
            JTable infoTable = new JTable(Backend.INFO,
                    new String[]{"Algorithm", "Best", "Average", "Worst", "Extra space", "Stable?"});

            JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                    new JScrollPane(compareTable), new JScrollPane(infoTable));
            bottomSplit.setResizeWeight(0.45);
            bottomSplit.setPreferredSize(new Dimension(100, 190));

            // The middle area on top, the two tables underneath
            JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, middleArea, bottomSplit);
            mainSplit.setResizeWeight(0.75);
            add(mainSplit, BorderLayout.CENTER);
        }

        //the animation part - preparing the sort, stepping tyhrough it, auto play and "compare all"
        
    }
}
