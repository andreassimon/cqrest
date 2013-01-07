package readmodels.eventhandlers

import org.springframework.jdbc.core.JdbcTemplate

public interface EventHandler {
    String getEventName()
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes)
}



