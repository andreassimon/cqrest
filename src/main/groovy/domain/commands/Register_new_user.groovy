package domain.commands

class Register_new_user {
    private final def newUserUUID
    private final def firstName
    private final def lastName
    private final def eMail

    Register_new_user(newUserUUID, firstName, lastName, eMail) {
        this.eMail = eMail
        this.lastName = lastName
        this.firstName = firstName
        this.newUserUUID = newUserUUID
    }
}
