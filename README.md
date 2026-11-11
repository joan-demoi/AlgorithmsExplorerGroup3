
# AlgorithmsExplorerGroup3
DAA Group Project: Interactive Algorithm Explorer – Recursion, Data Structures &amp; Sorting 

Uganda Christian University  
Faculty of Engineering, Design and Technology  
Department of Computing and Technology  

**Course**: CSC2107 – Design and Analysis of Algorithms  
**Semester**: Advent Semester 2026  

---

## Group Members

| Name                            | Registration Number           | Role / Contribution Area                  |
|---------------------------------|-------------------------------|-------------------------------------------|
| Akao Joan Mary Odwee            |  M25B23/025                   | Project coordination, GUI, report         |
| Nalinda Precious Grace          | M25B23/027                    | Recursion Lab                             |
|Calvin Ainebyona Nomusinguzi     | S25B23/080                    | Stacks & Queues                           |
|Isingoma Arthur                  |S25B23/007                     | Linked Lists                              |
| Mark Trevor Ssali               |S25B23/003                     | Sorting Lab & Time Complexity Analysis    |

---

## Project Overview

# Algorithm Explorer

Algorithm Explorer is a desktop application written in Java (Swing). You type your own input, the algorithm
runs in the back end, and the screen shows the **result, the steps taken, the number of operations and the
running time**. It covers recursion, stacks, queues, linked lists, five sorting algorithms and an experimental
time-complexity analysis.


## 1. Requirements

- A **JDK** (Java Development Kit) version 11 or newer. JDK 17 or 21 is recommended.
  Download "Temurin" from https://adoptium.net. On Windows, tick **Add to PATH** and **Set JAVA_HOME** in the installer.
- Check the installation. Open a **new** terminal (Command Prompt on Windows) and type:

```
java -version
javac -version
```

Both must print a version number.

## 2. How to install and run

1. Download or clone this repository so that `Backend.java` and `AlgorithmExplorer.java` are in the **same folder**.
2. Open a terminal **inside that folder**.
3. Compile:

```
javac Backend.java AlgorithmExplorer.java
```

4. Run:

```
java AlgorithmExplorer
```

The application window opens. There is nothing else to install.

**Using an IDE instead (IntelliJ IDEA, Eclipse, VS Code):** create a new Java project, copy both files into the
`src` folder, open `AlgorithmExplorer.java` and press **Run** on its `main` method.

## 3. Project structure

```
Backend.java            BACK END: all algorithms and data structures (no screen code)
AlgorithmExplorer.java  FRONT END: the window, buttons, drawings and charts
README.md               this file
```

The back end never touches the interface, so any algorithm can be tested on its own, for example from a small
`main` method that creates a `Backend.Sorter` and calls `sort(...)`.

### Architecture

```
  User  --types input-->  AlgorithmExplorer (front end)  --calls-->  Backend (algorithms + counters)
  User  <--sees result--  validates, draws, shows charts  <--returns-- result, steps, operation counts
```

### How a run flows

1. The user types input or presses **Generate random**.
2. The front end **validates** it. Empty, non-numeric or out-of-range input shows a clear message instead of crashing.
3. The front end calls the back end (for example `Sorter.sort(...)`).
4. The back end does the work and counts the operations. The front end measures the time with `System.nanoTime()`.
5. The front end shows the result, the steps, the counts and the time.

---

## 4. What each module does

### Module A: Recursion Lab
| Screen | What you can do |
|---|---|
| **Fibonacci** | Enter n (0 to 90). Computes fib(n) by naive recursion, memoised recursion and bottom-up dynamic programming, and shows the result, the number of calls/iterations and the time for each. Naive recursion is limited to n up to 30 and the screen explains why (O(2^n) calls). |
| **Factorial** | Enter n (0 to 20). Computes n! recursively and iteratively with calls/iterations and time. n = 0 works (0! = 1) and negative input is rejected. |
| **Tower of Hanoi** | Choose 1 to 8 disks and which peg is the source, destination and auxiliary. The puzzle is solved recursively, every move is listed ("Move disk 1 from A to C"), and the disks are animated with **Start**, **Pause / Step**, **Reset** and a **Speed** slider. A counter shows the moves made and confirms the total equals 2^n - 1. |

