import java.util.Random;

//Back end of the entire project.
/*
it has all the real algorithms and data structures, written with plain arrays and our own node
classes. There is no window code in here so everything can be tested without messups
the front end calls the methods in this file
*/

public class main {

  public static class Run {
    public final String result;
    public final long count;
    public final long nanos;

    Run(String result, long count, long nanos) {
      this.result = result;
      this.count = count;
      this.nanos = nanos;
    }
  }

  //counts recursive calls, set back to 0 at the start of every run
  private static long calls;

  //fibonacci naive recurssion
  private static long fibNaive(int n) {
    calls ++;

    if (n < 2) {
      return n;
    }
    return fibNaive(n - 1) + fibNaive(n -2);

  }

  // Fibonacci with memoisation: every answer is saved in "memo", so each sub-problem is solved once.
    // Time O(n), space O(n).
    private static long fibMemo(int n, long[] memo) {
        calls++;

        if (n < 2) {
            return n;
        }
        if (memo[n] != -1) {
            return memo[n];    // already worked out, just use it
        }

        memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        return memo[n];
    }

    // Fibonacci, bottom-up: a simple loop that only remembers the last two values.
    // Time O(n), space O(1).
    private static long fibBottomUp(int n) {
        if (n == 0) {
            return 0;
        }

        long previous = 0;
        long current = 1;

        for (int i = 2; i <= n; i++) {
            long next = previous + current;
            previous = current;
            current = next;
            calls++;
        }

        return current;
    }

    // Runs one of the three Fibonacci methods and measures it.
    // method: 0 = naive recursion, 1 = memoised recursion, 2 = bottom-up
    public static Run fibonacci(int method, int n) {
        calls = 0;
        long answer;
        long start = System.nanoTime();

        if (method == 0) {
            answer = fibNaive(n);
        } else if (method == 1) {
            // memo[i] holds fib(i) once it is known, or -1 if it has not been worked out yet
            long[] memo = new long[n + 2];
            for (int i = 0; i < memo.length; i++) {
                memo[i] = -1;
            }
            answer = fibMemo(n, memo);
        } else {
            answer = fibBottomUp(n);
        }

        long elapsed = System.nanoTime() - start;
        return new Run(Long.toString(answer), calls, elapsed);
    }

  //Factorial and the tower of Hanoi solver
  // Factorial, recursive. Time O(n), space O(n) for the call stack.
    private static long factRec(int n) {
        calls++;

        if (n <= 1) {
            return 1;    // the base case, which also covers 0! = 1
        }
        return n * factRec(n - 1);
    }

    // Factorial, with a loop. Time O(n), space O(1).
    private static long factIterative(int n) {
        long answer = 1;

        for (int i = 2; i <= n; i++) {
            answer = answer * i;
            calls++;
        }

        return answer;
    }

    // Runs the recursive or the iterative factorial and measures it.
    // n must be from 0 to 20, because 21! is too big for a long.
    public static Run factorial(boolean recursive, int n) {
        calls = 0;
        long answer;
        long start = System.nanoTime();

        if (recursive) {
            answer = factRec(n);
        } else {
            answer = factIterative(n);
        }

        long elapsed = System.nanoTime() - start;
        return new Run(Long.toString(answer), calls, elapsed);
    }

  //Tower of Hanoi solver
  public static class Hanoi {
    public final int[] disk;
    public final int[] from;
    public final int[] to;
    public int count;   //how many moves have been recorded so far

    public Hanoi(int n, int source, int destination, int auxillary) {
      int totalMoves = (1 << n) - 1;
      disk = new int[totalMoves];
      from = new int[totalMoves];
      to = new int[totalMoves];

      solve(n, source, destination, auxillary);
    }

    //moves n disks from peg to peg using a third peg as helper
    private void solve(int n, int source, int destination, int auxillary) {
      if (n == 0) {
        return; //no disks to move
      }

      solve(n - 1, source, auxillary, destination);

      disk[count] = n;
      from[count] = source;
      to[count] = destination;
      count++;

      //n - 1 smaller diks from helper peg on top of the big disk
      solve(n - 1, auxillary, destination, source);

    }
  }

  //ArrayStack
  public static class ArrayStack {
        public final int[] data;
        public int top = -1;

