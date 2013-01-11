package commands

import domain.commands.Register_user
import org.junit.Test

class RegisterNewUserTest extends CommandSideTest {
    @Test
    void should_create_a_new_user() {
        def newUserUUID = UUID.randomUUID()

        given {}
        when(new Register_user(
                newUserUUID: newUserUUID,
                firstName: "Christian",
                lastName: "Remfert",
                eMail: "c.remfert@one-os.de",
                password: "optimus"
        ))

        then {
            User_was_registered(newUserUUID, "Christian", "Remfert", "c.remfert@one-os.de", 'optimus')
        }
    }
}
