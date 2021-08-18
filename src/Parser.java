import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

public class Parser {
    public static void main(String... args) {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter board row by row (enter 'end' when finished):");
        System.out.println(" [Legend]");
        System.out.println(" x - wall");
        System.out.println(" s - stone");
        System.out.println(" b - ball");
        System.out.println(" g - goal");
        System.out.println(" o - box");
        System.out.println(" d - box and goal");
        System.out.println(" 1/! - first door/button");
        System.out.println(" 2/\" - second door/button");
        System.out.println(" 3/3 - third door/button");
        
        Set<Point> walls = new HashSet<>();
        Set<Point> balls = new HashSet<>();
        Set<Point> goals = new HashSet<>();
        Set<Point> boxes = new HashSet<>();
        Set<Point> stones = new HashSet<>();
        HashMap<Character, Point> buttonsMap = new HashMap<>();
        int y = 0;
        int w = 0;
        while (true) {
            String row = s.nextLine();
            if (row.equalsIgnoreCase("end")) break;
            w = Math.max(w, row.length());
            for (int x = 0; x < row.toCharArray().length; x++) {
                switch (row.charAt(x)) {
                    case 'x', 'X' -> walls.add(new Point(x, y));
                    case 'b', 'B' -> balls.add(new Point(x, y));
                    case 'g', 'G' -> goals.add(new Point(x, y));
                    case 's', 'S' -> stones.add(new Point(x, y));
                    case 'o', 'O' -> boxes.add(new Point(x, y));
                    case 'd', 'D' -> {
                        boxes.add(new Point(x, y));
                        goals.add(new Point(x, y));
                    }
                    case '1', '!',
                            '2', '"',
                            '3', '#' -> buttonsMap.put(row.charAt(x), new Point(x, y));
                    case ' ' -> {
                    }
                    default -> throw new IllegalArgumentException("Char '" + row.charAt(x) + "' unrecognized");
                }
            }
            y++;
        }
        
        Set<AdvancedBoard.Door> doors = new HashSet<>(buttonsMap.size() / 2);
        for (Character character : buttonsMap.keySet()) {
            switch (character) {
                case '1' -> doors.add(new AdvancedBoard.Door(buttonsMap.get('1'), buttonsMap.get('!')));
                case '2' -> doors.add(new AdvancedBoard.Door(buttonsMap.get('2'), buttonsMap.get('"')));
                case '3' -> doors.add(new AdvancedBoard.Door(buttonsMap.get('3'), buttonsMap.get('#')));
            }
        }
        
        Board board = new AdvancedBoard(y, w, balls, goals, stones, boxes, doors, walls);
        long time = System.currentTimeMillis();
        Optional<Board> optSolution = Solver.solve(board);
        time = System.currentTimeMillis() - time;
        
        if (optSolution.isPresent()) {
            Board solution = optSolution.get();
            for (int i = 0; i < solution.getMoves().length; i++) {
                Move move = solution.getMoves()[i];
                System.out.printf("#%d (%d,%d) %s -> (%d,%d)%n", i + 1,
                        move.a().x(), move.a().y(),
                        move.direction().name(),
                        move.b().x(), move.b().y());
    
                for (int yp = 0; yp <= y; yp++) {
                    for (int xp = 0; xp <= w; xp++) {
                        if (yp == move.a().y() && xp == move.a().x()) System.out.print('a');
                        else if (yp == move.b().y() && xp == move.b().x()) System.out.print('b');
                        else
                            System.out.print(walls.contains(new Point(xp, yp)) ? 'x' : ' ');
                    }
                    System.out.println();
                }
            }
            System.out.printf("Solution with %d moves found in %,d ms%n", solution.getMoves().length, time);
        } else {
            System.err.println("No solution found");
        }
    }
    
}
