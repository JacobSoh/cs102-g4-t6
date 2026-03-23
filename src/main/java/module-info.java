module edu.cs102.g04t06 {
    requires com.google.gson;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    
    exports edu.cs102.g04t06;
    // exports edu.cs102.g04t06.game.execution;
    // exports edu.cs102.g04t06.game.execution.ai;
    exports edu.cs102.g04t06.game.infrastructure.config;
    opens edu.cs102.g04t06.game.presentation.network to com.google.gson;
    opens edu.cs102.g04t06.game.rules to com.google.gson;
    opens edu.cs102.g04t06.game.rules.entities to com.google.gson;
    opens edu.cs102.g04t06.game.rules.valueobjects to com.google.gson;
    // exports edu.cs102.g04t06.game.infrastructure.data;
    // exports edu.cs102.g04t06.game.rules;
    // exports edu.cs102.g04t06.game.rules.entities;
    // exports edu.cs102.g04t06.game.rules.valueobjects;
}
