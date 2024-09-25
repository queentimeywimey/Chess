import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javalib.worldimages.FromFileImage;
import javalib.worldimages.Posn;
import javalib.worldimages.WorldImage;

abstract class Piece{
  Posn coords; 
  BW color;
  WorldImage image;
  static final ArrayList<Posn> straights = new ArrayList<Posn>(Arrays.asList(
      new Posn(-1, 0),
      new Posn(1, 0),
      new Posn(0, -1),
      new Posn(0, 1)));
  static final ArrayList<Posn> diagonals = new ArrayList<Posn>(Arrays.asList(
      new Posn(-1, -1),
      new Posn(-1, 1),
      new Posn(1, -1),
      new Posn(1, 1)));
  static final ArrayList<Posn> combined = Glider.combinedGenerate();


  Piece(Posn coords, BW color){
    this.coords = coords;
    this.color = color;
  }

  WorldImage getImage() {
    return new FromFileImage("pieces/" + this.color.stringify() + this.stringify() + ".png");
  }

  Posn getCoords() {
    return this.coords;
  }

  public BW getColor() {
    return this.color;
  }

  abstract String stringify();

  void trimChecks(HashMap<Posn, Action> actions, HashMap<Posn, Piece> pieces){
    ArrayList<Posn> toRemove = new ArrayList<Posn>();
    
    for (Entry<Posn, Action> e : actions.entrySet()) {
      if (new Board(e.getValue().execute(pieces)).inCheck(this.color)) {
        toRemove.add(e.getKey());
      }
    }
    
    for (Posn p : toRemove) {
      actions.remove(p);
    }
  }

  abstract HashMap<Posn, Action> generateActions(HashMap<Posn, Piece> pieces);

  abstract HashMap<Posn, Action> generateCaptures(HashMap<Posn, Piece> pieces);

  public static Posn add(Posn p1, Posn p2) {
    return new Posn(p1.x + p2.x, p1.y + p2.y);
  }

  public static boolean outOfBounds(Posn p) {
    return !(p.x >= 0
        && p.x < 8
        && p.y >= 0
        && p.y < 8);
  }

  abstract ArrayList<Posn> generateVectors();

  static ArrayList<Posn> combinedGenerate() {
    ArrayList<Posn> toReturn = new ArrayList<Posn>();
    toReturn.addAll(straights);
    toReturn.addAll(diagonals);
    return toReturn;
  }

  public static Posn yMult(Posn p, int n) {
    return new Posn(p.x, p.y * n);
  }

  abstract Piece cloneDiffCoords(Posn newCoords);

  public boolean enPassantable() {
    return false;
  }

  public boolean rightKing(BW color) {
    return false;
  }

  public void unEnPassantify() {}

  public boolean isKing() {
    return false;
  }

  public boolean goodRook() {
    return false;
  }

  public boolean isPawn() {
    return false;
  }

  protected abstract int getValue();
}

abstract class Glider extends Piece{
  Glider(Posn coords, BW color){
    super(coords, color);
  }

  HashMap<Posn, Action> generateActions(HashMap<Posn, Piece> pieces){
    ArrayList<Posn> vectors = this.generateVectors();
    Set<Posn> occupied = pieces.keySet();
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();

    for (Posn vector : vectors){
      Posn possibleNewPosition = Piece.add(this.coords, vector);

      while (!Piece.outOfBounds(possibleNewPosition)) {
        if (occupied.contains(possibleNewPosition)) {
          if(!this.color.equals(pieces.get(possibleNewPosition).color)) {
            toReturn.put(possibleNewPosition, new Capture(possibleNewPosition, this));
          }
          break;
        } else {
          toReturn.put(possibleNewPosition, new Move(possibleNewPosition, this));
        }

        possibleNewPosition = Piece.add(possibleNewPosition, vector);
      }
    }

    this.trimChecks(toReturn, pieces);
    return toReturn;
  }