        public ArrayStack(int capacity) {
            if (capacity < 1) {
                throw new IllegalArgumentException("Capacity must be at least 1.");
            }
            data = new int[capacity];
        }

        // Adds a value on top of the stack
        public void push(int value) {
            if (isFull()) {
                throw new IllegalStateException("Stack overflow: the stack is full, cannot push " + value);
            }
            top++;
            data[top] = value;
        }

        // Removes the top value and gives it back
        public int pop() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack underflow: the stack is empty");
            }
            int value = data[top];
            top--;
            return value;
        }

        // Gives back the top value WITHOUT removing it
        public int peek() {
            if (isEmpty()) {
                throw new IllegalStateException("Stack is empty, nothing to peek");
            }
            return data[top];
        }

        public int size() {
            return top + 1;
        }

        public boolean isEmpty() {
            return top == -1;
        }

        public boolean isFull() {
            return top == data.length - 1;
        }

        // Turns the stack into text from bottom to top, e.g. "[ 3 5 8 ]".
        // If asCharacters is true, the numbers are shown as characters (used by the bracket checker).
        public String show(boolean asCharacters) {
            StringBuilder text = new StringBuilder("[ ");

            for (int i = 0; i <= top; i++) {
                if (asCharacters) {
                    text.append((char) data[i]);
                } else {
                    text.append(data[i]);
                }
                text.append(' ');
            }

            text.append("]");
            return text.toString();
        }
    }

  //The Queues - circular and Linear
 // ===== BACKEND BLOCK B4: Queues (shared base class, linear queue, circular queue) =====

    // The part that both queues have in common.
    // "front" is the index of the first item, "rear" is the index of the last item.
    // "abstract" means you can't create an ArrayQueue directly: the two queues below
    // extend it and each writes its own enqueue, dequeue, size and so on.
    public static abstract class ArrayQueue {
        public final int[] arr;
        public int front = 0;
        public int rear = -1;

        ArrayQueue(int capacity) {
            if (capacity < 1) {
                throw new IllegalArgumentException("Capacity must be at least 1.");
            }
            arr = new int[capacity];
        }

        public abstract void enqueue(int value);
        public abstract int dequeue();
        public abstract int size();
        public abstract boolean isFull();
        public abstract boolean[] occupied();    // which slots hold items right now (used for drawing)

        public boolean isEmpty() {
            return size() == 0;
        }

        // Gives back the first item WITHOUT removing it
        public int front() {
            if (isEmpty()) {
                throw new IllegalStateException("Queue is empty, no front element");
            }
            return arr[front];
        }
    }

    // Linear queue: front and rear only ever move forward.
    // So when items are dequeued, the free slots at the front can never be used again.
    public static class LinearQueue extends ArrayQueue {

        public LinearQueue(int capacity) {
            super(capacity);
        }

        @Override
        public void enqueue(int value) {
            if (rear == arr.length - 1) {
                // Rear is on the last slot. Is the queue really full, or is it a "false full"?
                if (front > 0) {
                    throw new IllegalStateException("FALSE FULL: rear is at the last index, yet " + front
                            + " slot(s) at the front are free but unusable. "
                            + "A circular queue fixes this by wrapping rear around.");
                }
                throw new IllegalStateException("Queue is full");
            }

            rear++;
            arr[rear] = value;
        }

        @Override
        public int dequeue() {
            if (isEmpty()) {
                throw new IllegalStateException("Queue underflow: the queue is empty");
            }

            int value = arr[front];
            front++;
            return value;
        }

        @Override
        public int size() {
            if (rear >= front) {
                return rear - front + 1;
            }
            return 0;    // front has passed rear, so nothing is left
        }

        @Override
        public boolean isFull() {
            return rear == arr.length - 1;
        }

        // The items are the slots from front to rear
        @Override
        public boolean[] occupied() {
            boolean[] result = new boolean[arr.length];

            for (int i = front; i <= rear; i++) {
                result[i] = true;
            }

            return result;
        }
    }

    // Circular queue: the indexes wrap around using modulo (%),
    // so every free slot can be used again.
    public static class CircularQueue extends ArrayQueue {

        private int count = 0;    // how many items are in the queue right now

        public CircularQueue(int capacity) {
            super(capacity);
        }

        @Override
        public void enqueue(int value) {
            if (count == arr.length) {
                throw new IllegalStateException("Queue is full (circular queue, all " + count + " slots used)");
            }

            rear = (rear + 1) % arr.length;    // goes back to 0 after the last index
            arr[rear] = value;
            count++;
        }

        @Override
        public int dequeue() {
            if (count == 0) {
                throw new IllegalStateException("Queue underflow: the queue is empty");
            }

            int value = arr[front];
            front = (front + 1) % arr.length;
            count--;
            return value;
        }

        @Override
        public int size() {
            return count;
        }

        @Override
        public boolean isFull() {
            return count == arr.length;
        }

        // The items start at front and run for "count" slots, wrapping around the end of the array
        @Override
        public boolean[] occupied() {
            boolean[] result = new boolean[arr.length];

            for (int k = 0; k < count; k++) {
                int index = (front + k) % arr.length;
                result[index] = true;
            }

            return result;
        }
    }





  //Linked list
  // ===== BACKEND BLOCK B5: Linked list base class and the singly linked list =====

    // What every linked list in this project can do.
    // The front end only talks to this type, so it doesn't care whether the list
    // is singly or doubly linked. "abstract" means each list writes its own versions.
    public static abstract class LList {
        public int size;    // how many nodes are in the list

        public abstract void insertBegin(int value);
        public abstract void insertEnd(int value);
        public abstract void insertBefore(int target, int value);
        public abstract void insertAfter(int target, int value);
        public abstract int deleteBegin();
        public abstract int deleteEnd();
        public abstract int deleteBefore(int target);
        public abstract int deleteAfter(int target);
        public abstract void deleteValue(int value);
        public abstract int search(int value);          // the position of the value, or -1 if it is not there
        public abstract int[] toArray();                // the values from head to end
        public abstract int[] toArrayBackward();        // the values from tail to head (doubly linked only)
        public abstract boolean isDoubly();

        // The two errors that every list can throw, so the messages are written only once
        static IllegalStateException notFound(int target) {
            return new IllegalStateException("Element " + target + " was not found in the list");
        }

        static IllegalStateException empty() {
            return new IllegalStateException("The list is empty");
        }
    }

    // One node of the singly linked list: a value and a link to the next node
    private static class SNode {
        int value;
        SNode next;

        SNode(int value) {
            this.value = value;
        }
    }

    // Singly linked list: each node only knows the node AFTER it.
    // We keep one reference, "head", to the first node.
    public static class SLinkedList extends LList {

        private SNode head;

        // Finds the first node holding this value, or gives back null if there is none
        private SNode find(int value) {
            SNode current = head;
            while (current != null && current.value != value) {
                current = current.next;
            }
            return current;
        }

        // ---- Insert ----

        @Override
        public void insertBegin(int value) {
            SNode newNode = new SNode(value);
            newNode.next = head;     // the new node points at the old first node...
            head = newNode;          // ...and becomes the new first node
            size++;
        }

        @Override
        public void insertEnd(int value) {
            SNode newNode = new SNode(value);

            if (head == null) {
                head = newNode;      // the list was empty
            } else {
                // Walk to the last node and attach the new node after it
                SNode current = head;
                while (current.next != null) {
                    current = current.next;
                }
                current.next = newNode;
            }
            size++;
        }

        @Override
        public void insertAfter(int target, int value) {
            SNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }

            SNode newNode = new SNode(value);
            newNode.next = targetNode.next;
            targetNode.next = newNode;
            size++;
        }

        @Override
        public void insertBefore(int target, int value) {
            if (head == null) {
                throw empty();
            }

            // If the target is the first node, this is the same as inserting at the beginning
            if (head.value == target) {
                insertBegin(value);
                return;
            }

            // Walk until "previous" is the node just BEFORE the target
            SNode previous = head;
            while (previous.next != null && previous.next.value != target) {
                previous = previous.next;
            }
            if (previous.next == null) {
                throw notFound(target);
            }

            SNode newNode = new SNode(value);
            newNode.next = previous.next;
            previous.next = newNode;
            size++;
        }

        // ---- Delete ----

        @Override
        public int deleteBegin() {
            if (head == null) {
                throw empty();
            }

            int value = head.value;
            head = head.next;
            size--;
            return value;
        }

        @Override
        public int deleteEnd() {
            if (head == null) {
                throw empty();
            }

            // Only one node: the list becomes empty
            if (head.next == null) {
                int onlyValue = head.value;
                head = null;
                size--;
                return onlyValue;
            }

            // Otherwise walk to the second-last node, and cut off the last one
            SNode previous = head;
            while (previous.next.next != null) {
                previous = previous.next;
            }

            int lastValue = previous.next.value;
            previous.next = null;
            size--;
            return lastValue;
        }

        @Override
        public int deleteAfter(int target) {
            SNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }
            if (targetNode.next == null) {
                throw new IllegalStateException("There is no element after " + target);
            }

            int value = targetNode.next.value;
            targetNode.next = targetNode.next.next;    // skip over the deleted node
            size--;
            return value;
        }

        @Override
        public int deleteBefore(int target) {
            if (head == null) {
                throw empty();
            }
            if (head.value == target) {
                throw new IllegalStateException("There is no element before " + target);
            }

            // If the target is the second node, the node before it is the head
            if (head.next != null && head.next.value == target) {
                return deleteBegin();
            }

            // Otherwise walk until the node AFTER "previous" is the one to delete,
            // which means the node after that is the target
            SNode previous = head;
            while (previous.next != null && previous.next.next != null
                    && previous.next.next.value != target) {
                previous = previous.next;
            }
            if (previous.next == null || previous.next.next == null) {
                throw notFound(target);
            }

            int value = previous.next.value;
            previous.next = previous.next.next;
            size--;
            return value;
        }

        @Override
        public void deleteValue(int value) {
            if (head == null) {
                throw empty();
            }

            // The value is in the first node
            if (head.value == value) {
                deleteBegin();
                return;
            }

            // Walk until "previous" is the node just before the one to delete
            SNode previous = head;
            while (previous.next != null && previous.next.value != value) {
                previous = previous.next;
            }
            if (previous.next == null) {
                throw notFound(value);
            }

            previous.next = previous.next.next;
            size--;
        }

        // ---- Search and display ----

        @Override
        public int search(int value) {
            int position = 0;
            SNode current = head;

            while (current != null) {
                if (current.value == value) {
                    return position;
                }
                current = current.next;
                position++;
            }

            return -1;    // not found
        }

        @Override
        public int[] toArray() {
            int[] result = new int[size];
            int index = 0;
            SNode current = head;

            while (current != null) {
                result[index] = current.value;
                index++;
                current = current.next;
            }

            return result;
        }

        @Override
        public int[] toArrayBackward() {
            throw new IllegalStateException("Backward display needs a doubly linked list");
        }

        @Override
        public boolean isDoubly() {
            return false;
        }
    }

  //DOubly Linked Lists
  private static class DNode {
        int value;
        DNode next;
        DNode prev;

        DNode(int value) {
            this.value = value;
        }
    }

  public static class DLinkedList extends LList {
    private DNode head;
    private DNode tail;

    private DNode find(int value) {
      DNode current = head;
      while (current != null && current.value != value) {
        current = current.next;
      }
      return current;

    }

    private int unlink(DNode node) {
            // The node before it now skips over it (or, if it was the head, head moves forward)
            if (node.prev != null) {
                node.prev.next = node.next;
            } else {
                head = node.next;
            }

            // The node after it now points back past it (or, if it was the tail, tail moves back)
            if (node.next != null) {
                node.next.prev = node.prev;
            } else {
                tail = node.prev;
            }

            size--;
            return node.value;
        }

        // ---- Insert ----

        @Override
        public void insertBegin(int value) {
            DNode newNode = new DNode(value);
            newNode.next = head;

            if (head != null) {
                head.prev = newNode;
            } else {
                tail = newNode;      // the list was empty, so the new node is also the tail
            }

            head = newNode;
            size++;
        }

        @Override
        public void insertEnd(int value) {
            DNode newNode = new DNode(value);
            newNode.prev = tail;

            if (tail != null) {
                tail.next = newNode;
            } else {
                head = newNode;      // the list was empty, so the new node is also the head
            }

            tail = newNode;
            size++;
        }

        @Override
        public void insertAfter(int target, int value) {
            DNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }

            DNode newNode = new DNode(value);
            newNode.prev = targetNode;
            newNode.next = targetNode.next;

            if (targetNode.next != null) {
                targetNode.next.prev = newNode;
            } else {
                tail = newNode;      // the target was the last node, so the new node is the new tail
            }

            targetNode.next = newNode;
            size++;
        }

        @Override
        public void insertBefore(int target, int value) {
            DNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }

            DNode newNode = new DNode(value);
            newNode.next = targetNode;
            newNode.prev = targetNode.prev;

            if (targetNode.prev != null) {
                targetNode.prev.next = newNode;
            } else {
                head = newNode;      // the target was the first node, so the new node is the new head
            }

            targetNode.prev = newNode;
            size++;
        }

        // ---- Delete ----

        @Override
        public int deleteBegin() {
            if (head == null) {
                throw empty();
            }
            return unlink(head);
        }

        @Override
        public int deleteEnd() {
            if (tail == null) {
                throw empty();
            }
            return unlink(tail);
        }

        @Override
        public int deleteAfter(int target) {
            DNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }
            if (targetNode.next == null) {
                throw new IllegalStateException("There is no element after " + target);
            }
            return unlink(targetNode.next);
        }

        @Override
        public int deleteBefore(int target) {
            DNode targetNode = find(target);
            if (targetNode == null) {
                throw notFound(target);
            }
            if (targetNode.prev == null) {
                throw new IllegalStateException("There is no element before " + target);
            }
            return unlink(targetNode.prev);
        }

        @Override
        public void deleteValue(int value) {
            DNode node = find(value);
            if (node == null) {
                throw notFound(value);
            }
            unlink(node);
        }

        // ---- Search and display ----

        @Override
        public int search(int value) {
            int position = 0;
            DNode current = head;

            while (current != null) {
                if (current.value == value) {
                    return position;
                }
                current = current.next;
                position++;
            }

            return -1;    // not found
        }

        // The values from head to tail
        @Override
        public int[] toArray() {
            int[] result = new int[size];
            int index = 0;
            DNode current = head;

            while (current != null) {
                result[index] = current.value;
                index++;
                current = current.next;
            }

            return result;
        }

        // The values from tail to head, following the "prev" links
        @Override
        public int[] toArrayBackward() {
            int[] result = new int[size];
            int index = 0;
            DNode current = tail;

            while (current != null) {
                result[index] = current.value;
                index++;
                current = current.prev;
            }

            return result;
        }

        @Override
        public boolean isDoubly() {
            return true;
        }

  }

  //Bracket Checker
  //Arthur place your code below this comment
    // ===== BACKEND BLOCK B7: Bracket checker (a use of the stack) =====

    // Checks whether an opening bracket and a closing bracket belong together
    private static boolean matches(char open, char close) {
        if (open == '(' && close == ')') {
            return true;
        }
        if (open == '[' && close == ']') {
            return true;
        }
        if (open == '{' && close == '}') {
            return true;
        }
        return false;
    }

    // Is the bracket expression balanced? The idea:
    //   - when we read an opening bracket, push it on the stack
    //   - when we read a closing bracket, pop the top of the stack; it must be its partner
    //   - at the end the stack must be empty
    // The "trace" gets one line of explanation for every symbol, showing the stack each time.
    // Time O(n).
    public static boolean checkBrackets(String expression, StringBuilder trace) {
        // The stack can never need more room than the length of the expression
        ArrayStack stack = new ArrayStack(Math.max(1, expression.length()));

        for (int i = 0; i < expression.length(); i++) {
            char symbol = expression.charAt(i);

            // Spaces are skipped without saying anything
            if (symbol == ' ') {
                continue;
            }

            boolean isOpening = (symbol == '(' || symbol == '[' || symbol == '{');
            boolean isClosing = (symbol == ')' || symbol == ']' || symbol == '}');

            if (isOpening) {
                stack.push(symbol);    // the stack holds the character as a number
                trace.append("Read '" + symbol + "' -> push           stack: " + stack.show(true) + "\n");

            } else if (isClosing) {
                // A closing bracket with nothing open to match it
                if (stack.isEmpty()) {
                    trace.append("Read '" + symbol + "' -> stack is empty, nothing to match with. NOT balanced\n");
                    return false;
                }

                char openBracket = (char) stack.pop();

                // The brackets are different kinds, e.g. ( closed by ]
                if (!matches(openBracket, symbol)) {
                    trace.append("Read '" + symbol + "' -> popped '" + openBracket + "', they do not match. NOT balanced\n");
                    return false;
                }

                trace.append("Read '" + symbol + "' -> matches '" + openBracket + "', pop  stack: " + stack.show(true) + "\n");

            } else {
                // Letters, numbers, operators and so on do not matter here
                trace.append("Read '" + symbol + "' -> not a bracket, ignored\n");
            }
        }

        // We read everything. Any bracket still on the stack was never closed.
        if (!stack.isEmpty()) {
            trace.append("End of input: " + stack.size() + " opening bracket(s) never closed: " + stack.show(true) + "\n");
            return false;
        }

        trace.append("End of input: stack is empty\n");
        return true;
    }

  //SORTING MODULE - the algorithm names, the complexity table and the Steps class that records
  //each step for the animation

  public static final String[] NAMES = {
    "Bubble sort",
    "Selection sort",
    "Insertion sort",
    "Merge sort",
    "Quicksort (first pivot)",
    "Quicksort (random pivot)",
  };

  //complexity table
  public static final String[][] INFO = {
        {"Bubble sort", "O(n) (early exit)", "O(n^2)", "O(n^2)", "O(1)", "Yes"},
        {"Selection sort", "O(n^2)", "O(n^2)", "O(n^2)", "O(1)", "No"},
        {"Insertion sort", "O(n)", "O(n^2)", "O(n^2)", "O(1)", "Yes"},
        {"Merge sort", "O(n log n)", "O(n log n)", "O(n log n)", "O(n)", "Yes"},
        {"Quicksort (first pivot)", "O(n log n)", "O(n log n)", "O(n^2) on sorted input", "O(log n) avg, O(n) worst", "No"},
        {"Quicksort (random pivot)", "O(n log n)", "O(n log n)", "O(n^2) (very unlikely)", "O(log n) expected", "No"}
    };

  //records animation of a sort
  public static class Steps {

        public static final int MAX = 3000;       // stop recording after this many frames, so a long sort can't use up all the memory
        private static final int START_SIZE = 16; // the arrays start small and grow when they fill up

        public int[][] snaps = new int[START_SIZE][];
        public String[] notes = new String[START_SIZE];
        public int[] hi1 = new int[START_SIZE];
        public int[] hi2 = new int[START_SIZE];
        public int count;                          // how many frames are saved

        // Saves one frame
        public void add(int[] array, int highlightFrom, int highlightTo, String note) {
            if (count >= MAX) {
                return;
            }
            if (count == snaps.length) {
                grow();
            }

            // Save a COPY, because the real array keeps changing as the sort goes on
            int[] copy = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                copy[i] = array[i];
            }

            snaps[count] = copy;
            notes[count] = note;
            hi1[count] = highlightFrom;
            hi2[count] = highlightTo;
            count++;
        }

        // Makes all four arrays twice as big and copies the saved frames across
        private void grow() {
            int newSize = snaps.length * 2;

            int[][] newSnaps = new int[newSize][];
            String[] newNotes = new String[newSize];
            int[] newHi1 = new int[newSize];
            int[] newHi2 = new int[newSize];

            for (int i = 0; i < count; i++) {
                newSnaps[i] = snaps[i];
                newNotes[i] = notes[i];
                newHi1[i] = hi1[i];
                newHi2[i] = hi2[i];
            }

            snaps = newSnaps;
            notes = newNotes;
            hi1 = newHi1;
            hi2 = newHi2;
        }
    }
  // Sorter class
  public static class Sorter {

        public long comparisons;
        public long moves;                      // swaps or moves, depending on the algorithm
        public Steps steps;                     // null = don't record the steps
        private final Random random = new Random();    // used by quicksort with a random pivot

        // Sorts the array in place using the chosen algorithm.
        // algorithm: 0 bubble, 1 selection, 2 insertion, 3 merge, 4 quicksort (first pivot), 5 quicksort (random pivot)
        public void sort(int algorithm, int[] array) {
            comparisons = 0;
            moves = 0;

            if (steps != null) {
                steps.add(array, -1, -1, "Initial array");
            }

            // An array with 0 or 1 numbers is already sorted
            if (array.length < 2) {
                return;
            }

            switch (algorithm) {
                case 0:
                    bubble(array);
                    break;
                case 1:
                    selection(array);
                    break;
                case 2:
                    insertion(array);
                    break;
                case 3:
                    mergeSort(array, new int[array.length], 0, array.length - 1);
                    break;
                case 4:
                    quick(array, 0, array.length - 1, false);
                    break;
                default:
                    quick(array, 0, array.length - 1, true);
            }
        }

        // Swaps two numbers in the array and counts it as one move
        private void swap(int[] array, int i, int j) {
            if (i == j) {
                return;    // same position, nothing to do (and not counted)
            }

            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            moves++;
        }

        // BUBBLE SORT
        private void bubble(int[] array) {
            int n = array.length;

            for (int pass = 0; pass < n - 1; pass++) {
                boolean swapped = false;

                // Compare neighbours; the biggest value "bubbles" to the end of the unsorted part
                for (int j = 0; j < n - 1 - pass; j++) {
                    comparisons++;
                    if (array[j] > array[j + 1]) {
                        swap(array, j, j + 1);
                        swapped = true;
                    }
                }

                if (steps != null) {
                    int placedIndex = n - 1 - pass;
                    steps.add(array, placedIndex, placedIndex,
                            "After pass " + (pass + 1) + ": the largest remaining value bubbled to index " + placedIndex);
                }

                // A whole pass with no swaps means the array is already sorted
                if (!swapped) {
                    break;
                }
            }
        }

        // SELECTION SORT
        private void selection(int[] array) {
            int n = array.length;

            for (int i = 0; i < n - 1; i++) {
                // Find the smallest value in the part that is not sorted yet
                int minIndex = i;
                for (int j = i + 1; j < n; j++) {
                    comparisons++;
                    if (array[j] < array[minIndex]) {
                        minIndex = j;
                    }
                }

                // Put it in its final place
                swap(array, i, minIndex);

                if (steps != null) {
                    steps.add(array, i, i,
                            "After pass " + (i + 1) + ": smallest remaining value placed at index " + i);
                }
            }
        }

       // INSERTION SORT
        private void insertion(int[] array) {
            for (int i = 1; i < array.length; i++) {
                int key = array[i];    // the value we are about to insert into the sorted part
                int j = i - 1;

                // Shift bigger values one place to the right to make a gap for the key
                while (j >= 0) {
                    comparisons++;
                    if (array[j] > key) {
                        array[j + 1] = array[j];
                        moves++;
                        j--;
                    } else {
                        break;    // found the right place
                    }
                }

                // Drop the key into the gap (unless it did not need to move at all)
                if (j + 1 != i) {
                    array[j + 1] = key;
                    moves++;
                }

                if (steps != null) {
                    steps.add(array, 0, i,
                            "After inserting element " + (i + 1) + ": the first " + (i + 1) + " elements are sorted");
                }
            }
        }
   //MERGE SORT

    private void mergeSort(int[] array, int[] temp, int low, int high) {
            // One number (or none) is already sorted
            if (low >= high) {
                return;
            }

            int mid = (low + high) / 2;
            mergeSort(array, temp, low, mid);          // sort the left half
            mergeSort(array, temp, mid + 1, high);     // sort the right half

            // Merge: repeatedly take the smaller of the two front numbers
            int leftIndex = low;
            int rightIndex = mid + 1;
            int tempIndex = low;

            while (leftIndex <= mid && rightIndex <= high) {
                comparisons++;

                // "<=" (not "<") keeps equal numbers in their original order, which makes the sort stable
                if (array[leftIndex] <= array[rightIndex]) {
                    temp[tempIndex] = array[leftIndex];
                    leftIndex++;
                } else {
                    temp[tempIndex] = array[rightIndex];
                    rightIndex++;
                }
                tempIndex++;
            }

            // One half ran out first; copy whatever is left of the other half
            while (leftIndex <= mid) {
                temp[tempIndex] = array[leftIndex];
                leftIndex++;
                tempIndex++;
            }
            while (rightIndex <= high) {
                temp[tempIndex] = array[rightIndex];
                rightIndex++;
                tempIndex++;
            }

            // Copy the merged numbers from temp back into the real array
            for (int index = low; index <= high; index++) {
                array[index] = temp[index];
                moves++;
            }

            if (steps != null) {
                steps.add(array, low, high, "Merged a[" + low + ".." + high + "]");
            }
        }

    //QUICKSORT

    private void quick(int[] array, int low, int high, boolean randomPivot) {
            if (low >= high) {
                return;
            }

            // With a random pivot, swap a random element to the front so it becomes the pivot
            if (randomPivot) {
                int randomIndex = low + random.nextInt(high - low + 1);
                swap(array, low, randomIndex);
            }

            int pivot = array[low];
            int smallerEnd = low;

            for (int j = low + 1; j <= high; j++) {
                comparisons++;
                if (array[j] < pivot) {
                    smallerEnd++;
                    swap(array, smallerEnd, j);
                }
            }

            // Put the pivot into its final place
            swap(array, low, smallerEnd);

            if (steps != null) {
                steps.add(array, smallerEnd, smallerEnd,
                        "Partitioned a[" + low + ".." + high + "] around pivot " + pivot
                        + ", now fixed at index " + smallerEnd);
            }

            // Sort the two sides separately (the pivot itself is already in place)
            quick(array, low, smallerEnd - 1, randomPivot);
            quick(array, smallerEnd + 1, high, randomPivot);
        }
    }

