package readmodels

class UserSummary {
    UUID userId
    String firstName
    String lastName
    String eMail

    @Override
    String toString() {
        "UserSummary{userId=${userId}, firstName=${firstName}, lastName=${lastName}, eMail=${eMail}}"
    }

    UserSummary() { }

    UserSummary(UUID userId, String firstName, String lastName, String eMail) {
        this.userId = userId
        this.firstName = firstName
        this.lastName = lastName
        this.eMail = eMail
    }

    void setUserid(userId) {
        this.userId = userId
    }

    void setFirstname(firstName) {
        this.firstName = firstName
    }

    void setLastname(lastName) {
        this.lastName = lastName
    }

    void setEmail(email) {
        this.eMail = email
    }

    boolean equals(Object that) {
        this.userId == that.userId &&
        this.firstName == that.firstName &&
        this.lastName == that.lastName &&
        this.eMail == that.eMail
    }

}
