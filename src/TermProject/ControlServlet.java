package TermProject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//------------------------------------------------------------------------------------------------------//
// [Project-Wide Notes]                                                                                 //
//      - Tables with foreign keys need to be declared AFTER the table they reference                   //
//      - The password for our Default Root user was changed to differentiate between:                  //
//          [Root: INITIALIZEDB.jsp] AND [Root: Local MySQL Instance Admin, Workbench]                  //
//      - Attributes defined as "NOT NULL" are critical to functionality (e.g. password in User table)  //
//      - Attributes that are dependent on an Animal existing will be auto-deleted once said animal     //
//          is removed (either deleted or adopted). These attributes can be identified in their         //
//          tables as they'll be defined with "ON DELETE CASCADE".                                      //
//      - Attributes that get their data from what a user types in (Reviews, species, etc) must         //
//          match the maxlength as set in the JSP files (e.g. In Reviews Table: "comments varchar(140)" //
//          and thus: In ReviewForm.jsp <textarea maxlength="140"> )                                    //
//      - The method closeAndDisconnectAll() is implemented to replace the methods                      //
//          resultSet.close() statement.close(), preparedStatement.close(), connect.close().            //
//          with intent to eliminate guess work as to which, and when, SQL items need to be closed,     //
//          as well as cleaning up the code.                                                            //
//          The limitation of this method however, is that any actions needed with any of               //
//          those objects needs to be carried out before closeAndDisconnectAll() is called.             //
//          A good example of this is UserDAO.validateUser().                                           //
//		- The Reviews table is a perfect example for [ON DELETE CASCADE] and [ON UPDATE CASCADE]		//
//			and when to use one, both, or neither														//
//                                                                                                      //
//   !! - The Built-in Objects (e.g. session, response, request, out) are accessed in TWO very          //
//          different ways dependent on where they're used. In servlets (e.g. ControlServlet.java)      //
//          they must be EXPLICITLY declared before use. In JSP's, they DO NOT NEED to be declared      //
//          and can be accessed just by referencing them like any other object.                         //
//                                                                                                      //
//------------------------------------------------------------------------------------------------------//
//                                                                                                      //
//      [Rough Format of ControlServlet Methods]                                                        //
//          (!) CRUD functions are to be handle by the DAO java files NOT ControlServlet                //
//          (1) Declare a local variable for holding info we want to output                             //
//          (2) Attach the variable from (1) to a new parameter in the request                          //
//          (3) Set the RequestDispatcher object equal to the target JSP                                //
//              (Object "dispatcher" is a field of ControlServlet.java)                                 //
//          (4) Forward the dispatcher with the newly-updated request object attached                   //
//                                                                                                      //
//      [Example]                                                                                       //
//           (1) List<Animal> allAnimals                                                                //
//           (2) request.setAttribute("listOfAnimals",allAnimals)                                       //
//           (3) dispatcher=request.getRequestDispatcher("PrintAllAnimals.jsp")                         //
//           (4) dispatcher.forward(request,response)                                                   //
//                                                                                                      //
//------------------------------------------------------------------------------------------------------//

/*                                        ~   ~   ~ | ~   ~   ~                                         */

//------------------------------------------------------------------------------------------------------//
// Notes:                                                                                               //
//      - ControlServlet does not handle parsing and storing of the animal traits. Instead, the single  //
//          String returned is sent to TraitDAO which handles parsing and adding the traits to table    //
//      - The output format of t handle parsing and storing of the animal traits. Instead, the single   //
//      - As per instructions, the info displayed after a trait search will be the items in Feature 3   //
//          ( Animal Name, Species, User who posted animal, and adoption price )                        //
//      - JSPs that ultimately output the same TYPE of info are reused. What is actually OUTPUT in      //
//          them is handled in this file (ControlServlet.java) via the DAO objects                      //
//          (e.g Listing all animals and listing a user's fav animals will both use AdoptionList.jsp,   //
//          but the List<Animals> list passed to JSP will have different contents: either ALL animals   //
//          or a User's fav)                                                                            //
//      - The boolean returns of CRUD methods (e.g. insertUser() ) are only being used for testing      //
//          and demo purposes currently)                                                                //
//      - Our Traits table is set up to associate animal traits to the animal's ID. However, animalID   //
//          is auto-incremented. Thus, we need a special SELECT statement to get the animal's ID from   //
//          the DB once it is added.																	//
// !! CRITICAL: Determine when to keep boolean method or split into separate JSP files (this seems likely)
//      - Because we are reusing out JSP list files, when we list a user's favorites 					//
//      	(animals or breeders) we will be setting a boolean value will be set to false				//
//      	to skipping over extra items not relevant to a user's list									//
//      	the appropriate outputs of the JSP. Will be set back to false once done.					//
//                                                                                                      //
//------------------------------------------------------------------------------------------------------//


