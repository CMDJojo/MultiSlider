import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * An advanced boards, which can have these items:
 * * Balls that can be moved in one direction
 * * Goals that has to be filled to win
 * * Stones that can be moved in one direction once, thereafter becoming a stationary wall
 * * Boxes that will be pushed to another blocking object when a ball pushes it
 * * Buttons that will turn a door to an open space or vice versa
 * * Walls that cannot be passed through or blocked
 * * Doors that can be opened or closed by a button
 *
 * There are blocking and non-blocking items: Blocking items (balls, stones, walls, boxes) activate the
 * non-blocking object they stand on (if any).
 *
 * What needs copying when changed:
 * * When a ball is moved, the balls[] and movables[] needs to be copied and replaced with the new value
 * * If a box is moved, the boxes[] needs to be copied and replaced with the new value
 * * If a stone is moved, the stone should be removed from movables[], stones[] should be copied and replaced with
 *   new value
 * * No 2d arrays need copying, walls won't change and cBoxes[] and cOthers[] will be cached from balls, stones, boxes
 *
 * Objects are designed so they avoid deep copying as much as possible. Doors reference their corresponding button,
 * and if a blocking object is on that button the door is opened. Stones has their position stored in stones, and if
 * a stone is moved (check "from" in "Move" against "movableStones") make a new movableStones from all
 * movableStonePoints except that one, and make a new movableStonePoints without that one, and deepcopy walls and add
 * the solid stone as wall.
 */
public class AdvancedBoard implements Board {
    private final int h;
    private final int w;
    private final boolean[][] walls;
    private final Point[][] buttonMappings; //if null, no wall, if not-null, the button which needs to be pressed
    private final Set<Point> balls; //all balls (that should be cached under cOthers)
    private final Set<Point> goals; //all goals (won't change)
    private final Set<Point> stones; //all stones (that should be cached under cOthers)
    private final Set<Point> boxes; //all boxes (that should be cached under cBoxes)
    private final List<Move> moves;
    private final Set<AdvancedBoard> exploredBoards;
    private final Set<Point> movables; //balls and stones that may move
    
    private AdvancedBoard(int h,
                          int w,
                          boolean[][] walls,
                          Point[][] buttonMappings,
                          Set<Point> movables,
                          Set<Point> balls,
                          Set<Point> goals,
                          Set<Point> stones,
                          Set<Point> boxes,
                          List<Move> moves,
                          Set<AdvancedBoard> exploredBoards) {
        this.h = h;
        this.w = w;
        this.walls = walls;
        this.buttonMappings = buttonMappings;
        this.movables = movables;
        this.balls = balls;
        this.goals = goals;
        this.stones = stones;
        this.boxes = boxes;
        this.moves = moves;
        this.exploredBoards = exploredBoards;
    }
    
    public AdvancedBoard(int h,
                         int w,
                         Set<Point> balls,
                         Set<Point> goals,
                         Set<Point> stones,
                         Set<Point> boxes,
                         Set<Door> doors,
                         Set<Point> walls) {
        this.h = h;
        this.w = w;
        this.balls = balls;
        this.goals = goals;
        this.stones = stones;
        this.boxes = boxes;
        
        this.walls = new boolean[h][w];
        for (Point wall : walls) {
            this.walls[wall.y()][wall.x()] = true;
        }
        
        this.buttonMappings = new Point[h][w];
        for (Door door : doors) {
            buttonMappings[door.door().y()][door.door().x()] = door.button();
        }
        
        movables = new HashSet<>(balls.size() + stones.size());
        movables.addAll(balls);
        movables.addAll(stones);
        
        moves = new ArrayList<>();
        exploredBoards = Collections.synchronizedSet(new HashSet<>());
    }
    
    @Override
    public List<Board> step() {
        List<Board> nextSteps = new ArrayList<>(movables.size() * Direction.values().length);
        for (Point movingObject : movables) {
            for (Direction dir : Direction.values()) {
                Move[] moves = toBound(movingObject, dir);
                if (moves[0].a().equals(moves[0].b())) continue;
                
                // a move has occurred: need to create new object and compare and possibly add to list
                Set<Point> nStones = stones,
                        nMovables = new HashSet<>(movables),
                        nBalls = balls,
                        nBoxes = boxes;
                List<Move> nMoves = new ArrayList<>(this.moves);
                nMoves.add(moves[0]);
                
                boolean isStone = stones.contains(moves[0].a());
                nMovables.remove(moves[0].a());
                if (isStone) {
                    nStones = new HashSet<>(stones);
                    nStones.remove(moves[0].a());
                    nStones.add(moves[0].b());
                } else {
                    nBalls = new HashSet<>(balls);
                    nBalls.remove(moves[0].a());
                    nBalls.add(moves[0].b());
                    nMovables.add(moves[0].b());
                }
                
                if (moves.length == 2 && !moves[1].a().equals(moves[1].b())) {
                    //box is moved
                    nBoxes = new HashSet<>(boxes);
                    nBoxes.remove(moves[1].a());
                    nBoxes.add(moves[1].b());
                }
                
                AdvancedBoard newBoard = new AdvancedBoard(h,
                        w,
                        walls,
                        buttonMappings,
                        nMovables,
                        nBalls,
                        goals,
                        nStones,
                        nBoxes,
                        nMoves,
                        exploredBoards
                );
                if (!exploredBoards.contains(newBoard)){
                    exploredBoards.add(newBoard);
                    nextSteps.add(newBoard);
                }
            }
        }
        return nextSteps;
    }
    
    @Override
    public boolean hasWon() {
        return balls.containsAll(goals);
    }
    
    @Override
    public Move[] getMoves() {
        return moves.toArray(Move[]::new);
    }
    
    /*
    If 1-length: Movable moved move[0]
    If 2-length: Movable moved move[0]
      AND box moved move[1]
     */
    private Move[] toBound(Point origin, Direction direction) {
        Point dest = moveItem(origin, direction);
        Point next = direction.apply(dest);
        if (boxes.contains(next)) {
            Point box = next;
            Point boxEnd = moveItem(box, direction);
            if (!boxEnd.equals(box)) {
                //box is moved, ball ends right before box
                Point ballEnd = direction.back(boxEnd);
                return new Move[]{
                        new Move(origin, ballEnd),
                        new Move(box, boxEnd)
                };
            }
        }
        return new Move[]{
                new Move(origin, dest)
        };
    }
    
    private Point moveItem(Point origin, Direction direction) {
        Point p = origin;
        while (true) {
            p = direction.apply(p);
            if (p.x() < 0 || p.x() >= w || p.y() < 0 || p.y() >= h) break;
            Point btn = buttonMappings[p.y()][p.x()];
            if (walls[p.y()][p.x()] ||
                    boxes.contains(p) ||
                    stones.contains(p) ||
                    balls.contains(p) ||
                    (btn != null && !(boxes.contains(btn) || //if it is a wall and nothing is on the button, cant move
                            stones.contains(btn) ||
                            balls.contains(btn))
                    )) break;
        }
        return direction.back(p);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AdvancedBoard that = (AdvancedBoard) o;
        
        if (!balls.equals(that.balls)) return false;
        if (!stones.equals(that.stones)) return false;
        if (!boxes.equals(that.boxes)) return false;
        return movables.equals(that.movables);
    }
    
    @Override
    public int hashCode() {
        int result = balls.hashCode();
        result = 31 * result + stones.hashCode();
        result = 31 * result + boxes.hashCode();
        result = 31 * result + movables.hashCode();
        return result;
    }
    
    public record Door(Point door, Point button) {
    }
}