### Module B: Data Structures Lab
Every structure is our own class built from plain arrays or our own node objects. Each operation has a button.
After every operation the structure is redrawn and a message such as "Popped 42" or "Queue is full" is shown.

| Tab | Operations |
|---|---|
| **Stack** (array, fixed capacity 1 to 12) | push, pop, peek, size, is_empty, is_full, display. Overflow and underflow give clear messages. |
| **Linear Queue** | enqueue, dequeue, front, size, is_empty, is_full, display. Fill it, dequeue a few items, then enqueue again to see the **FALSE FULL** problem. |
| **Circular Queue** | The same operations, with the front and rear indices shown so you can watch **rear wrap round** to the start of the array. |
| **Singly Linked List** | Insert at the beginning, at the end, before a given element and after a given element. Delete at the beginning, at the end, before and after a given element, and delete a given element. Search, size, display forward. |
| **Doubly Linked List** | Everything above, plus display backward. |
| **Bracket Checker** | Stack application: checks if an expression such as `{ ( [ ] ) }` is balanced and shows the stack after every symbol. |

### Module C: Sorting Lab
- Algorithms: **bubble, selection, insertion, merge, quicksort (first-element pivot)** and **quicksort (random pivot)**.
- Type your own numbers (separated by spaces or commas) or press **Generate random** (n from 1 to 20,000).
- **Run** shows the final result. **Step >** shows the array after each pass, merge or partition. **Auto-play / Stop** plays the steps automatically. **Reset** goes back to the start.
- Bars are **yellow** for values not yet finalised and **green** for the part that was just sorted, merged or fixed as a pivot.
- Step display works for up to 200 numbers. Larger inputs show the sorted result and the counts only.
- Every run shows the **number of comparisons, the number of swaps/moves and the time in milliseconds**.
- **Compare all** runs every algorithm on the same input and shows one table side by side.
- A help table lists each algorithm's best, average and worst case, extra space and whether it is stable.

### Module D: Time Complexity Analysis
- Press **Run full analysis**. Every algorithm is timed on **random, already sorted and reverse sorted** input for n = 100, 500, 1,000, 2,000, 5,000 and 10,000. Merge sort and quicksort also run on 50,000 and 100,000. Each time is the **average of 3 runs**.
- Results appear as three tables. Two charts are drawn: one for the O(n^2) sorts and one for the O(n log n) sorts.
- Use **Chart input type** to switch between random, sorted and reverse sorted data, and tick **Log scale** to see growth more clearly.
- Limits (on purpose, to keep the run short): the O(n^2) sorts stop at n = 10,000, and first-pivot quicksort on sorted or reverse data stops at n = 10,000 because its worst case is O(n^2). These cells show `-`.
- Very small n values can be noisy because Java needs a moment to warm up. Run the analysis more than once and base your discussion on the stable, larger-n results.

---

## 5. Input rules (validation)

| Where | Accepted |
|---|---|
| Fibonacci n | whole number 0 to 90 |
| Factorial n | whole number 0 to 20 (21! does not fit in a 64-bit number) |
| Stack / queue capacity | 1 to 12 |
| Stack / queue / list values | whole numbers from -999,999 to 999,999 |
| Sorting numbers | whole numbers from -1,000,000 to 1,000,000, at most 20,000 numbers |
| Random n | 1 to 20,000 |

---

## 6. The code explained