public class ControlServlet extends HttpServlet {
//  private final long serialVersionUID = 1L;
    private HttpSession session = null;
    private RequestDispatcher dispatcher = null;

    private UserDAO userDAO;
    private AnimalDAO animalDAO;
    private TraitDAO traitDAO;
    private ReviewDAO reviewDAO;
    private FavAnimalDAO favAnimalDAO;
    private FavBreederDAO favBreederDAO;

    public void initializeAll(HttpServletResponse response)
            throws SQLException, IOException {


        userDAO         = new UserDAO();                                        // Initialize all the local DAO objects
        animalDAO       = new AnimalDAO();
        traitDAO        = new TraitDAO();
        reviewDAO       = new ReviewDAO();
        favAnimalDAO    = new FavAnimalDAO();
        favBreederDAO   = new FavBreederDAO();

        userDAO         .initializeTable();                                     // Explicitly initialize all Tables
        animalDAO       .initializeTable();
        traitDAO        .initializeTable();
        reviewDAO       .initializeTable();
        favAnimalDAO    .initializeTable();
        favBreederDAO   .initializeTable();

        userDAO.insert(new User("root", "123",
                "rootFName", "rootLName", "r@root.com"));                       // Add the root user to the Users Table

        response.sendRedirect("Login.jsp");                                     // Redirect to Login page once initialization is complete
    }


    //     ---------------------| doPost() Landing |---------------------     //

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    //     --------------------------| SWITCH |---------------------------    //

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getServletPath();                				// Intended destination

