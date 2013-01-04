package commands

import domain.commands.Register_new_user
import org.junit.Test

class RegisterNewUserTest extends CommandSideTest {
    @Test
    void should_create_a_new_user() {
        def newUserUUID = UUID.randomUUID()

        given {}
        when(new Register_new_user(newUserUUID, "Christian", "Remfert", "c.remfert@one-os.de"))

        then {
            New_user_was_created(newUserUUID, "Christian", "Remfert", "c.remfert@one-os.de")
        }
    }
}