  HashMap<Posn, Action> generateCaptures(HashMap<Posn, Piece> pieces){
    ArrayList<Posn> vectors = this.generateVectors();
    Set<Posn> occupied = pieces.keySet();
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();

    for (Posn vector : vectors){
      Posn possibleNewPosition = Piece.add(this.coords, vector);

      while (!Piece.outOfBounds(possibleNewPosition)) {
        if (occupied.contains(possibleNewPosition)) {
          if(!this.color.equals(pieces.get(possibleNewPosition).color)) {
            toReturn.put(possibleNewPosition, new Capture(possibleNewPosition, this));
          }
          break;
        }
        possibleNewPosition = Piece.add(possibleNewPosition, vector);
      }
    }

    return toReturn;
  }
}

class Queen extends Glider{

  Queen(BW color){
    super(null, color);
    this.coords = color.equals(BW.BLACK) ? new Posn(3, 0) : new Posn(3, 7);
  }

  Queen(Posn coords, BW color){
    super(coords, color);
  }

  String stringify() {
    return "Queen";
  }

  ArrayList<Posn> generateVectors() {
    return Piece.combined;
  }

  Piece cloneDiffCoords(Posn newCoords) {
    return new Queen(newCoords, this.color);
  }

  protected int getValue() {
    return 9;
  }
}

class Bishop extends Glider{

  Bishop(BW color, RL side){
    super(null, color);
    int x = side.equals(RL.LEFT) ? 2 : 5;
    int y = color.equals(BW.BLACK) ? 0 : 7;
    this.coords = new Posn(x, y);
  }

  Bishop(Posn coords, BW color){
    super(coords, color);
  }

  String stringify() {
    return "Bishop";
  }

  ArrayList<Posn> generateVectors() {
    return Piece.diagonals;
  }

  Piece cloneDiffCoords(Posn newCoords) {
    return new Bishop(newCoords, this.color);
  }

  protected int getValue() {
    return 3;
  }
}

class Rook extends Glider{
  boolean moved = false;

  Rook(BW color, RL side){
    super(null, color);
    int x = side.equals(RL.LEFT) ? 0 : 7;
    int y = color.equals(BW.BLACK) ? 0 : 7;
    this.coords = new Posn(x,y);
  }

  Rook(Posn coords, BW color){
    super(coords, color);
  }

  String stringify() {
    return "Rook";
  }

  ArrayList<Posn> generateVectors() {
    return Piece.straights;
  }

  Piece cloneDiffCoords(Posn newCoords) {
    return new Rook(newCoords, this.color).movify();
  }
  
  Piece movify() {
    this.moved = true;
    return this;
  }
  
  public boolean goodRook() {
    return !this.moved;
  }

  protected int getValue() {
    return 5;
  }
}

abstract class Teleporter extends Piece{
  Teleporter(Posn coords, BW color){
    super(coords, color);
  }

  HashMap<Posn, Action> generateActions(HashMap<Posn, Piece> pieces) {
    ArrayList<Posn> vectors = this.generateVectors();
    Set<Posn> occupied = pieces.keySet();
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();

    for (Posn vector : vectors) {
      Posn current = Piece.add(this.coords, vector);
      if (Piece.outOfBounds(current)) {
      } else if (occupied.contains(current)) {
        if (!this.color.equals(pieces.get(current).color)) {
          toReturn.put(current, new Capture(current, this));
        }
      } else {
        toReturn.put(current, new Move(current, this));
      }
    }

    this.trimChecks(toReturn, pieces);
    return toReturn;
  }

  HashMap<Posn, Action> generateCaptures(HashMap<Posn, Piece> pieces){
    ArrayList<Posn> vectors = this.generateVectors();
    Set<Posn> occupied = pieces.keySet();
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();

    for (Posn vector : vectors) {
      Posn current = Piece.add(this.coords, vector);
      if (Piece.outOfBounds(current)) {
      } else if (occupied.contains(current)) {
        if (!this.color.equals(pieces.get(current).color)) {
          toReturn.put(current, new Capture(current, this));
        }
      }
    }

    return toReturn;
  }
}

class King extends Teleporter{
  boolean moved = false;
  static final RL[] sides = {RL.LEFT, RL.RIGHT};

  King(BW color){
    super(null, color);
    this.coords = color.equals(BW.BLACK) ? new Posn(4, 0) : new Posn(4, 7);
  }

