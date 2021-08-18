enum Direction {
    RIGHT(1, 0),
    LEFT(-1, 0),
    UP(0, -1),
    DOWN(0, 1);
    private final int dx;
    private final int dy;
    
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }
    
    public Point apply(Point p) {
        return new Point(p.x() + dx, p.y() + dy);
    }
    
    public Point back(Point p) {
        return new Point(p.x() - dx, p.y() - dy);
    }
}
