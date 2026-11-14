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
  //Precious place youre code below these comments here



  
  
  //Linked list
  //Joan place your code below these comments here




  
} //kindly leave this bracket untouched. make sure for whatsoever code placement you make, this brace
//isn't touched. it is for the main class (public class main{}) - hope you get the logic

