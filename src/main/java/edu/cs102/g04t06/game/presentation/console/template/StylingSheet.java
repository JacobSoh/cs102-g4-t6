package edu.cs102.g04t06.game.presentation.console.template;

public interface StylingSheet {
    String CARD_BG       = "#1e1e1e";
    String CARD_BORDER   = "#2a2a2a";
    String HIGHLIGHT_CLR = "#8b5cf6";   
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
    
    // for OnBoardingUI
    double BUTTON_GROUP_BOTTOM_OFFSET_RATIO = -0.20;
    String BUTTON_FONT_FAMILY = "'Georgia'";
    double BUTTON_GLOW_OPACITY = 0.35;
    double BUTTON_GLOW_RADIUS = 16;
    double BUTTON_HEIGHT = 38;
    double MODE_BUTTON_WIDTH = 140;
    double START_BUTTON_WIDTH = 140;
    String GOLD_TEXT = "#F5E6B3";
    String DARK_TEXT = "#1a0a00";
    String MARK_COLOR_BASE = GOLD_TEXT;
    String MARK_COLOR_HOVER = DARK_TEXT;
    String POPUP_MENU_STYLE =
        "-fx-background-color: linear-gradient(to bottom, rgba(36, 22, 12, 0.97), rgba(15, 9, 5, 0.97));" +
        "-fx-border-color: rgba(242, 210, 122, 0.85);" +
        "-fx-border-width: 1.6px;" +
        "-fx-border-radius: 10px;" +
        "-fx-background-radius: 10px;" +
        "-fx-padding: 2px;" +
        "-fx-background-insets: 0;";
    String TRANSPARENT_ROOT_STYLE =
        "-fx-background-color: transparent;" +
        "-fx-border-color: transparent;";
    String POPUP_STYLESHEET_CSS =
        ".menu-item { -fx-background-color: transparent !important; -fx-padding: 0; }" +
        ".menu-item:focused { -fx-background-color: transparent !important; }" +
        ".menu-item:hover { -fx-background-color: transparent !important; }" +
        ".context-menu { -fx-background-color: transparent; -fx-padding: 0; }";
    // for OnBoardingUI
}