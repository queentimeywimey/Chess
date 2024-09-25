import java.awt.Color;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map.Entry;
import javalib.impworld.WorldScene;
import javalib.worldimages.AboveImage;
import javalib.worldimages.BesideImage;
import javalib.worldimages.CircleImage;
import javalib.worldimages.Posn;
import javalib.worldimages.RectangleImage;
import javalib.worldimages.TextImage;
import javalib.worldimages.WorldImage;

class Draw {
  static final int tileSize = 75;
  static final WorldImage black = new RectangleImage(tileSize, tileSize, "solid", Color.lightGray);
  static final WorldImage white = new RectangleImage(tileSize, tileSize, "solid", Color.white);
  static final WorldImage row1 = new BesideImage(white, black, white, black, white, black, white, black);
  static final WorldImage row2 = new BesideImage(black, white, black, white, black, white, black, white);
  static final WorldImage boardBase = new AboveImage(row1, row2, row1, row2, row1, row2, row1, row2);
  
  static final WorldImage selectImage = new CircleImage(Draw.tileSize/2 - 10, "outline", Color.GREEN);
  static final WorldImage checkImage = new CircleImage(Draw.tileSize/2 - 10, "outline", Color.RED);
  
  public static void checked(Posn king, WorldScene boardScene) {
    boardScene.placeImageXY(checkImage, Draw.transform(king.x), Draw.transform(king.y));
    
  }

  public static void selected(Optional<Posn> selected, WorldScene boardScene) {
    selected.ifPresent(p -> boardScene.placeImageXY(selectImage, Draw.transform(p.x), Draw.transform(p.y)));
    
  }

  public static void win(String msg, WorldScene toReturn) {
    toReturn.placeImageXY(new TextImage(msg, tileSize, Color.GREEN), 4*tileSize, 4*tileSize);
  }

  public static void drawActions(HashMap<Posn, Action> actions, WorldScene boardScene) {
    for (Entry<Posn, Action> pair : actions.entrySet()) {
      boardScene.placeImageXY(pair.getValue().getImage(), Draw.transform(pair.getKey().x), Draw.transform(pair.getKey().y));
    }
  }

  public static WorldScene newBoardScene() {
    WorldScene toReturn = new WorldScene(8*tileSize, 8*tileSize);
    toReturn.placeImageXY(boardBase, 4*tileSize, 4*tileSize);
    return toReturn;
  }

  public static void placePieces(HashMap<Posn, Piece> pieces, WorldScene boardScene) {
    for (Entry<Posn, Piece> pair : pieces.entrySet()) {
      boardScene.placeImageXY(pair.getValue().getImage(), Draw.transform(pair.getKey().x), Draw.transform(pair.getKey().y));
    }
  }
  
  // transforms board coordinates to scene coordinates
  static int transform(int x) {
    return x*tileSize + tileSize/2;
  }
}