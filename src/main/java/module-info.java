module edu.cs102.g04t06 {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    
    exports edu.cs102.g04t06;
    exports edu.cs102.g04t06.game.execution;
    exports edu.cs102.g04t06.game.execution.ai;
    exports edu.cs102.g04t06.game.infrastructure.config;
    exports edu.cs102.g04t06.game.infrastructure.data;
    exports edu.cs102.g04t06.game.presentation.console;
    exports edu.cs102.g04t06.game.rules;
    exports edu.cs102.g04t06.game.rules.entities;
    exports edu.cs102.g04t06.game.rules.valueobjects;
}
