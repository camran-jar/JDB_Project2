/*
 * LibraryModel.java
 * Author: evanscame
 * Created on: 24/05/24
 * db password: hahaha
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class LibraryModel {
        private Connection con; 

        // For use in creating dialogs and making them modal
        private JFrame dialogParent;

    public LibraryModel(JFrame parent, String userid, String password) {
        dialogParent = parent;

        // password I set
        final String REQUIRED_PASSWORD = "hahaha";

        // Check if the password is correct, if incorrect, the program will terminate 
        if (!password.equals(REQUIRED_PASSWORD)) {
            throw new SecurityException("Invalid password. Program will not launch.");
        }

        // Establish connection
        String url = "jdbc:postgresql:" + "//db.ecs.vuw.ac.nz/" + userid + "_jdbc";
        try {
            this.con = DriverManager.getConnection(url, userid, password);
            System.out.println("Connected to PostgreSQL server");
        } catch (SQLException sqlex) {
            System.out.println("Can not connect: " + sqlex.getMessage());
        }
        }

        public String bookLookup(int isbn) {
            try{
                // SQL statement to get book details and authors, sorted by AuthSeqNo
                String sql = "SELECT b.Isbn, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft, a.Name, a.Surname " + 
                "FROM book b " +
                "JOIN book_author ba ON b.ISBN = ba.ISBN " +
                "JOIN author a ON ba.AuthorID = a.AuthorID " +
                "WHERE b.ISBN = ? " +
                "ORDER BY ba.authorseqno";

                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, isbn);

                // Execute the query
                ResultSet rs = ps.executeQuery();

                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = true;
                while(rs.next()){
                    if(found){
                        // show book information if there is one 
                        int ISBN = rs.getInt("ISBN");
                        String title = rs.getString("Title");
                        int edition = rs.getInt("Edition_No");
                        int numOfCop = rs.getInt("NumOfCop");
                        int numLeft = rs.getInt("NumLeft");
                        result.append("IBSN: ").append(ISBN).append("\nTitle: ").append(title).append("\nEdition:").append(edition)
                            .append("\nNumber of Copies: ").append(numOfCop).append("\nCopies Left: ").append(numLeft)
                            .append("\nAuthors: \n");
                        found = false; 
                    }
                    String name = rs.getString("Name").trim() + (" ") + rs.getString("Surname").trim();
                    result.append(name).append("\n");
                }
                if(found){
                    result.append("No book with ISBN ").append(isbn).append(" found in the database");
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving book information";
            }
        }     

        public String showCatalogue() {
            try {
                // SQL query to retrieve all books in the catalogue
                String sql = "SELECT b.ISBN, b.Title, b.edition_no, b.numofcop, b.numleft, a.Name, a.Surname " +
                                "FROM book b " +
                                "JOIN book_author ba ON b.ISBN = ba.ISBN " +
                                "JOIN author a ON ba.AuthorID = a.AuthorID " +
                                "ORDER BY b.Title, ba.authorseqno";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                String currTitle = null;
                boolean found = true;
        
                while(rs.next()){
                    String title = rs.getString("Title");
                    int isbn = rs.getInt("ISBN");
        
                    if(!title.equals(currTitle)){
                        if(!found){
                            result.append("\n");
                        }
                        // Show book information
                        int edition = rs.getInt("edition_no");
                        int numOfCop = rs.getInt("numofcop");
                        int numLeft = rs.getInt("numleft");
                        result.append("ISBN: ").append(isbn).append("\n")
                                .append("Title: ").append(title).append("\n")
                                .append("Edition: ").append(edition).append("\n")
                                .append("Number of Copies: ").append(numOfCop).append("\n")
                                .append("Copies Left: ").append(numLeft).append("\n")
                                .append("Authors:\n");
                        currTitle = title;
                        found = false;
                    }
        
                    // Append author information
                    String author = rs.getString("Name").trim() + " " + rs.getString("Surname").trim();
                    result.append(author).append("\n");
                }
        
                if(found){
                    result.append("No books found in the database");
                }
        
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving catalogue information";
            }
        }
        
        public String showLoanedBooks() {
            try {
                // SQL query to retrieve loaned books and borrower details
                String sql = "SELECT b.ISBN, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft, c.CustomerID, c.F_Name, c.L_Name, cb.DueDate " +
                             "FROM book b " +
                             "JOIN cust_book cb ON b.ISBN = cb.ISBN " +
                             "JOIN customer c ON cb.CustomerID = c.CustomerID " +
                             "ORDER BY b.Title";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = true;
        
                while (rs.next()) {
                    if (!found) {
                        result.append("\n");
                    }
        
                    // Get book information
                    int isbn = rs.getInt("ISBN");
                    String title = rs.getString("Title").trim();
                    int edition = rs.getInt("Edition_No");
                    int numOfCop = rs.getInt("NumOfCop");
                    int numLeft = rs.getInt("NumLeft");
        
                    // Get customer information
                    int customerId = rs.getInt("CustomerID");
                    String fName = rs.getString("F_Name").trim();
                    String lName = rs.getString("L_Name").trim();
                    Date dueDate = rs.getDate("DueDate");
        
                    // Append book and customer information to the result
                    result.append("ISBN: ").append(isbn).append("\n")
                          .append("Title: ").append(title).append("\n")
                          .append("Edition: ").append(edition).append("\n")
                          .append("Number of Copies: ").append(numOfCop).append("\n")
                          .append("Copies Left: ").append(numLeft).append("\n")
                          .append("Borrowed by: ").append(fName).append(" ").append(lName)
                          .append(" (Customer ID: ").append(customerId).append(")\n")
                          .append("Due Date: ").append(dueDate).append("\n");
                    found = false;
                }
                if (!found) {
                    result.append("No loaned books found in the database");
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving loaned books information";
            }
        }

        public String showAuthor(int authorID) {
            try {
                String sql = "SELECT * FROM author WHERE authorID = ?";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, authorID);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = false;
                while (rs.next()) {
                    String id = rs.getInt("AuthorID") + "";
                    String name = rs.getString("Name").trim();
                    String surname = rs.getString("Surname");
                    result.append("Author ID: ").append(id).append("\nName: ").append(name).append(" ").append(surname).append("\n");
                    found = true;
                }
                if (!found) {
                    return "Author with ID " + authorID + " not found.";
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving author information";
            }
        }
        
        public String showAllAuthors() {
            try{
                String sql = "SELECT * FROM author";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = false;
                while (rs.next()) {
                    String id = rs.getInt("AuthorID") + "";
                    String name = rs.getString("Name").trim();
                    String surname = rs.getString("Surname");
                    result.append("Author ID: ").append(id).append("\nName: ").append(name).append(" ").append(surname).append("\n\n");
                    found = true;
                }
                if (!found) {
                    return "No authors found in the database";
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving author information";
            
            }
        }

        public String showCustomer(int customerID) {
            try {
                String sql = "SELECT * FROM customer WHERE CustomerID = ?";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
                ps.setInt(1, customerID);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = false;
        
                while (rs.next()) {
                    String id = rs.getInt("CustomerID") + "";
                    String name = rs.getString("f_name").trim() + " " + rs.getString("l_name").trim();
                    String city = rs.getString("City");
                    result.append("Customer ID: ").append(id).append("\nName: ").append(name).append("\n").append(city).append("\n");
                    found = true;
                }
                if (!found) {
                    return "Customer with ID " + customerID + " not found.";
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error retrieving customer information";
            }
        }

        public String showAllCustomers() {
            try{
                String sql = "SELECT * FROM customer";
        
                // Prepare the statement
                PreparedStatement ps = con.prepareStatement(sql);
        
                // Execute the query
                ResultSet rs = ps.executeQuery();
        
                // Process the query
                StringBuilder result = new StringBuilder();
                boolean found = false;
                while (rs.next()) {
                    String id = rs.getInt("customerid") + "";
                    String name = rs.getString("f_name").trim() + " " + rs.getString("l_name").trim();
                    String city = rs.getString("City");
                    result.append("Customer ID: ").append(id).append("\nName: ").append(name).append("\n").append(city).append("\n\n");
                    found = true;
                }
                if (!found) {
                    return "No customers found in the database";
                }
                return result.toString();
            } catch (SQLException sqlex) {
                // Handle SQL exception
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error showing all customer information";
            }
        }

        public String borrowBook(int isbn, int customerID, int day, int month, int year) {
            try {
                // Begin transaction
                con.setAutoCommit(false);
        
                // Step 1: Check whether the customer exists and lock them
                String custCheckSQL = "SELECT * FROM customer WHERE customerID = ? FOR UPDATE";
                PreparedStatement custCheckPS = con.prepareStatement(custCheckSQL);
                custCheckPS.setInt(1, customerID);
                ResultSet custCheckRS = custCheckPS.executeQuery();
        
                if (!custCheckRS.next()) {
                    con.rollback();
                    return "Customer not found";
                }
        
                // Step 2: Lock the book record if it exists and is available
                String bookCheckSQL = "SELECT * FROM book WHERE ISBN = ? FOR UPDATE";
                PreparedStatement bookCheckPS = con.prepareStatement(bookCheckSQL);
                bookCheckPS.setInt(1, isbn);
                ResultSet bookCheckRS = bookCheckPS.executeQuery();
        
                if (!bookCheckRS.next()) {
                    con.rollback();
                    return "Book not found";
                }
        
                int numLeft = bookCheckRS.getInt("NumLeft");
                if (numLeft <= 0) {
                    con.rollback();
                    return "No copies of book with ISBN " + isbn + " left";
                }
        
                // Step 3: Insert the book into the customer record
                String borrowSQL = "INSERT INTO cust_book (ISBN, CustomerID, DueDate) VALUES (?, ?, ?)";
                PreparedStatement borrowPS = con.prepareStatement(borrowSQL);
                borrowPS.setInt(1, isbn);
                borrowPS.setInt(2, customerID);
                borrowPS.setDate(3, new java.sql.Date(new GregorianCalendar(year, month - 1, day).getTimeInMillis()));
                borrowPS.executeUpdate();
        
                // Interaction command to simulate contention
                int resp = JOptionPane.showConfirmDialog(dialogParent, "Confirm Borrow book?\n ISBN:" + isbn + " by customer ID: " + customerID, "Confirm Borrow", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) {
                    con.rollback();
                    return "Transaction cancelled";
                }
        
                // Step 4: Update the book record
                String updateSQL = "UPDATE book SET NumLeft = NumLeft - 1 WHERE ISBN = ?";
                PreparedStatement updatePS = con.prepareStatement(updateSQL);
                updatePS.setInt(1, isbn);
                updatePS.executeUpdate();
        
                // Step 5: Commit the transaction
                con.commit();
                return "Book with ISBN " + isbn + " borrowed by customer with ID " + customerID;
        
            } catch (SQLException sqlex) {
                try {
                    con.rollback();
                } catch (SQLException rollBackEx) {
                    System.out.println("Error rolling back transaction: " + rollBackEx.getMessage());
                }
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error borrowing book";
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    System.out.println("Error setting auto-commit to true: " + autoCommitEx.getMessage());
                }
            }
        }

        public String returnBook(int isbn, int customerid) {
            try{
               // begin transaction
                con.setAutoCommit(false);

                // check if customer exists, lock them 
                String custCheckSQL = "SELECT * FROM customer WHERE customerID = ? FOR UPDATE";
                PreparedStatement custCheckPS = con.prepareStatement(custCheckSQL);
                custCheckPS.setInt(1, customerid);
                ResultSet custCheckRS = custCheckPS.executeQuery();

                if (!custCheckRS.next()) {
                    con.rollback();
                    return "Customer not found";
                }

                // check if book exists, lock it
                String bookCheckSQL = "SELECT * FROM book WHERE ISBN = ? FOR UPDATE";
                PreparedStatement bookCheckPS = con.prepareStatement(bookCheckSQL);
                bookCheckPS.setInt(1, isbn);
                ResultSet bookCheckRS = bookCheckPS.executeQuery();

                if (!bookCheckRS.next()) {
                    con.rollback();
                    return "Book not found";
                }

                // check if book is borrowed by the customer
                String borrowCheckSQL = "SELECT * FROM cust_book WHERE ISBN = ? AND CustomerID = ?";
                PreparedStatement borrowCheckPS = con.prepareStatement(borrowCheckSQL);
                borrowCheckPS.setInt(1, isbn);
                borrowCheckPS.setInt(2, customerid);
                ResultSet borrowCheckRS = borrowCheckPS.executeQuery();

                if (!borrowCheckRS.next()) {
                    con.rollback();
                    return "Book with ISBN " + isbn + " not borrowed by customer ID: " + customerid;
                }
                
                // promt user to confirm return
                int resp = JOptionPane.showConfirmDialog(dialogParent, "Confirm Return book?\n ISBN:" + isbn + " by customer ID: " + customerid, "Confirm Return", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) {
                    con.rollback();
                    return "Transaction cancelled";
                }

                // delete the book from the customer record
                String returnSQL = "DELETE FROM cust_book WHERE ISBN = ? AND CustomerID = ?";
                PreparedStatement returnPS = con.prepareStatement(returnSQL);
                returnPS.setInt(1, isbn);
                returnPS.setInt(2, customerid);
                returnPS.executeUpdate();

                // update the book record
                String updateSQL = "UPDATE book SET NumLeft = NumLeft + 1 WHERE ISBN = ?";
                PreparedStatement updatePS = con.prepareStatement(updateSQL);
                updatePS.setInt(1, isbn);
                updatePS.executeUpdate();

                // commit the transaction
                con.commit();

                return "Book with ISBN " + isbn + " returned by customer with ID " + customerid;

            } catch (SQLException sqlex) {
                try {
                    con.rollback();
                } catch (SQLException rollBackEx) {
                    System.out.println("Error rolling back transaction: " + rollBackEx.getMessage());
                }
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error returning book";
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    System.out.println("Error setting auto-commit to true: " + autoCommitEx.getMessage());
                }
            }
        }

        public String deleteCus(int customerID) {
            return "Delete Customer";

        }

        public String deleteAuthor(int authorID) {
            return "Delete Author";
        }

        public String deleteBook(int isbn) {
            return "Delete Book";
        }

        public void closeDBConnection() {
            try {
                if (con != null) {
                    con.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException sqlex) {
                System.out.println("Error closing database connection: " + sqlex.getMessage());
            }
        }
    }

