import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

public class Solver {
    public static Optional<Board> solve(Board board) {
        return solve(board, 8);
    }
    
    public static Optional<Board> solve(Board board, int threads) {
        return solve(board, new ForkJoinPool(threads));
    }
    
    public static Optional<Board> solve(Board board, ForkJoinPool pool) {
        try {
            List<Board> boards = Collections.singletonList(board);
            int round = 0;
            while (true) {
                System.out.printf("Trying %d moves (%d tracked boards)\r", ++round, boards.size());
                final List<Board> b = boards;
                List<Board> newBoards = pool.submit(() -> b
                        .parallelStream()
                        .map(Board::step)
                        .flatMap(Collection::parallelStream)
                        .collect(Collectors.toList())).get();
                Optional<Board> winner = newBoards.stream().filter(Board::hasWon).findAny();
                if (newBoards.size() == 0) {
                    System.out.println("\nAll combinations exhausted; no solution found");
                    return Optional.empty();
                } else if (winner.isPresent()) {
                    System.out.println();
                    return winner;
                } else boards = newBoards;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static void solveOld(List<Board> boards, int round) {
        System.out.println("Exploring " + round + " moves");
        List<Board> next = new ArrayList<>();
        for (Board board : boards) {
            next.addAll(board.step());
        }
        for (Board board : next) {
            if (board.hasWon()) {
                System.out.println("Found solution!");
                Move[] moves = board.getMoves();
                for (int i = 0; i < moves.length; i++) {
                    Move move = moves[i];
                    System.out.printf("#%-2d (%2d,%2d) -> (%2d,%2d)%n", i + 1,
                            move.a().x(), move.a().y(),
                            move.b().x(), move.b().y());
                }
                return;
            }
        }
        solveOld(next, round + 1);
    }
}
