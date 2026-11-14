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

    purblic Hanoi(int n, int source, int destination, int auxillary) {
      int totalMove = (1 << n) - 1;
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
    
  public static class DLinkedList extends LLists {
    private DNode head;
    private DNode tail;

    private DNode find(int value) {
      DNode current = head;
      while (current != null  current.value != value) {
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

  
} //kindly leave this bracket untouched. make sure for whatsoever code placement you make, this brace
//isn't touched. it is for the main class (public class main{}) - hope you get the logic