  King(Posn coords, BW color){
    super(coords, color);
  }

  String stringify() {
    return "King";
  }
  
  HashMap<Posn, Action> generateActions(HashMap<Posn, Piece> pieces) {
    HashMap<Posn, Action> toReturn = super.generateActions(pieces);

    if (!this.moved) {
      for (RL side : sides) {
        int edgeX = side.edgeX();
        Posn maybeRook = new Posn(edgeX, this.coords.y);
        if (pieces.containsKey(maybeRook) && pieces.get(maybeRook).goodRook()){
          if (this.betweenEmpty(edgeX, pieces) && this.noCastleThroughCheck(side, pieces)) {
            toReturn.put(
                new Posn(side.kingToX(), this.coords.y), 
                new Castle(this, pieces.get(maybeRook), side));
          }
        }
      }
    }
    
    return toReturn;
  }

  private boolean noCastleThroughCheck(RL side, HashMap<Posn, Piece> pieces) {
    int a = Math.min(side.kingToX(), this.coords.x);
    int b = Math.max(side.kingToX(), this.coords.x);
    
    for (int x = a; x <= b; x++) {
      Move theoMove = new Move(new Posn(x, this.coords.y), this);
      Board theoBoard = new Board(theoMove.execute(pieces));
     if (theoBoard.inCheck(this.color)) {
       return false;
     }
    }
    return true;
  }

  private boolean betweenEmpty(int edgeX, HashMap<Posn, Piece> pieces) {
    int a = Math.min(edgeX, this.coords.x);
    int b = Math.max(edgeX, this.coords.x);
    
    for (int x = a + 1; x < b; x++) {
      if (pieces.containsKey(new Posn(x, this.coords.y))) {
        return false;
      }
    }
    
    return true;
  }

  ArrayList<Posn> generateVectors() {
    return Piece.combined;
  }

  Piece cloneDiffCoords(Posn newCoords) {
    return new King(newCoords, this.color).movify();
  }
  
  Piece movify() {
    this.moved = true;
    return this;
  }

  public boolean rightKing(BW color) {
    return this.color == color;
  }
  
  public boolean isKing() {
    return true;
  }

  protected int getValue() {
    return 0;
  }
}

class Knight extends Teleporter{
  static final ArrayList<Posn> l = new ArrayList<Posn>(Arrays.asList(
      new Posn(-2, -1),
      new Posn(-2, 1),
      new Posn(2, -1),
      new Posn(2, 1),
      new Posn(-1, -2),
      new Posn(-1, 2),
      new Posn(1, -2),
      new Posn(1, 2)));

  Knight(BW color, RL side){
    super(null, color);
    int x = side.equals(RL.LEFT) ? 1 : 6;
    int y = color.equals(BW.BLACK) ? 0 : 7;
    this.coords = new Posn(x, y);
  }

  Knight(Posn coords, BW color){
    super(coords, color);
  }

  String stringify() {
    return "Knight";
  }

  ArrayList<Posn> generateVectors() {
    return l;
  }

  Piece cloneDiffCoords(Posn newCoords) {
    return new Knight(newCoords, this.color);
  }

  protected int getValue() {
    return 3;
  }
}

class Pawn extends Piece{
  boolean enPassantable;
  boolean moved;
  static final Posn FORWARD = new Posn(0, 1);
  Posn forwards;
  List<Posn> forwardDiagonals = List.of(new Posn(-1, 1), new Posn(1, 1));
  static final List<Posn> sides = List.of(new Posn(1, 0), new Posn(-1, 0));

  Pawn(BW color, int x){
    super(null, color);
    this.coords = color.equals(BW.BLACK) ? new Posn(x, 1) : new Posn(x, 6);
    this.forwards = Piece.yMult(Pawn.FORWARD, this.color.numberify());
    this.forwardDiagonals = this.forwardDiagonals.stream()
        .map(p -> Piece.yMult(p, this.color.numberify()))
        .collect(Collectors.toList());
  }

  Pawn(Posn coords, BW color){
    super(coords, color);
    this.forwards = Piece.yMult(Pawn.FORWARD, this.color.numberify());
    this.forwardDiagonals = this.forwardDiagonals.stream()
        .map(p -> Piece.yMult(p, this.color.numberify()))
        .collect(Collectors.toList());
  }

