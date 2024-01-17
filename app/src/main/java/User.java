import java.util.Currency;

public class User {
    public String emailAddress;
    String password;
    Currency income;

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Currency getIncome() {
        return income;
    }

    public void setIncome(Currency income) {
        this.income = income;
    }
}
