package edu.cs102.g04t06.game.presentation.console_old.layout.market;

import edu.cs102.g04t06.game.presentation.console_old.template.BaseStack;
import edu.cs102.g04t06.game.presentation.console_old.template.StylingSheet;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ForegroundLayout extends BaseStack<VBox> implements StylingSheet {

    private final HBox wideLayout = new HBox();
    private final VBox narrowLayout = new VBox(12);

    public ForegroundLayout() {
        super(new VBox());
        setBaseBackground(GAME_BG_IMG_URL);
        setBasePadding();

        wideLayout.setAlignment(Pos.TOP_CENTER);
        wideLayout.setFillHeight(true);
        wideLayout.prefHeightProperty().bind(this.root.heightProperty());
        wideLayout.setMaxHeight(Double.MAX_VALUE);

        narrowLayout.setAlignment(Pos.TOP_CENTER);
        narrowLayout.setFillWidth(true);

    }

    public ScrollPane createForegroundLayout() {
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle(TRANSPARENT_BG);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        this.root.setFillWidth(true);
        this.root.prefWidthProperty().bind(scrollPane.widthProperty());
        this.root.minHeightProperty().bind(
                Bindings.createDoubleBinding(
                        () -> scrollPane.getViewportBounds().getHeight(),
                        scrollPane.viewportBoundsProperty()
                )
        );

        this.wideLayout.visibleProperty().bind(this.root.widthProperty().greaterThanOrEqualTo(RESPONSIVE_BREAKPOINT_WIDTH));
        this.wideLayout.managedProperty().bind(wideLayout.visibleProperty());
        this.narrowLayout.visibleProperty().bind(this.root.widthProperty().lessThan(RESPONSIVE_BREAKPOINT_WIDTH));
        this.narrowLayout.managedProperty().bind(narrowLayout.visibleProperty());

        addChildrens(wideLayout, narrowLayout);
        scrollPane.setContent(this.root);

        return scrollPane;
    }

    public void addWideLayoutChildrens(Node... childrens) {
        this.wideLayout.getChildren().addAll(childrens);
    }

    public void addNarrowLayoutChildrens(Node... childrens) {
        this.narrowLayout.getChildren().addAll(childrens);
    }

    public HBox getWideLayout() {
        return wideLayout;
    }

    public VBox getNarrowLayout() {
        return narrowLayout;
    }
}
