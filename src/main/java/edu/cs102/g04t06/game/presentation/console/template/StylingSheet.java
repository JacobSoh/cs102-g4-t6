package edu.cs102.g04t06.game.presentation.console.template;

public interface StylingSheet {
    String CARD_BG       = "#1e1e1e";
    String CARD_BORDER   = "#2a2a2a";
    String HIGHLIGHT_CLR = "#8b5cf6";   // purple
    String TEXT_COLOR    = "#e0e0e0";
    String BTN_BG        = "#f0f0f0";
    String BTN_TEXT      = "#1a1a1a";

    String BG_IMAGE_URL  = "/images/menuImg.jpeg";
    String GAME_BG_IMG_URL = "/images/gameImg.jpg";
    double BG_OPACITY    = 0.30;

    int  SLOT_COUNT      = 4;
    int  CARD_HEIGHT     = 110;
    int  CARD_RADIUS     = 12;
    int  CARD_SPACING    = 18;

    String TRANSPARENT_BG = "-fx-background: transparent;" + 
                            "-fx-background-color: transparent;" + 
                            "-fx-control-inner-background: transparent;";

    double PLAYER_COL_RATIO = 0.40;
    double MARKET_COL_RATIO = 0.60;
    double RESPONSIVE_BREAKPOINT_WIDTH = 1200;
}