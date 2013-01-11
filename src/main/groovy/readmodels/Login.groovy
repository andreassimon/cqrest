package readmodels

class Login {

    private UUID userId
    private String eMail
    private String password


    Login() { }

    Login(UUID userId, String eMail, String password) {
        this.userId = userId
        this.eMail = eMail
        this.password = password
    }

    @Override
    String toString() {
        "Login{userId=${userId}, eMail=${eMail}, password=${password}}"
    }

    void setUserid(UUID userId) {
        this.userId = userId
    }

    void setEmail(String eMail) {
        this.eMail = eMail
    }

    void setPassword(String password) {
        this.password = password
    }

    @Override
    boolean equals(Object that) {
        if (this.class != that.class) {
            return false
        }

        this.userId == that.userId &&
        this.eMail == that.eMail &&
        this.password == that.password
    }
}
