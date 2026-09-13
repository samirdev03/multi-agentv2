package org.example.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTools {

    @Tool(description = "Returns the current date and time")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}