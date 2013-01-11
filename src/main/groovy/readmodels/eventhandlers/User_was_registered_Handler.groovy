package readmodels.eventhandlers

import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate

class User_was_registered_Handler implements readmodels.eventhandlers.EventHandler {

    @Override
    String getEventName() {
        'User was registered'
    }

    @Override
    void handleEvent(JdbcTemplate jdbcTemplate, eventName, eventAttributes) {
        try {
            int rowsAffected
            rowsAffected = jdbcTemplate.update(
                "INSERT INTO UserSummary (userId,  firstName, lastName, eMail, htmlTableRow) VALUES (?, ?, ?, ?, ?);",
                    UUID.fromString(eventAttributes.newUserUUID),
                    eventAttributes.firstName,
                    eventAttributes.lastName,
                    eventAttributes.eMail,
                    "<tr>" +
                        "<td>${eventAttributes.newUserUUID}</td>" +
                        "<td><a href=\"mailto:${eventAttributes.eMail}\">${eventAttributes.eMail}</a></td>" +
                        "<td>${eventAttributes.firstName}</td>" +
                        "<td>${eventAttributes.lastName}</td>" +
                    "</tr>"
            );
//            println "Added $rowsAffected ${rowsAffected == 1 ? 'row' : 'rows'} to UserSummary"
        } catch (DataAccessException ex) {
            println "Error executing insert to UserSummary"
            ex.printStackTrace()
        }
        try {
            rowsAffected = jdbcTemplate.update(
                "INSERT INTO Login (userId,  eMail, password) VALUES (?, ?, ?);",
                    UUID.fromString(eventAttributes.newUserUUID),
                    eventAttributes.eMail,
                    eventAttributes.password,
            );
//            println "Added $rowsAffected ${rowsAffected == 1 ? 'row' : 'rows'} to Login"
        } catch (DataAccessException ex) {
            println "Error executing insert to Login"
            ex.printStackTrace()
        }
    }

}
