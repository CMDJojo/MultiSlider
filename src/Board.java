import java.util.List;

public interface Board {
    List<Board> step();
    
    boolean hasWon();
    
    Move[] getMoves();
    
}