### Backend.java
| Part | Purpose |
|---|---|
| `Backend.fibonacci(method, n)` | Fibonacci: 0 = naive, 1 = memoised, 2 = bottom-up. Returns a `Run` (result, call count, time). |
| `Backend.factorial(recursive, n)` | Factorial, recursive or iterative. |
| `Backend.Hanoi` | Solves Tower of Hanoi recursively and records every move (disk, from, to). |
| `ArrayStack` | Stack on a fixed array. `top` is the index of the top item. |
| `LinearQueue` | `front` and `rear` only move forward, so freed slots are never reused (false full). |
| `CircularQueue` | `rear = (rear + 1) % capacity` wraps round. A `count` tells full from empty. |
| `SLinkedList`, `DLinkedList` | Lists built from our own node classes. The doubly list has `prev` references and a `tail` pointer, and a private `unlink` method that cuts a node out in O(1). |
| `checkBrackets` | Pushes opening brackets and pops on closing ones. |
| `Sorter` | The six sorting algorithms. Fields `comparisons` and `moves` count the operations. Setting `steps` records the array after each pass. |
| `Steps` | Stores copies of the array after each pass plus the range to highlight. |
| `parseInts`, `randomArray`, `sortedArray`, `reverseArray`, `isSorted`, `avgMillis` | Input parsing, test data and the timing helper used by Module D. |

**Quicksort partition (yellow / green):** the pivot is the first element. A pointer `green` marks the end of the zone of
values smaller than the pivot. The loop scans the remaining (yellow) values and grows the green zone each time it finds a
smaller value. At the end the pivot is swapped into the gap between green and yellow, so it is in its final place.
For the random-pivot version a random element is swapped to the front first.

### AlgorithmExplorer.java
| Part | Purpose |
|---|---|
| `main` | Opens the window with four tabs. |
| `RecursionTab` (`FibPanel`, `FactPanel`, `HanoiPanel`, `Board`) | Module A screens and the Hanoi drawing. |
| `DataStructuresTab` (`StackPanel`, `QueuePanel`, `ListPanel`, `BracketPanel`) | Module B screens. `StructPanel` is the shared layout and turns errors into messages. `ArrayView` and `ListView` draw the structures. |
| `SortingTab`, `SortView` | Module C screen and the bar drawing. |
| `AnalysisTab`, `LineChart` | Module D screen and the charts. |
| `readInt`, `bigStack` | Input checking, and a helper that runs sorts on a thread with a large stack. |

**Why `bigStack`?** Quicksort on sorted data calls itself once per element, so a long list needs a very deep call stack.
Running the sort on a thread with a big stack avoids a `StackOverflowError`.

**Timing:** the time is measured with `System.nanoTime()` around the sort. Recording the steps is done in a separate run,
so it does not slow down the timed run.

---

## 7. Complexity summary

| Algorithm | Best | Average | Worst | Extra space | Stable |
|---|---|---|---|---|---|
| Bubble sort (early exit) | O(n) | O(n^2) | O(n^2) | O(1) | Yes |
| Selection sort | O(n^2) | O(n^2) | O(n^2) | O(1) | No |
| Insertion sort | O(n) | O(n^2) | O(n^2) | O(1) | Yes |
| Merge sort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| Quicksort (first pivot) | O(n log n) | O(n log n) | O(n^2) on sorted input | O(log n) average | No |
| Quicksort (random pivot) | O(n log n) | O(n log n) | O(n^2), very unlikely | O(log n) expected | No |

## 8. First-principles rule

The algorithms and data structures do **not** use `Arrays.sort`, `Collections.sort`, `ArrayList`, `LinkedList`,
`Stack`, `Queue`, `Deque` or similar ready-made classes. The only library use is `java.util.Random` (random numbers),
`String.split` and `Long.parseLong` (reading the typed text), `System.nanoTime` (timing), threads, and Swing/AWT (the screens).

## 9. Troubleshooting

| Problem | Fix |
|---|---|
| `'javac' is not recognized` | Install the JDK, then open a **new** terminal so the PATH change applies. |
| `Could not find or load main class AlgorithmExplorer` | Run `javac Backend.java AlgorithmExplorer.java` first, from the folder with the files, then `java AlgorithmExplorer` (no `.java` or `.class`). |
| Errors about `Backend` not found | Compile both files together in one command. |
| Module D seems stuck | It is running. The label next to the button shows the current test. Wait for it to finish. |
| The window is small | Drag it larger. It is resizable. |


## 10. References

- Cormen, T. H., Leiserson, C. E., Rivest, R. L., and Stein, C. _Introduction to Algorithms_. MIT Press.
- DAA lecture notes.
- Oracle. _The Java Tutorials: Swing_. https://docs.oracle.com/javase/tutorial/uiswing/

## 11. Repository link




