package domain.commands

class Register_user {
    private UUID newUserUUID
    private String firstName
    private String lastName
    private String eMail
    private String password

    Register_user(Map attrs) {
        final propertyNames = ['newUserUUID', 'firstName', 'lastName', 'eMail', 'password']
        copyProperties(attrs, this, propertyNames)
    }

    public static void copyProperties(Map map, Object command, Collection<String> propertyNames) {
        for (String propertyName : propertyNames) {
            assert map[propertyName] != null
            command[propertyName] = map[propertyName]
        }
    }
}
