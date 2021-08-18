# MultiSlider

This program solves the puzzle game Multislide from [Fancade](https://fancade.com/). The game consists of pieces which
can be slided in a direction until they collide with a wall. The goal is to slide all "balls" to all "goals", and there
are also switches that opens doors, boxes that balls can push, and stones that can be moved once only. This solver
respects all in-game physic laws, like that a ball can push one box but not two, and it manages to solve all levels
currently in the game. The solver always finds the solution with the least amount of moves. If the program reports that
no solution is found, it means that is has tried all possible moves and couldn't find a solution.

## Running

The program is written in Java and runs on Java 16 (or Java 15 with preview features enabled). Compile all sources
using `javac -d build src/*`, enter the directory with `cd build` and run with `java Parser`.

## Usage

Enter the puzzle, row by row, using these characters:

- x for wall
- s for stone
- b for ball
- g for goal
- o for box
- d for box and goal
- 1/! for first door/button
- 2/" for second door/button
- 3/3 for third door/button

1 and ! were chosen since they occupy the same button, as with 2 and ", and 3 and #. 'd' was added since in one level,
there were boxes placed on goals.

### Example

![Example](example.png)
This puzzle can be entered as

```
   xxxx
   x  x
 xxxobx
 x !  x
xxxxobx
xgg1  x
xxxxxxx
```

...and you end your input with "end". Walls are automatically added to each side, so you could have skipped entering the
first and last row, and the first and last column. Then, the program finds the solution and lists all the moves as
coordinate pairs, directions and a preview, such as:

```
#1 (5,2) DOWN -> (5,3)
   xxxx 
   x  x 
 xxx ax 
 x   bx 
xxxx  x 
x     x 
xxxxxxx 
```

That means that you move the piece at 'a' downwards, and it ends up at 'b'.

## Licenses

The code is released under an MIT license. The screenshot is from a minigame in the [Fancade](https://fancade.com) app
developed by Martin Magni, and the minigame [Multislide](https://fancade.page.link/YrN6) is made by LukaszM.
