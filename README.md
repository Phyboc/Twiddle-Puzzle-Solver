# Twiddle Puzzle Solver (Java)

![Java](https://img.shields.io/badge/Language-Java-orange)
![Algorithms](https://img.shields.io/badge/Algorithms-BFS%20%7C%20A*%20%7C%20DP-blue)
![GUI](https://img.shields.io/badge/GUI-Java%20Swing-green)
![Project](https://img.shields.io/badge/Type-DAA%20Project-purple)

A **Java implementation of the Twiddle Puzzle**, an *N×N tile puzzle* where the only allowed move is **rotating a 2×2 sub-square**.

The project demonstrates and compares multiple **algorithmic approaches** for solving the puzzle and provides both:

* 🖥 **Command Line Interface (CLI)**
* 🎮 **Interactive Swing GUI**

This project was developed as part of the **Design and Analysis of Algorithms (DAA)** coursework.

---

# Table of Contents

* [Project Objective](#project-objective)
* [Puzzle Rules](#puzzle-rules)
* [Algorithms Implemented](#algorithms-implemented)
* [Algorithm Comparison](#algorithm-comparison)
* [Project Structure](#project-structure)
* [Requirements](#requirements)
* [How to Run (CLI)](#how-to-run-cli)
* [How to Run (GUI)](#how-to-run-gui)
* [GUI Preview](#gui-preview)
* [Notes on MDF DP Solver](#notes-on-mdf-dp-solver)
* [Contributing](#contributing)
* [License](#license)

---

# Project Objective

The goal of this project is to explore and compare **different algorithmic strategies** for solving the Twiddle Puzzle.

It focuses on:

* Understanding **search algorithms**
* Applying **dynamic programming**
* Exploring **divide and conquer strategies**
* Comparing **algorithm efficiency and behavior**

The project allows users to observe solver behavior through both **step-by-step CLI mode** and a **visual GUI interface**.

---

# Puzzle Rules

The Twiddle Puzzle consists of an **N × N board of numbered tiles**.

Example goal state for a 3×3 board:

```
1 2 3
4 5 6
7 8 9
```

### Allowed Move

A move selects a **2×2 sub-square** and rotates it **clockwise**.

Example:

```
1 2
4 5
```

becomes

```
4 1
5 2
```

### Move Indexing

For an **N × N board**, there are:

```
(N - 1) × (N - 1)
```

possible 2×2 rotations.

They are numbered in **row-major order**:

```
move = 1  → top-left 2×2
move = 2  → next 2×2 in top row
...
move = (N-1)*(N-1) → bottom-right 2×2
```

---

# Algorithms Implemented

The project includes several solver strategies:

### Search Algorithms

* **Breadth First Search (BFS)**
* **A* Search**

### Divide & Conquer Approaches

* **Spatial Divide & Conquer**
* **Cycle-based Divide & Conquer**
* **Depth-based Divide & Conquer**

### Dynamic Programming

* **MDF DP Solver**
  Precomputes reachable states from the goal.

### Backtracking

* **Iterative Deepening Backtracking**
* Uses **heuristic move ordering**

---

# Algorithm Comparison

| Algorithm    | Strategy             | Optimal | Performance                |
| ------------ | -------------------- | ------- | -------------------------- |
| BFS          | Breadth First Search | Yes     | Slow for large boards      |
| A*           | Heuristic Search     | Yes     | Faster than BFS            |
| Spatial D&C  | Divide & Conquer     | Depends | Moderate                   |
| Cycle D&C    | Divide & Conquer     | Depends | Moderate                   |
| Depth D&C    | Divide & Conquer     | Depends | Moderate                   |
| MDF DP       | Dynamic Programming  | Yes     | Very fast for small boards |
| Backtracking | Iterative Deepening  | Yes     | Slow but complete          |

---

# Project Structure

```
Twiddle_puzzle_solver
│
├── src
│   ├── game
│   │   ├── Main.java
│   │   ├── Board.java
│   │   ├── GameEngine.java
│   │   ├── Player.java
│   │   ├── ComputerPlayer*.java
│   │
│   └── gui
│       └── TwiddleGUI.java
│
├── images
│   └── gui.png
│
├── README.md
└── report.pdf
```

### Key Files

| File                   | Description                         |
| ---------------------- | ----------------------------------- |
| `Main.java`            | CLI entry point                     |
| `Board.java`           | Board representation and move logic |
| `ComputerPlayer*.java` | AI solver implementations           |
| `TwiddleGUI.java`      | Swing-based graphical interface     |

---

# Requirements

* **Java 8 or higher** (recommended: Java 11+)
* No external dependencies
* Uses **Java Standard Library + Swing**

---

# How to Run (CLI)

From the project root:

### Compile

```bash
javac -d out $(find src -name "*.java")
```

### Run

```bash
java -cp out game.Main
```

### CLI Menu

You will be prompted for:

1️⃣ Board size `N`

2️⃣ Mode

```
1 → Human vs Computer
2 → Computer Only
```

3️⃣ Solver Algorithm

```
1 BFS
2 A*
3 Spatial D&C
4 Cycle D&C
5 Depth D&C
6 MDF DP
7 Backtracking
```

---

# Computer Only Mode

In computer-only mode:

* Press **ENTER** to execute the next solver move
* Type **q** to quit

This mode allows you to **observe algorithm behavior step-by-step**.

---

# How to Run (GUI)

After compiling:

```bash
javac -d out $(find src -name "*.java")
java -cp out gui.TwiddleGUI
```

If the GUI does not include a `main()` method, you can:

* Launch it through **Eclipse / IntelliJ**
* Or create a small **GUI launcher class**

---

# GUI Preview

*(Add a screenshot of your GUI here)*

```
images/gui.png
```

Example:

```markdown
![Twiddle GUI](images/gui.png)
```

---

# Notes on MDF DP Solver

The **MDF DP solver** builds a **precomputed table of reachable states** starting from the goal configuration.

Advantages:

* Very fast lookup during gameplay

Limitations:

* Memory usage increases rapidly with board size

Recommended usage:

```
N ≤ 3
```

---

# Contributing

Contributions are welcome.

If you add a new solver:

1. Implement a new class extending the **Player abstraction**
2. Integrate it into:

   * `game.Main` (CLI)
   * `TwiddleGUI` (optional)
3. Ensure the solver works with the **Board API**

---

# License

This project was developed as part of a **Design and Analysis of Algorithms (DAA) academic course**.

You are free to study and modify the code for **educational purposes**.
