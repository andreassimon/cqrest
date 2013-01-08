package readmodels.eventhandlers

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate

class New_user_was_created_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'New user was created'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        try {
            final rowsAffected = jdbcTemplate.update(
                "INSERT INTO UserSummary (userId,  firstName, lastName, eMail) VALUES (?, ?, ?, ?);",
                    UUID.fromString(eventAttributes.newUserUUID),
                    eventAttributes.frstName,
                    eventAttributes.lastName,
                    eventAttributes.eMail
            );
            println "added $rowsAffected rows to UserSummary"
        } catch (DataAccessException ex) {
            println "Error executing insert to UserSummary"
            ex.printStackTrace()
        }
    }

}
