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

    //THE QUEUE PANEL
     static class QueuePanel extends StructPanel {

        final boolean circular;
        Backend.ArrayQueue queue;                                // the real queue, from the back end
        JTextField capacityField = new JTextField("5", 4);
        JTextField valueField = new JTextField("", 4);
        ArrayView view;                                          // the drawing in the middle

        QueuePanel(boolean circular) {
            super(new ArrayView(), 7);
            this.circular = circular;
            view = (ArrayView) centerView;

            // Controls row: capacity box and "Create queue" button
            controls.add(new JLabel(" Capacity (1-12):"));
            controls.add(capacityField);
            addOperation("Create queue", () -> createQueue());

            // Value box and the buttons that work on the queue
            controls.add(new JLabel(" Value:"));
            controls.add(valueField);
            addOperation("Enqueue", () -> enqueue());
            addOperation("Dequeue", () -> say("Dequeued " + queue.dequeue()));
            addOperation("Front", () -> say("Front element is " + queue.front() + " (not removed)"));
            addOperation("Size", () -> say("Size = " + queue.size()));
            addOperation("is_empty", () -> say("is_empty = " + queue.isEmpty()));
            addOperation("is_full", () -> say("is_full = " + queue.isFull()));
            addOperation("Display", () -> display());

            // Start with an empty queue of size 5 so the screen is not blank
            queue = makeQueue(5);
            refresh();

            // A hint that tells the user what to try
            if (circular) {
                say("Fill the queue, dequeue a few, then enqueue again: rear wraps around to index 0.");
            } else {
                say("Try: fill the queue, dequeue a few items, then enqueue again to see the FALSE FULL problem.");
            }
        }

        // Makes the right kind of queue depending on which tab this is
        Backend.ArrayQueue makeQueue(int capacity) {
            if (circular) {
                return new Backend.CircularQueue(capacity);
            } else {
                return new Backend.LinearQueue(capacity);
            }
        }

        // "Create queue" button
        void createQueue() {
            int capacity = readInt(capacityField, "Capacity", 1, 12);
            queue = makeQueue(capacity);

            if (circular) {
                say("Created an empty circular queue");
            } else {
                say("Created an empty linear queue");
            }
        }

        // "Enqueue" button: reads the value box and adds that number at the rear
        void enqueue() {
            int value = readInt(valueField, "Value", -999999, 999999);
            queue.enqueue(value);    // throws an error if the queue is full
            say("Enqueued " + value);
        }

        // "Display" button: prints the items from the front to the rear
        void display() {
            StringBuilder text = new StringBuilder("Queue (front to rear): ");

            if (circular) {
                // Start at the front and walk forward, wrapping around the end of the array
                for (int k = 0; k < queue.size(); k++) {
                    int index = (queue.front + k) % queue.arr.length;
                    text.append(queue.arr[index]).append(' ');
                }
            } else {
                // In a linear queue the items are simply from front to rear
                for (int i = queue.front; i <= queue.rear; i++) {
                    text.append(queue.arr[i]).append(' ');
                }
            }

            say(text.toString());
        }

        // Redraws the queue after every button click
        void refresh() {
            int slots = queue.arr.length;
            boolean[] filled = queue.occupied();
            String[] tags = new String[slots];

            for (int i = 0; i < slots; i++) {
                tags[i] = "";
            }

            // Mark the front and the rear (only when the queue has something in it)
            if (!queue.isEmpty()) {
                tags[queue.front] = "front";

                if (queue.front == queue.rear) {
                    tags[queue.rear] = "front/rear";   // only one item, so it is both
                } else {
                    tags[queue.rear] = "rear";
                }
            }

            String caption = "front = " + queue.front + ", rear = " + queue.rear
                    + ", size = " + queue.size() + " / " + slots;
            view.setState(queue.arr, filled, tags, caption);
        }
    }

    //The ListView - drawing class for linked lists

    static class ListView extends JPanel {

        int[] values = new int[0];     // the values in the list, from head to the end
        boolean doubly;                // true = doubly linked (two arrows and a TAIL label)

        // Called by ListPanel whenever the list changes
        void setState(int[] values, boolean doubly) {
            this.values = values;
            this.doubly = doubly;
            repaint();
        }

        // Draws a line from (x1, y1) to (x2, y2) with a small arrowhead at the (x2, y2) end
        void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
            g2.drawLine(x1, y1, x2, y2);

            // The arrowhead is a small triangle. Its back edge is on the left of the tip
            // when the arrow points right, and on the right of the tip when it points left.
            int direction;
            if (x2 > x1) {
                direction = -1;
            } else {
                direction = 1;
            }

            int[] xs = {x2, x2 + direction * 8, x2 + direction * 8};
            int[] ys = {y2, y2 - 4, y2 + 4};
            g2.fillPolygon(xs, ys, 3);
        }

        // Swing calls this automatically whenever the view needs to be redrawn
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   // smooth edges
            g2.setColor(Color.BLACK);

            // An empty list has nothing to draw except this message
            if (values.length == 0) {
                g2.drawString("HEAD -> null   (the list is empty)", 20, 40);
                return;
            }

            int boxWidth = 56;
            int boxHeight = 36;
            int gap = 50;                                                       // space between boxes, where the arrows go
            int perRow = Math.max(1, (getWidth() - 100) / (boxWidth + gap));    // how many nodes fit on one row

            for (int i = 0; i < values.length; i++) {
                int column = i % perRow;
                int row = i / perRow;

                int x = 20 + column * (boxWidth + gap);
                int y = 50 + row * 90;
                int midY = y + boxHeight / 2;

                // The node: light blue box with a dark outline and its value inside
                g2.setColor(new Color(200, 225, 255));
                g2.fillRect(x, y, boxWidth, boxHeight);

                g2.setColor(Color.DARK_GRAY);
                g2.drawRect(x, y, boxWidth, boxHeight);
                g2.drawString(String.valueOf(values[i]), x + boxWidth / 2 - 8, midY + 5);

                // Red labels: HEAD on the first node, TAIL on the last node (doubly linked only)
                g2.setColor(new Color(180, 30, 30));
                if (i == 0) {
                    g2.drawString("HEAD", x + 10, y - 8);
                }
                if (doubly && i == values.length - 1) {
                    g2.drawString("TAIL", x + 10, y - 8);
                }

                // What comes after this node?
                g2.setColor(Color.BLACK);
                boolean isLast = (i == values.length - 1);
                boolean endOfRow = (column == perRow - 1);

                if (isLast) {
                    g2.drawString("-> null", x + boxWidth + 6, midY + 5);
                } else if (endOfRow) {
                    g2.drawString("continues below", x, y + boxHeight + 18);
                } else if (doubly) {
                    // Two arrows: one forward (next) and one backward (prev)
                    drawArrow(g2, x + boxWidth, midY - 6, x + boxWidth + gap, midY - 6);
                    drawArrow(g2, x + boxWidth + gap, midY + 6, x + boxWidth, midY + 6);
                } else {
                    // One arrow forward (next)
                    drawArrow(g2, x + boxWidth, midY, x + boxWidth + gap, midY);
                }
            }
        }
    }
    
    // ===== BLOCK 11: Module B - Linked list panel (singly and doubly) =====

    // Linked list: one panel for BOTH list types.
    // doubly = false gives a singly linked list, doubly = true gives a doubly linked list.
    static class ListPanel extends StructPanel {

        final Backend.LList list;                                // the real list, from the back end
        JTextField valueField = new JTextField("", 4);
        JTextField targetField = new JTextField("", 4);          // used by the "before/after target" buttons
        ListView view;                                           // the drawing in the middle

        ListPanel(boolean doubly) {
            super(new ListView(), 7);
            view = (ListView) centerView;

            // Make the right kind of list depending on which tab this is
            if (doubly) {
                list = new Backend.DLinkedList();
            } else {
                list = new Backend.SLinkedList();
            }

            // Controls row: the two text boxes
            controls.add(new JLabel(" Value:"));
            controls.add(valueField);
            controls.add(new JLabel(" Target element:"));
            controls.add(targetField);

            // Insert buttons
            addOperation("Insert at beginning", () -> insertAtBeginning());
            addOperation("Insert at end", () -> insertAtEnd());
            addOperation("Insert before target", () -> insertBeforeTarget());
            addOperation("Insert after target", () -> insertAfterTarget());

            // Delete buttons
            addOperation("Delete at beginning", () -> say("Deleted " + list.deleteBegin() + " from the beginning"));
            addOperation("Delete at end", () -> say("Deleted " + list.deleteEnd() + " from the end"));
            addOperation("Delete before target", () -> deleteBeforeTarget());
            addOperation("Delete after target", () -> deleteAfterTarget());
            addOperation("Delete value", () -> deleteValue());

            // Search, size and display buttons
            addOperation("Search value", () -> searchValue());
            addOperation("Size", () -> say("Size = " + list.size));
            addOperation("Display forward", () -> say("Forward: " + joinValues(list.toArray(), " -> ") + " -> null"));

            // Only a doubly linked list can be read backwards
            if (doubly) {
                addOperation("Display backward", () -> say("Backward: " + joinValues(list.toArrayBackward(), " -> ") + " -> null"));
            }

            refresh();
        }

        // ---- Reading the two text boxes ----

        int readValue() {
            return readInt(valueField, "Value", -999999, 999999);
        }

        int readTarget() {
            return readInt(targetField, "Target element", -999999, 999999);
        }

        // ---- Insert operations ----

        void insertAtBeginning() {
            int value = readValue();
            list.insertBegin(value);
            say("Inserted " + value + " at the beginning");
        }

        void insertAtEnd() {
            int value = readValue();
            list.insertEnd(value);
            say("Inserted " + value + " at the end");
        }

        void insertBeforeTarget() {
            int value = readValue();
            int target = readTarget();
            list.insertBefore(target, value);
            say("Inserted " + value + " before " + target);
        }

        void insertAfterTarget() {
            int value = readValue();
            int target = readTarget();
            list.insertAfter(target, value);
            say("Inserted " + value + " after " + target);
        }

        // ---- Delete and search operations ----

        void deleteBeforeTarget() {
            int target = readTarget();
            int deleted = list.deleteBefore(target);
            say("Deleted " + deleted + " (the element before " + target + ")");
        }

        void deleteAfterTarget() {
            int target = readTarget();
            int deleted = list.deleteAfter(target);
            say("Deleted " + deleted + " (the element after " + target + ")");
        }

        void deleteValue() {
            int value = readValue();
            list.deleteValue(value);
            say("Deleted " + value);
        }

        void searchValue() {
            int value = readValue();
            int position = list.search(value);    // -1 means "not found"

            if (position < 0) {
                say(value + " was not found");
            } else {
                say(value + " found at position " + position + " (0 = head)");
            }
        }

        // ---- Helpers ----

        // Turns {1, 2, 3} into "1 -> 2 -> 3" (or "(empty)" if there is nothing in it)
        String joinValues(int[] numbers, String separator) {
            if (numbers.length == 0) {
                return "(empty)";
            }

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < numbers.length; i++) {
                if (i > 0) {
                    text.append(separator);
                }
                text.append(numbers[i]);
            }
            return text.toString();
        }

        // Redraws the list after every button click
        void refresh() {
            view.setState(list.toArray(), list.isDoubly());
        }
    }



    

 // ===== BLOCK 12: Module B - Bracket checker panel =====

    // Bracket checker: tells you whether an expression like { ( [ ] ) } is balanced.
    // The stack work is done in Backend.checkBrackets; this panel only shows the result.
    static class BracketPanel extends JPanel {

        JTextField inputField = new JTextField("{ ( [ ] ) }", 30);
        JTextArea output = makeOutputArea();

        BracketPanel() {
            super(new BorderLayout());

            // Top bar: label, input box and the button
            JPanel topBar = new JPanel();
            topBar.add(new JLabel("Expression:"));
            topBar.add(inputField);
            topBar.add(makeButton("Check if balanced", e -> check()));

            add(topBar, BorderLayout.NORTH);
            add(new JScrollPane(output), BorderLayout.CENTER);
        }

        // Runs when the button is clicked
        void check() {
            String expression = inputField.getText();

            if (expression.trim().isEmpty()) {
                output.setText("The expression is empty. Type something like { ( [ ] ) }");
                return;
            }

            // The back end fills "trace" with one line for every symbol it reads
            StringBuilder trace = new StringBuilder();
            boolean balanced = Backend.checkBrackets(expression, trace);

            String verdict;
            if (balanced) {
                verdict = "BALANCED";
            } else {
                verdict = "NOT BALANCED";
            }

            output.setText(trace + "\nRESULT: " + verdict);
        }
    }
    
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

        //the animation part - preparing the sort, stepping through it, auto play and "compare all"

         static class SortResult {
            long comparisons;
            long moves;          // swaps or moves, depending on the algorithm
            long nanos;          // how long it took
            int[] sortedData;    // the array after sorting
        }

        // Sorts a COPY of the data with the chosen algorithm and measures it.
        // Used by both "Run" and "Compare all", so the timing is done the same way each time.
        SortResult timeSort(int algorithm, int[] data) {
            SortResult result = new SortResult();

            runWithBigStack(() -> {
                int[] copy = Backend.copy(data);
                Backend.Sorter sorter = new Backend.Sorter();

                long start = System.nanoTime();
                sorter.sort(algorithm, copy);
                result.nanos = System.nanoTime() - start;

                result.comparisons = sorter.comparisons;
                result.moves = sorter.moves;
                result.sortedData = copy;
            });

            return result;
        }

        // ---------------------------------------------------------------
        // Button actions
        // ---------------------------------------------------------------

        // Runs an action; if the input is bad, shows a pop-up message instead of crashing
        void guard(Runnable action) {
            try {
                action.run();
            } catch (IllegalArgumentException ex) {
                autoTimer.stop();
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input problem", JOptionPane.WARNING_MESSAGE);
            }
        }

        // "Generate random" button: fills the numbers box with random numbers
        void generateRandom() {
            int count = readInt(randomCountField, "n", 1, 20000);
            int[] numbers = Backend.randomArray(count, 99, System.nanoTime());

            StringBuilder text = new StringBuilder();
            for (int i = 0; i < count; i++) {
                if (i > 0) {
                    text.append(", ");
                }
                text.append(numbers[i]);
            }

            inputField.setText(text.toString());
        }

        // "Run (show result)" button: sorts and jumps straight to the last step
        void runAndShowResult() {
            prepare();
            currentFrame = frameCount - 1;
            showFrame();
        }

        // "Step >" button: shows the next step
        void stepForward() {
            // Nothing prepared yet, or the input/algorithm changed: prepare and show step 0
            if (frames == null || !currentKey().equals(preparedKey)) {
                prepare();
                return;
            }

            if (currentFrame < frameCount - 1) {
                currentFrame++;
                showFrame();
            }
        }

        // "Auto-play / Stop" button
        void toggleAutoPlay() {
            // If it is already playing, this click stops it
            if (autoTimer.isRunning()) {
                autoTimer.stop();
                return;
            }

            if (frames == null || !currentKey().equals(preparedKey)) {
                prepare();
            }

            // If we were already at the end, play again from the beginning
            if (currentFrame >= frameCount - 1) {
                currentFrame = 0;
                showFrame();
            }

            autoTimer.start();
        }

        // "Reset" button: stops playing and goes back to the first step
        void resetPlayback() {
            autoTimer.stop();

            if (frames != null) {
                currentFrame = 0;
                showFrame();
            }
        }

        // "Compare all" button: runs every algorithm on the same numbers and fills the table
        void compareAll() {
            int[] data = Backend.parseInts(inputField.getText());
            compareModel.setRowCount(0);    // clear the old rows

            for (int algorithm = 0; algorithm < Backend.NAMES.length; algorithm++) {
                SortResult result = timeSort(algorithm, data);

                String timeText = String.format("%.4f", result.nanos / 1e6);

                String correct;
                if (Backend.isSorted(result.sortedData)) {
                    correct = "yes";
                } else {
                    correct = "NO";
                }

                compareModel.addRow(new Object[]{
                        Backend.NAMES[algorithm], result.comparisons, result.moves, timeText, correct});
            }
        }

        // ---------------------------------------------------------------
        // Preparing and showing the steps
        // ---------------------------------------------------------------

        // Identifies the current input + algorithm, so we know when the saved steps are out of date
        String currentKey() {
            return inputField.getText() + "|" + algorithmBox.getSelectedIndex();
        }

        // Sorts the input twice: once to measure it, and once (small inputs only) to record every step
        void prepare() {
            int[] data = Backend.parseInts(inputField.getText());
            int algorithm = algorithmBox.getSelectedIndex();

            // 1) Measure: count comparisons and moves and time the sort
            SortResult result = timeSort(algorithm, data);

            // 2) Record the steps for the animation
            if (data.length <= 200) {
                Backend.Sorter recorder = new Backend.Sorter();
                recorder.steps = new Backend.Steps();
                runWithBigStack(() -> recorder.sort(algorithm, Backend.copy(data)));

                frames = recorder.steps.snaps;
                highlightStarts = recorder.steps.hi1;
                highlightEnds = recorder.steps.hi2;
                notes = recorder.steps.notes;
                frameCount = recorder.steps.count;
            } else {
                // Too many numbers to animate: just show the finished result as one frame
                frames = new int[][]{result.sortedData};
                highlightStarts = new int[]{0};
                highlightEnds = new int[]{data.length - 1};
                notes = new String[]{"More than 200 numbers: step-by-step display is off, showing the sorted result"};
                frameCount = 1;
            }

            metricsLabel.setText(String.format(" %s | n = %d | comparisons = %d | swaps/moves = %d | time = %.4f ms",
                    Backend.NAMES[algorithm], data.length, result.comparisons, result.moves, result.nanos / 1e6));

            currentFrame = 0;
            preparedKey = currentKey();
            showFrame();
        }

        // Puts the current frame on the screen
        void showFrame() {
            view.setState(frames[currentFrame], highlightStarts[currentFrame], highlightEnds[currentFrame]);
            noteLabel.setText(" Step " + currentFrame + " / " + (frameCount - 1) + ":  " + notes[currentFrame]);
        }
      }

    //The LineChart - drawing class behind the two graphs.

    static class LineChart extends JPanel {

        static final Color[] COLORS = {
                new Color(31, 119, 180),     // blue
                new Color(214, 39, 40),      // red
                new Color(44, 160, 44)       // green
        };

        // What to draw (set by AnalysisTab)
        String title = "";
        double[] xValues;          // the n values along the bottom
        String[] seriesNames;      // the name of each line
        double[][] yValues;        // yValues[line][i] = time in ms for xValues[i]; NaN means "not measured"
        boolean logScale;          // true = both axes use a log scale

        // Space left around the plot for the labels
        int marginLeft = 70;
        int marginRight = 20;
        int marginTop = 30;
        int marginBottom = 40;

        // Worked out every time the chart is painted
        int plotWidth;
        int plotHeight;
        double xMin;
        double xRange;
        double yMin;
        double yRange;

        // Called by AnalysisTab when there is new data
        void setState(String title, double[] xValues, String[] seriesNames, double[][] yValues, boolean logScale) {
            this.title = title;
            this.xValues = xValues;
            this.seriesNames = seriesNames;
            this.yValues = yValues;
            this.logScale = logScale;
            repaint();
        }

        // On a log scale we plot log10 of the value instead of the value itself.
        // (The tiny minimum stops us taking the log of 0.)
        double transform(double value) {
            if (logScale) {
                return Math.log10(Math.max(value, 1e-4));
            }
            return value;
        }

        // Converts a data value into a pixel position on the screen
        int xToPixel(double x) {
            return marginLeft + (int) ((transform(x) - xMin) / xRange * plotWidth);
        }

        int yToPixel(double y) {
            return getHeight() - marginBottom - (int) ((transform(y) - yMin) / yRange * plotHeight);
        }

        // Swing calls this automatically whenever the chart needs to be redrawn
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);   // smooth edges

            g2.setColor(Color.BLACK);
            g2.drawString(title, marginLeft, 18);

            // Nothing to draw until the analysis has been run
            if (xValues == null) {
                g2.drawString("Click \"Run full analysis\" to draw this chart.", marginLeft, getHeight() / 2);
                return;
            }

            // ---- Work out the range of the x axis ----
            if (logScale) {
                xMin = transform(xValues[0]);
            } else {
                xMin = 0;
            }
            double xMax = transform(xValues[xValues.length - 1]);

            // ---- Work out the range of the y axis by looking at every measured value ----
            double yMax = -Double.MAX_VALUE;
            if (logScale) {
                yMin = Double.MAX_VALUE;
            } else {
                yMin = 0;
            }

            boolean hasData = false;
            for (double[] line : yValues) {
                for (double value : line) {
                    if (!Double.isNaN(value)) {
                        hasData = true;
                        yMax = Math.max(yMax, transform(value));
                        if (logScale) {
                            yMin = Math.min(yMin, transform(value));
                        }
                    }
                }
            }

            if (!hasData) {
                g2.drawString("No data for this input type.", marginLeft, getHeight() / 2);
                return;
            }

            // If every value is the same, widen the range a little so we never divide by zero
            if (yMax - yMin < 1e-9) {
                yMax = yMin + 1;
            }

            xRange = xMax - xMin;
            yRange = yMax - yMin;
            plotWidth = getWidth() - marginLeft - marginRight;
            plotHeight = getHeight() - marginTop - marginBottom;

            // ---- Draw everything ----
            g2.drawRect(marginLeft, marginTop, plotWidth, plotHeight);    // the frame around the plot
            drawYAxis(g2);
            drawXAxis(g2);
            drawLines(g2);
        }

        // Light grid lines and the numbers down the left side
        void drawYAxis(Graphics2D g2) {
            for (int k = 0; k <= 4; k++) {
                double value = yMin + yRange * k / 4;
                int y = getHeight() - marginBottom - (int) (k * plotHeight / 4.0);

                g2.setColor(new Color(225, 225, 225));
                g2.drawLine(marginLeft, y, getWidth() - marginRight, y);

                // On a log scale the axis holds log10 values, so convert back for the label
                if (logScale) {
                    value = Math.pow(10, value);
                }
                g2.setColor(Color.BLACK);
                g2.drawString(String.format("%.3g", value), 5, y + 4);
            }

            g2.drawString("ms", 5, marginTop - 8);
        }

        // The n values along the bottom and the axis title
        void drawXAxis(Graphics2D g2) {
            g2.setColor(Color.BLACK);

            int lastLabelX = -100;
            for (double x : xValues) {
                int px = xToPixel(x);

                // Skip a label if it would overlap the previous one
                if (px - lastLabelX > 42) {
                    g2.drawString(String.valueOf((long) x), px - 12, getHeight() - marginBottom + 16);
                    lastLabelX = px;
                }
            }

            String axisTitle = "n";
            if (logScale) {
                axisTitle += "  (log-log scale)";
            }
            g2.drawString(axisTitle, getWidth() / 2, getHeight() - 6);
        }

        // One coloured line for each algorithm, with a dot at every measured point
        void drawLines(Graphics2D g2) {
            for (int s = 0; s < yValues.length; s++) {
                g2.setColor(COLORS[s % COLORS.length]);

                int prevX = -1;
                int prevY = -1;

                for (int i = 0; i < xValues.length; i++) {
                    double y = yValues[s][i];

                    // Points that were not measured (NaN) are skipped
                    if (!Double.isNaN(y)) {
                        int px = xToPixel(xValues[i]);
                        int py = yToPixel(y);

                        if (prevX >= 0) {
                            g2.drawLine(prevX, prevY, px, py);    // join to the previous point
                        }
                        g2.fillOval(px - 3, py - 3, 6, 6);

                        prevX = px;
                        prevY = py;
                    }
                }

                // The legend: the line's name in its own colour, at the top left of the plot
                g2.drawString(seriesNames[s], marginLeft + 10, marginTop + 16 + s * 16);
            }
        }
    }
    // ===== BLOCK 17: Module D - AnalysisTab (timing tests, report and charts) =====

    // The "D: Complexity Analysis" tab.
    // Times all 6 sorting algorithms on different input sizes and types,
    // then shows the numbers in a report and draws them as two charts.
    static class AnalysisTab extends JPanel {

        static final int[] SIZES = {100, 500, 1000, 2000, 5000, 10000, 50000, 100000};
        static final String[] KINDS = {"Random", "Already sorted", "Reverse sorted"};

        // The numbers for KINDS, so the code below reads better than "kind == 0"
        static final int RANDOM = 0;
        static final int SORTED = 1;
        static final int REVERSE = 2;

        // 6 algorithms: numbers 0, 1, 2 are the O(n^2) sorts and 3, 4, 5 are the O(n log n) sorts
        static final int ALGORITHMS = 6;

        // results[algorithm][kind][size index] = average time in ms. NaN means "not run".
        double[][][] results;

        JButton runButton;
        JLabel statusLabel = new JLabel(" Press the button. This takes roughly half a minute.");
        JComboBox<String> kindBox = new JComboBox<>(KINDS);
        JCheckBox logBox = new JCheckBox("Log scale");
        JTextArea reportArea = makeOutputArea();
        LineChart quadChart = new LineChart();      // the O(n^2) sorts
        LineChart fastChart = new LineChart();      // the O(n log n) sorts

        AnalysisTab() {
            super(new BorderLayout());

            runButton = makeButton("Run full analysis", e -> runAnalysis());

            // Changing the input type or the log scale redraws the charts
            kindBox.addActionListener(e -> refreshCharts());
            logBox.addActionListener(e -> refreshCharts());

            // Top bar
            JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
            topBar.add(runButton);
            topBar.add(new JLabel("Chart input type:"));
            topBar.add(kindBox);
            topBar.add(logBox);
            topBar.add(statusLabel);
            add(topBar, BorderLayout.NORTH);

            // The two charts on top of each other
            JPanel chartsPanel = new JPanel(new GridLayout(2, 1));
            chartsPanel.add(quadChart);
            chartsPanel.add(fastChart);

            // Report on the left, charts on the right
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(reportArea), chartsPanel);
            split.setResizeWeight(0.5);
            add(split, BorderLayout.CENTER);
        }

        // ---------------------------------------------------------------
        // Running the tests
        // ---------------------------------------------------------------

        // "Run full analysis" button.
        // The tests take a while, so they run on a separate thread. If they ran on the
        // normal Swing thread, the whole window would freeze until they finished.
        void runAnalysis() {
            runButton.setEnabled(false);

            // A thread with a big stack, because the recursive sorts can go very deep
            Thread worker = new Thread(null, () -> {
                measureEverything();

                // Swing screens must only be updated from the Swing thread, so we hand
                // the final update over with invokeLater
                SwingUtilities.invokeLater(() -> {
                    reportArea.setText(buildReport());
                    reportArea.setCaretPosition(0);     // scroll back to the top
                    refreshCharts();
                    statusLabel.setText(" Done.");
                    runButton.setEnabled(true);
                });
            }, "analysis", 512L * 1024 * 1024);

            worker.start();
        }

        // Runs every algorithm on every input kind and size and fills the results array
        void measureEverything() {
            results = new double[ALGORITHMS][KINDS.length][SIZES.length];

            // Start with NaN everywhere, which means "not run"
            for (int algorithm = 0; algorithm < ALGORITHMS; algorithm++) {
                for (int kind = 0; kind < KINDS.length; kind++) {
                    for (int i = 0; i < SIZES.length; i++) {
                        results[algorithm][kind][i] = Double.NaN;
                    }
                }
            }

            // Warm-up: run each algorithm once so Java has "warmed up" before we time anything.
            // These results are thrown away.
            int[] warmUpData = Backend.randomArray(2000, 100000, 1);
            for (int algorithm = 0; algorithm < ALGORITHMS; algorithm++) {
                Backend.avgMillis(algorithm, warmUpData, 2);
            }

            // The real measurements
            for (int algorithm = 0; algorithm < ALGORITHMS; algorithm++) {
                for (int kind = 0; kind < KINDS.length; kind++) {
                    for (int i = 0; i < SIZES.length; i++) {
                        int n = SIZES[i];

                        if (shouldSkip(algorithm, kind, n)) {
                            continue;
                        }

                        // Tell the user what is running right now
                        String message = " Running " + Backend.NAMES[algorithm] + ", " + KINDS[kind] + ", n = " + n + " ...";
                        SwingUtilities.invokeLater(() -> statusLabel.setText(message));

                        int[] data = makeInput(kind, n);
                        results[algorithm][kind][i] = Backend.avgMillis(algorithm, data, 3);    // average of 3 runs
                    }
                }
            }
        }

        // Some combinations would take far too long, so we leave them out
        boolean shouldSkip(int algorithm, int kind, int n) {
            // The O(n^2) sorts (algorithms 0, 1, 2) only go up to n = 10,000
            if (algorithm < 3 && n > 10000) {
                return true;
            }

            // Quicksort (algorithm 4) always picks the first element as pivot, so on sorted or
            // reversed data it becomes O(n^2). At 50,000 and above that would take minutes.
            if (algorithm == 4 && n > 10000 && kind != RANDOM) {
                return true;
            }

            return false;
        }

        // Makes the test data: random, already sorted, or reversed
        int[] makeInput(int kind, int n) {
            if (kind == RANDOM) {
                return Backend.randomArray(n, 1000000, 12345);
            } else if (kind == SORTED) {
                return Backend.sortedArray(n);
            } else {
                return Backend.reverseArray(n);
            }
        }

        // ---------------------------------------------------------------
        // Showing the results
        // ---------------------------------------------------------------

        // Turns the results into a text table for the left side
        String buildReport() {
            StringBuilder text = new StringBuilder();

            for (int kind = 0; kind < KINDS.length; kind++) {
                text.append("=== " + KINDS[kind] + " input: average time in ms (3 runs) ===\n");

                // Heading row: the n values
                text.append(String.format("%-26s", "n"));
                for (int n : SIZES) {
                    text.append(String.format("%11d", n));
                }
                text.append("\n");

                // One row for each algorithm
                for (int algorithm = 0; algorithm < ALGORITHMS; algorithm++) {
                    text.append(String.format("%-26s", Backend.NAMES[algorithm]));

                    for (int i = 0; i < SIZES.length; i++) {
                        double value = results[algorithm][kind][i];

                        if (Double.isNaN(value)) {
                            text.append(String.format("%11s", "-"));
                        } else {
                            text.append(String.format("%11.3f", value));
                        }
                    }
                    text.append("\n");
                }
                text.append("\n");
            }

            text.append("'-' = not run (O(n^2) sorts stop at n = 10,000; "
                    + "first-pivot quicksort on sorted/reverse data stops at 10,000 too).\n");
            return text.toString();
        }

        // Redraws both charts for the chosen input type and scale
        void refreshCharts() {
            // Nothing to draw until the analysis has been run
            if (results == null) {
                return;
            }

            int kind = kindBox.getSelectedIndex();
            boolean logScale = logBox.isSelected();

            // Chart 1: the O(n^2) sorts (algorithms 0, 1, 2).
            // They only ran up to n = 10,000, which is the first 6 sizes.
            int quadPoints = 6;
            double[] quadX = new double[quadPoints];
            double[][] quadY = new double[3][quadPoints];

            for (int i = 0; i < quadPoints; i++) {
                quadX[i] = SIZES[i];
            }
            for (int s = 0; s < 3; s++) {
                for (int i = 0; i < quadPoints; i++) {
                    quadY[s][i] = results[s][kind][i];
                }
            }

            // Chart 2: the O(n log n) sorts (algorithms 3, 4, 5), using all the sizes
            double[] fastX = new double[SIZES.length];
            double[][] fastY = new double[3][SIZES.length];

            for (int i = 0; i < SIZES.length; i++) {
                fastX[i] = SIZES[i];
            }
            for (int s = 0; s < 3; s++) {
                for (int i = 0; i < SIZES.length; i++) {
                    fastY[s][i] = results[s + 3][kind][i];
                }
            }

            String[] quadNames = {Backend.NAMES[0], Backend.NAMES[1], Backend.NAMES[2]};
            String[] fastNames = {Backend.NAMES[3], Backend.NAMES[4], Backend.NAMES[5]};

            quadChart.setState("O(n^2) sorts - " + KINDS[kind] + " input", quadX, quadNames, quadY, logScale);
            fastChart.setState("O(n log n) sorts - " + KINDS[kind] + " input", fastX, fastNames, fastY, logScale);
        }
    }

}   // <- this is the closing brace of the whole AlgorithmExplorer class. It is the LAST line of the file.

    //Arthur, place the code here.
    //it is about the AnalysisTab. it turns every sorting algorithm on different input sizes,
    //shows the timing report as a table and draws the two charts with the the line chart.

    

}
