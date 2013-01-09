package domain.commands

class Register_new_user {
    private final def newUserUUID
    private final def firstName
    private final def lastName
    private final def eMail

    Register_new_user(Map attrs) {
        this.newUserUUID = attrs.newUserUUID
        this.firstName = attrs.firstName
        this.lastName = attrs.lastName
        this.eMail = attrs.eMail
    }
}
