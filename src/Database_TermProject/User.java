package Database_TermProject;

//---------------------------------------------------------------------------------------------------------//
// Note:                                                                                                   //
//    - Identifiers for animals are automatically generated by the auto-increment feature provided by SQL  //
//                                                                                                         //
//---------------------------------------------------------------------------------------------------------//

public class User {
    protected int id;
    protected String username;
    protected String password;
    protected String firstName;
    protected String lastName;
    protected String email;

    public User() {
    }

    public User(int id) {
        this.id = id;
    }

    public User(int id, String username, String password, String firstName, String lastName, String email) {
        this(username, password, firstName, lastName, email);
        this.id = id;
    }

    public User(String username, String password, String firstName, String lastName, String email) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }

}// END CLASS [ User ]