// ===== BACKEND BLOCK B11: Helpers (input parsing, test data, timing) and the end of the file =====

    // ---------------------------------------------------------------
    // HELPERS: reading input, making test data, timing (used by Modules C and D)
    // ---------------------------------------------------------------

    // Turns text like "5, 3 8" into an array of numbers.
    // If something is wrong, it throws an error with a message the user can understand.
    public static int[] parseInts(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "The input is empty. Type numbers separated by commas or spaces, or generate random data.");
        }

        // Split on commas and/or spaces
        String[] parts = text.trim().split("[,\\s]+");

        if (parts.length > 20000) {
            throw new IllegalArgumentException("Too many numbers (maximum 20,000).");
        }

        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            // We read it as a long first, so a huge number gives our own "out of range"
            // message below instead of a confusing "not a number" error
            long value;
            try {
                value = Long.parseLong(parts[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + parts[i] + "' is not a whole number.");
            }

            if (value < -1000000 || value > 1000000) {
                throw new IllegalArgumentException("'" + parts[i] + "' is out of range (-1,000,000 to 1,000,000).");
            }

            numbers[i] = (int) value;
        }

        return numbers;
    }

    // Makes a separate copy of an array, so a sort can't change the original
    public static int[] copy(int[] original) {
        int[] result = new int[original.length];

        for (int i = 0; i < original.length; i++) {
            result[i] = original[i];
        }

        return result;
    }

    // n random numbers from 1 to bound. Using the same seed gives the same numbers every time,
    // so tests can be repeated.
    public static int[] randomArray(int n, int bound, long seed) {
        Random generator = new Random(seed);
        int[] numbers = new int[n];

        for (int i = 0; i < n; i++) {
            numbers[i] = 1 + generator.nextInt(bound);
        }

        return numbers;
    }

    // 0, 1, 2, ... n-1  (already sorted)
    public static int[] sortedArray(int n) {
        int[] numbers = new int[n];

        for (int i = 0; i < n; i++) {
            numbers[i] = i;
        }

        return numbers;
    }

    // n, n-1, ... 1  (sorted the wrong way round)
    public static int[] reverseArray(int n) {
        int[] numbers = new int[n];

        for (int i = 0; i < n; i++) {
            numbers[i] = n - i;
        }

        return numbers;
    }

    // Checks that the numbers are in increasing order
    public static boolean isSorted(int[] numbers) {
        for (int i = 1; i < numbers.length; i++) {
            if (numbers[i - 1] > numbers[i]) {
                return false;
            }
        }

        return true;
    }

    // Average running time in milliseconds over "repeats" runs.
    // Every run sorts a fresh copy of the source array, so each run starts from the same data.
    public static double avgMillis(int algorithm, int[] source, int repeats) {
        long totalNanos = 0;

        for (int run = 0; run < repeats; run++) {
            int[] copy = copy(source);
            Sorter sorter = new Sorter();

            long start = System.nanoTime();
            sorter.sort(algorithm, copy);
            totalNanos += System.nanoTime() - start;
        }

        return totalNanos / (double) repeats / 1e6;


}   // <- this is the closing brace of the whole Backend class. It is the LAST line of the file.



} //kindly leave this bracket untouched. make sure for whatsoever code placement you make, this brace
//isn't touched. it is for the main class (public class main{}) - hope you get the logic
