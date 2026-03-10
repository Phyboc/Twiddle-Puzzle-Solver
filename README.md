# Twiddle Puzzle Solver (Java)

A Java implementation of the **Twiddle Puzzle** (an *N×N* board of numbered tiles) where the only allowed move is to **rotate a 2×2 sub-square**. The project includes:

- A **CLI** game runner (human vs computer, or computer-only step-by-step)
- A **Swing GUI** (`TwiddleGUI`)
- Multiple solver/AI strategies:
  - **BFS**
  - **A\***
  - **Divide & Conquer variants** (Spatial / Cycle / Depth)
  - **MDF DP** (DP precomputed table / policy for solvable states; best for small N)
  - **Backtracking** (iterative deepening with heuristic ordering)

## Rules / Move definition

A move selects a 2×2 block and rotates it (clockwise) by one step.

For an N×N board, there are `(N-1) * (N-1)` possible 2×2 positions, numbered in row-major order:

- `move = 1` => top-left 2×2
- `move = 2` => top row, next 2×2
- ...
- `move = (N-1)*(N-1)` => bottom-right 2×2

The goal state is the board in increasing order:

```
1  2  3
4  5  6
7  8  9
```

## Project structure

- `src/game/Main.java` — CLI entry point
- `src/gui/TwiddleGUI.java` — Swing GUI
- `src/game/Board.java` — board state + move execution
- `src/game/ComputerPlayer*.java` — AI implementations
- `DAA - Twiddle - Phase 2 Sivasubramani.pdf` — project/report document


## Requirements

- Java 8+ (recommended: Java 11+)
- No external dependencies (uses standard Java + Swing)

## How to run (CLI)

From the repo root:

```bash
# compile
javac -d out $(find src -name "*.java")

# run CLI
java -cp out game.Main
```

You will be prompted for:

1. Board size `N`
2. Mode:
   - `1` Human vs Computer
   - `2` Computer Only (step-by-step)
3. AI choice:
   1. BFS
   2. A*
   3. Spatial D&C
   4. Cycle D&C
   5. Depth D&C
   6. MDF DP
   7. Backtracking

### Computer-only mode

In computer-only mode, press **ENTER** to advance one solver move at a time (or type `q` to quit).

## How to run (GUI)

If your repo includes a GUI entry point (commonly a `main` method in `TwiddleGUI`, or another launcher), you can run it similarly after compiling:

```bash
javac -d out $(find src -name "*.java")
java -cp out gui.TwiddleGUI
```

If `TwiddleGUI` does not currently include a `public static void main(String[] args)` method, you can:
- launch it from your IDE (Eclipse project files are present: `.project`, `.classpath`), or
- add a small GUI launcher class.

## Notes on MDF DP mode

The DP-based solver builds a table of reachable states from the goal (reverse search). This can be memory-heavy as N grows; the CLI prints the number of states and warns if the DP table was truncated.

For best results, use **small boards (recommended N ≤ 3)**.

## Contributing

PRs/issues welcome. If you add a new solver:
- implement a `Player` / extend the existing player abstractions
- wire it into `game.Main` (CLI) and optionally the GUI dropdown.

## License

No license file is currently included. If you intend others to reuse this code, consider adding a `LICENSE` (e.g., MIT, Apache-2.0).