        try {
            switch (action) {
                case "/InitializeDB":
                    initializeAll(response);                                    // Initialize components
                    break;
                case "/CheckLogin":
                    checkLogin(request, response);                              // Verify login information
                    break;
                case "/LogoutUser":
                    // IN PROGRESS
                    logoutHelper(request, response);                            // Log out the current user
                    break;

                // !! Note: [ case "/new" ] replaced with a simpler and more secure method (I THINK more secure..)

                case "/UpdateUser":
                    // BASICALLY DONE (SEE COMMENTS AT updateUserForm() )
                    prepForInfoUpdate(request, response);                          	// Update User information
                    break;
                case "/SubmitUpdate":
                    // BASICALLY DONE (SEE COMMENTS AT updateUserForm() )
                    updateUser(request, response);                          	// Submit the updated info to tables
                    break;
                case "/InsertUser":
                    // TEST
                    insertUser(request, response);                              // Insert the user to the table
                    break;
                case "/delete":
                    deleteUser(request, response);                              // Delete a user
                    break;
                case "/ListBreeders":
                    // TEST
                    listAllBreeders(request, response);                         // Get a list of all breeders
                    break;
                case "/PostAnimal":
                    // TEST
                    checkNumberOfAnimalsPost(request, response);                // Check animals user posted is <5 (SUCCESS: Redirect to AnimalForm.jsp)
                    break;
                case "/SubmitNewAnimal":
                    // TEST
                    postAnimal(request, response);                              // Update relevant tables (i.e. Animals and Traits)
                    break;
                case "/ListAnimals":
                    // TEST
                    listAllAnimals(request, response);                          // Get a list of all animals
                    break;
                /*
                // !! CHECK: Can user be routed directly to SearchByTrait.jsp instead?
                case "/SearchForAnimalByTrait":
                    searchForAnimalByTrait(request, response);                  // Redirect to the search form
                    break;*/
                case "/ProcessAnimalTraitSearch":
                    // IMPLEMENT
                    processAnimalTraitSearch(request, response);                // Get a list of animals by trait
                    break;
                case "/ReviewAnimal":
                    // IMPLEMENT: add review to Review table
                    animalReviewFormHelper(request, response);                  // Attach User and Animal's IDs to request then load: ReviewForm.jsp
                    break;
                case "/SubmitReview":
                    // IMPLEMENT
                    submitReview(request, response);                            // Have user enter a review (Will Load: ReviewForm.jsp)
                    break;
                case "DeleteAnimal":
                    // IMPLEMENT
                    animalDeletionHelper(request, response);                    // Remove animal from adoption list (incl'd its traits and reviews)
                    break;
				case "AddToMyFavoriteAnimals":
					// IMPLEMENT
					addToMyFavAnimals(request, response);                          // "Add" animal to user's favorite list
					break;
                case "/ListMyFavoriteAnimals":
                    // TEST
                    showFavAnimalsList(request, response);                  	// List a user's favorite animals (Will load AdoptionList.jsp)
                    break;
                case "/ListFavoriteBreeders":
                    // TEST
                    showFavBreedersList(request, response);                 	// List a user's favorite breeders (Will load UsersList.jsp)
                    break;
                default:
                    response.sendRedirect("Login.jsp");                         // Default action: Login page
                    break;

            }// [ SWITCH ]

        } catch (SQLException ex) {
            throw new ServletException(ex);
        }

    }// END METHOD [ doGet() ]


    //     -------------------------| METHODS |---------------------------    //

    protected void checkLogin(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String username;
        String password;
        boolean loginSuccessful;
        User currentUser;

        username = request.getParameter("username");                            // Extract login info typed in by user (i.e. from Login.jsp)
        password = request.getParameter("password");
        loginSuccessful = userDAO.validateLoginAttempt(username, password);     // Validate credentials

        if (loginSuccessful) {
            currentUser = userDAO.getUser(username, password);                  // Retrieve User

            session = request.getSession();                                     // Record the current session

            session.setAttribute("sUsername", currentUser.getUsername());       // Session requires info retrieval thru objects
            session.setAttribute("sFirstName", currentUser.getFirstName());
            session.setAttribute("sLastName", currentUser.getLastName());
            session.setAttribute("sEmail", currentUser.getEmail());

            response.sendRedirect("index.jsp");                                 // Route to website homepage
        }
        else {
            response.sendRedirect("Login.jsp");                                 // Re-Route *back* to login page on failure
        }
    }


    // !! TODO: Attach logout functionality to a button or link somewhere on the web pages
    protected void logoutHelper(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        session = request.getSession();                                         // Load the session instance
        session.invalidate();                                                   // Unbind the object of the current user

        request.setAttribute("loggedOut", true);
        response.sendRedirect("Login.jsp");                                     // Return to login page (since no user logged in)
    }

    protected void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String username;
        String password;
        User existingUser;

        username = request.getParameter("username");                            // Username of User to be modified
        password = request.getParameter("password");                            // Password of User to be modified

        existingUser = userDAO.getUser(username, password);                     // Retrieve/Load the user (Type: User.java)

        request.setAttribute("user", existingUser);                             // Attach loaded User to request

        dispatcher = request.getRequestDispatcher("UsersForm.jsp");
        dispatcher.forward(request, response);
    }


    protected void listAllBreeders(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

        List<User> listBreeders;

        listBreeders = userDAO.listAllUsers();                                  // Build the list of all users currently in the DB
        request.setAttribute("listUsers", listBreeders);

        dispatcher = request.getRequestDispatcher("UsersList.jsp");
        dispatcher.forward(request, response);
    }


    // !! TODO: Make the JSP form check minimum length name, etc... (email is already done somehow..?)
    protected void insertUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String username;
        String password;
        String firstName;
        String lastName;
        String email;
        User newUser;
        boolean insertSuccessful;

        username = request.getParameter("username");                            // Extract data entered in by user (UsersForm.jsp)
        password = request.getParameter("password");
        firstName = request.getParameter("firstName");
        lastName = request.getParameter("lastName");
        email = request.getParameter("email");

        newUser = new User(username, password, firstName, lastName, email);     // Build the temp new User object

        insertSuccessful = userDAO.insert(newUser);                             // Add the new user to the Users table

        // TESTING CONFIRMATION
        /*System.out.print("INSERT User (" + username + "): ");
        if (insertSuccessful)
            System.out.println("SUCCESS");
        else
            System.out.println("FAILED");*/

        response.sendRedirect("Login.jsp");                                     // Return to login page for new user to login
    }


	protected void prepForInfoUpdate(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

    	String currentUsername;
		String currentPassword;
		String currentFirstName;
		String currentLastName;
		String currentEmail;
		User currentUser;

		currentUsername = (String) session.getAttribute("sUsername");           // Record user's current info
		currentPassword = request.getParameter("password");
		currentFirstName = request.getParameter("firstName");
		currentLastName = request.getParameter("lastName");
		currentEmail = request.getParameter("email");

		currentUser = new User(currentUsername, currentPassword, currentFirstName, currentLastName, currentEmail);

		request.setAttribute("currentUser", currentUser);                       // Attach the user's current info
		response.sendRedirect("UpdateUsersForm.jsp");                           // Forward to UpdateUsersForm.jsp
	}

	// Fix the issue of user blank inputs (look into HTML book and hidden(?)
    protected void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String currentUsername;
        String newUsername;
        String password;
        String firstName;
        String lastName;
        String email;
        User updatedUser;

		currentUsername = (String) session.getAttribute("sUsername");
        newUsername = request.getParameter("username");                         // Extract data entered in by user (UsersForm.jsp)
        password = request.getParameter("password");
        firstName = request.getParameter("firstName");
        lastName = request.getParameter("lastName");
        email = request.getParameter("email");

        updatedUser = new User(newUsername, password, firstName, lastName, email);     // Build the temp new User object

        userDAO.update(updatedUser, currentUsername);                           // Update the Users table
        response.sendRedirect("index.jsp");                                     // Route back to homepage
    }


    // !! NOT BEING USED CURRENTLY
    protected void deleteUser(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = Integer.parseInt(request.getParameter("id"));
        userDAO.delete("temp");
        response.sendRedirect("list");
    }// !!


    protected void checkNumberOfAnimalsPost(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String ownersUsername;
        boolean userReachedMaxPosts;

        System.out.println("CHECKING POST #");
        ownersUsername = (String) session.getAttribute("sUsername");            // Get the username of the current user
        userReachedMaxPosts = userDAO.maxAnimalsReached(ownersUsername);

        if (userReachedMaxPosts) {                                              // Query the Animals table (See: UserDAO.java)
            // IF TIME: Prompt to let user know max reached
			//PrintWriter out = new PrintWriter("You have 5 animals already posted. Delete one to post a new one.");
			System.out.println("MAX ANIMALS REACHED");
            response.sendRedirect("index.jsp");                                 // Maxed out, route back to homepage
        }
        else {
            response.sendRedirect("AnimalForm.jsp");                            // <5 posted animals, route to AnimalForm.jsp
        }
    }


    protected void postAnimal(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String name;
        String species;
        String birthDate;
        int adoptionPrice;
        String traitsRawData;
        String owner;
        Animal newAnimal;

        name = request.getParameter("name");                                    // Extract data entered (AnimalForm.jsp)
        species = request.getParameter("species");
        birthDate = request.getParameter("birthDate");
        adoptionPrice = Integer.parseInt(request.getParameter("adoptionPrice"));
        traitsRawData = request.getParameter("traits");
        owner = (String) session.getAttribute("sUsername");            			// Current user
                                                                                // (↓ Below): Build the temp Animal object to add
        newAnimal = new Animal(name, species, birthDate, adoptionPrice, owner);
        animalDAO.insert(newAnimal, traitsRawData);                             // Add the new animal to the Animals table

        response.sendRedirect("index.jsp");                                     // Return to home page
    }

    protected void openSearchForm(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        dispatcher = request.getRequestDispatcher("SearchByTrait.jsp");
        dispatcher.forward(request, response);
    }


    protected void listAllAnimals(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

        List<Animal> listAnimals;

        listAnimals = animalDAO.listAllAnimals();                               // Build the list of animals

        request.setAttribute("listAnimals", listAnimals);						// Attach the List to a new session attribute
		session.setAttribute("myAdoptions", false);                             // Ensure value of false (See: Notes)

		dispatcher = request.getRequestDispatcher("AdoptionList.jsp");
        dispatcher.forward(request, response);
    }


    protected void processAnimalTraitSearch(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

        String trait;
        List<Animal> animalsWithTrait;

        trait = request.getParameter("trait");
        animalsWithTrait = traitDAO.getAnimalsWithTrait(trait);              // Build the list of animals with the desired trait
        request.setAttribute("listAnimals", animalsWithTrait);                  // !! CRITICAL: Make sure we can put the "setAttribute" here

        dispatcher = request.getRequestDispatcher("AdoptionList.jsp");
        dispatcher.forward(request, response);
    }


    protected void animalReviewFormHelper(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        int animalID;
        String author;

        animalID = Integer.parseInt(request.getParameter("animalID"));          // Get the ID of the animal the user selected to review
		author = (String) session.getAttribute("sUsername");            		// Current user

        request.setAttribute("animalID", animalID);                             // Attach the animalID to request
        request.setAttribute("author", author);                             	// Attach the author

        dispatcher = request.getRequestDispatcher("ReviewForm.jsp");
        dispatcher.forward(request, response);
    }


    protected void submitReview(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int animalID;
        String authorsUsername;
        String rating;
        String comment;
        Review newReview;

        animalID = Integer.parseInt(request.getParameter("animalID"));          // animalID is set when the link was clicked from the animal list
        authorsUsername = (String) session.getAttribute("sUsername");           // Author is will be currently-logged-in user
        rating = request.getParameter("sFirstName");                            // Extract data entered (ReviewForm.jsp)
        comment = request.getParameter("sEmail");
        newReview = new Review(animalID, authorsUsername, rating, comment);     // Build temp Review object

        reviewDAO.insert(newReview);                                            // Add the new review

        response.sendRedirect("index.jsp");                                     // Return to home page
    }


    protected void animalDeletionHelper(HttpServletRequest request, HttpServletResponse response)
            throws SQLException {

        int animalID;

        animalID = Integer.parseInt(request.getParameter("animalID"));          // animalID is set when the link was clicked from the animal list
        animalDAO.delete(animalID);                                             // Delete the animal'

        /*
        // !! CRITICAL: Confirm "ON DELETE CASCADE" is functioning properly, otherwise below code is needed
        traitDAO.deleteTraitsByAnimal(animalID);                                // Delete the animal's traits
        reviewDAO.deleteReviewsByAnimal(animalID);                              // Delete the animal's reviews
        favAnimalDAO.delete(animalID);                                          // Remove the animal from any users' favorite list
        favBreederDAO.delete(animalID);                                         // Remove the animal from any users' favorite list */
    }

    protected void addToMyFavAnimals(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

    	int animalID;															// Animal who the user wants to favorite
		String usernameWhoFavd;                                                 // Person who fav'd is simply the current user

    	animalID = Integer.parseInt(request.getParameter("animalID"));	// !! CRITICAL: check animalID is correct
    	usernameWhoFavd = (String) session.getAttribute("sUsername");

    	favAnimalDAO.insert(animalID, usernameWhoFavd);
	}


    protected void showFavAnimalsList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String username;
        List<Animal> listFavAnimals;

        username = (String) session.getAttribute("sUsername");                  // Get current user's username
        listFavAnimals = favAnimalDAO.listAllFavAnimals(username);              // Get fav. animals for supplied username

        request.setAttribute("listAnimals", listFavAnimals);
		session.setAttribute("myAdoptions", true);								// See: Notes

        dispatcher = request.getRequestDispatcher("AdoptionList.jsp");
        dispatcher.forward(request, response);

        session.setAttribute("myAdoptions", false);								// Reset back to standard output
    }

    // CURRENT: Finish animal and users JSP lists
    protected void showFavBreedersList(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, ServletException, IOException {

        String username;
        List<User> listFavBreeders;

        username = request.getParameter("sUsername");
        listFavBreeders = favBreederDAO.listAllFavBreeders(username);           // Get all the fav. breeders for current user

        request.setAttribute("listUsers", listFavBreeders);
		session.setAttribute("myBreeders", true);                               // See: Notes

        dispatcher = request.getRequestDispatcher("UsersList.jsp");
        dispatcher.forward(request, response);

		session.setAttribute("myBreeders", false);                              // Reset back to standard output
    }

}// END CLASS [ ControlServlet ]













