package server;

public class Pair<X,Y> {
    
	X x;
    Y y;
    
    public Pair(X x, Y y)
    {
        this.x = x;
        this.y = y;
    }
    
    public X getX()
    {
    	return x;
    }
    
    public Y getY()
    {
    	return y;
    }
}