  String stringify() {
    return "Pawn";
  }

  HashMap<Posn, Action> generateActions(HashMap<Posn, Piece> pieces) {
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();
    Set<Posn> occupied = pieces.keySet();

    ArrayList<Posn> moves = new ArrayList<Posn>();
    moves.add(Piece.add(this.coords, forwards));
    if (!moved) {
      moves.add(Piece.add(this.coords, Piece.yMult(forwards, 2)));
    }
    for (Posn move : moves) {
      if (!occupied.contains(move) && !Piece.outOfBounds(move)) {
        toReturn.put(move, new Move(move, this));
      } else {
        break;
      }
    }

    for (Posn diagonal : forwardDiagonals) {
      Posn capture = Piece.add(this.coords, diagonal);
      Posn side = Piece.add(capture, new Posn(0, -this.color.numberify()));
      if (!Piece.outOfBounds(capture)) {
        if (occupied.contains(capture)) {
          if (!this.color.equals(pieces.get(capture).color)) {
            toReturn.put(capture, new Capture(capture, this));
          }
        } else {
          if (occupied.contains(side) && !this.color.equals(pieces.get(side).color) && pieces.get(side).enPassantable()) {
            toReturn.put(capture, new EnPassant(capture, this));
          }
        }
      }
    }

    this.trimChecks(toReturn, pieces);
    return toReturn;
  }

  HashMap<Posn, Action> generateCaptures(HashMap<Posn, Piece> pieces) {
    HashMap<Posn, Action> toReturn = new HashMap<Posn, Action>();
    Set<Posn> occupied = pieces.keySet();

    for (Posn diagonal : forwardDiagonals) {
      Posn capture = Piece.add(this.coords, diagonal);
      Posn side = Piece.add(capture, new Posn(0, -this.color.numberify()));
      if (!Piece.outOfBounds(capture)) {
        if (occupied.contains(capture)) {
          if (!this.color.equals(pieces.get(capture).color)) {
            toReturn.put(capture, new Capture(capture, this));
          }
        } else {
          if (occupied.contains(side) && !this.color.equals(pieces.get(side).color) && pieces.get(side).enPassantable()) {
            toReturn.put(capture, new EnPassant(capture, this));
          }
        }
      }
    }

    return toReturn;
  }

  ArrayList<Posn> generateVectors() {
    throw new IllegalArgumentException("you didn't make this a thing, remember?");
  }

  Piece cloneDiffCoords(Posn newCoords) {
    Pawn toReturn = new Pawn(newCoords, this.color);
    if (Math.abs(this.coords.y - newCoords.y) == 2) {
      toReturn.enPassantify();
    }
    toReturn.movify();
    return toReturn;
  }

  private void movify() {
    this.moved = true;
  }

  private void enPassantify() {
    this.enPassantable = true;
  }

  public boolean enPassantable() {
    return this.enPassantable;
  }
  
  public void unEnPassantify() {
    this.enPassantable = false;
  }
  
  public boolean isPawn() {
    return true;
  }

  protected int getValue() {
    return 1;
  }
}

enum BW{
  BLACK, WHITE;

  String stringify() {
    if(this.equals(BLACK)) {
      return "black";
    } else {
      return "white";
    }
  }

  int numberify() {
    if (this.equals(BLACK)) {
      return 1;
    } else {
      return -1;
    }
  }
  
  int finalRow() {
    if (this.equals(BLACK)) {
      return 7;
    } else {
      return 0;
    }
  }

  BW opposite() {
    if (this.equals(BLACK)) {
      return WHITE;
    } else {
      return BLACK;
    }
  }
}

enum RL{
  RIGHT, LEFT;
  
  int kingToX() {
    if (this.equals(LEFT)) {
      return 2;
    } else {
      return 6;
    }
  }
  
  int rookToX() {
    if (this.equals(LEFT)) {
      return 3;
    } else {
      return 5;
    }
  }
  
  int edgeX() {
    if (this.equals(LEFT)) {
      return 0;
    } else {
      return 7;
    }
  }
}