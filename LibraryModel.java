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
                String sql = "SELECT b.Isbn, b.Title, b.Edition_No, b.NumOfCop, b.NumLeft, a.authorID, a.Name, a.Surname " + 
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
                    String authorID = "ID: " + rs.getString("authorID") + "";
                    String name = rs.getString("Name").trim() + (" ") + rs.getString("Surname").trim();
                    result.append(authorID).append(" - ").append(name).append("\n");
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
                String sql = "SELECT b.ISBN, b.Title, b.edition_no, b.numofcop, b.numleft, a.authorID, a.Name, a.Surname " +
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
                    String authorID = "ID: " + rs.getString("authorID") + "";
                    String name = rs.getString("Name").trim() + " " + rs.getString("Surname").trim();
                    result.append(authorID).append(" - ").append(name).append("\n");
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
        
                // check if customer exists, lock them 
                String custCheckSQL = "SELECT * FROM customer WHERE customerID = ? FOR UPDATE";
                PreparedStatement custCheckPS = con.prepareStatement(custCheckSQL);
                custCheckPS.setInt(1, customerID);
                ResultSet custCheckRS = custCheckPS.executeQuery();
        
                if (!custCheckRS.next()) {
                    con.rollback();
                    return "Customer not found";
                }
        
                // check if book exists and is available to lock it 
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
        
                // insert the book into the customers record
                String borrowSQL = "INSERT INTO cust_book (ISBN, CustomerID, DueDate) VALUES (?, ?, ?)";
                PreparedStatement borrowPS = con.prepareStatement(borrowSQL);
                borrowPS.setInt(1, isbn);
                borrowPS.setInt(2, customerID);
                borrowPS.setDate(3, new java.sql.Date(new GregorianCalendar(year, month - 1, day).getTimeInMillis()));
                borrowPS.executeUpdate();
        
                // Interaction command to simulate contention
                int response = JOptionPane.showConfirmDialog(dialogParent, "Confirm Borrow book?\n ISBN:" + isbn + " by customer ID: " + customerID, "Confirm Borrow", JOptionPane.YES_NO_OPTION);
                if (response != JOptionPane.YES_OPTION) {
                    con.rollback();
                    return "Transaction cancelled";
                }
        
                // update the book record 
                String updateSQL = "UPDATE book SET NumLeft = NumLeft - 1 WHERE ISBN = ?";
                PreparedStatement updatePS = con.prepareStatement(updateSQL);
                updatePS.setInt(1, isbn);
                updatePS.executeUpdate();
        
                // show some commitment and commit the transaction
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
                int response = JOptionPane.showConfirmDialog(dialogParent, "Confirm Return book?\n ISBN:" + isbn + " by customer ID: " + customerid, "Confirm Return", JOptionPane.YES_NO_OPTION);
                if (response != JOptionPane.YES_OPTION) {
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
           try{
            con.setAutoCommit(false);

            // Check if customer exists and lock 
            String custCheckSQL = "SELECT * FROM customer WHERE customerID = ? FOR UPDATE";
            PreparedStatement custCheckPS = con.prepareStatement(custCheckSQL);
            custCheckPS.setInt(1, customerID);
            ResultSet custCheckRS = custCheckPS.executeQuery();

            if(!custCheckRS.next()){
                con.rollback();
                return "Customer not found";
            }

            // Check if customer has any borrowed books
            String custCheckBook = "SELECT * FROM cust_book WHERE customerID = ?";
            PreparedStatement custCheckBookPS = con.prepareStatement(custCheckBook);
            custCheckBookPS.setInt(1, customerID);
            ResultSet custBookCheck = custCheckBookPS.executeQuery();

            if(!custBookCheck.next()){
                con.rollback();
                return "Customer with ID " + customerID + " has no borrowed books";
            }

            // Prompt user to confirm deletion
            int response = JOptionPane.showConfirmDialog(dialogParent, "Confirm Delete Customer?\n Customer ID: " + customerID, "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (response != JOptionPane.YES_OPTION) {
                con.rollback();
                return "Transaction cancelled";
            }

            // Delete customer from the table 
            String deleteSQL = "DELETE FROM customer WHERE customerID = ?";
            PreparedStatement deletePS = con.prepareStatement(deleteSQL);
            deletePS.setInt(1, customerID);
            deletePS.executeUpdate();

            con.commit();
            return "Customer with ID " + customerID + " deleted";

            } catch (SQLException sqlex) {
                try {
                    con.rollback();
                } catch (SQLException rollBackEx) {
                    System.out.println("Error rolling back transaction: " + rollBackEx.getMessage());
                }
                System.out.println("SQL Error: " + sqlex.getMessage());
                return "Error deleting customer";
            } finally {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException autoCommitEx) {
                    System.out.println("Error setting auto-commit to true: " + autoCommitEx.getMessage());
                }
            }
        }

        public String deleteAuthor(int authorID) {
            try{
                con.setAutoCommit(false);

                // check if author exists and lock it 
                String checkAuthorSQL = "SELECT * FROM author WHERE authorID = ? FOR UPDATE";
                PreparedStatement checkAuthorPS = con.prepareStatement(checkAuthorSQL);
                checkAuthorPS.setInt(1, authorID);
                ResultSet checkAuthorRS = checkAuthorPS.executeQuery();

                if(!checkAuthorRS.next()){
                    con.rollback();
                    return "Author not found";
                }

                // check if author has any books
                String checkBookSQL = "SELECT * FROM book_author WHERE authorID = ?";
                PreparedStatement checkBookPS = con.prepareStatement(checkBookSQL);
                checkBookPS.setInt(1, authorID);
                ResultSet checkBookRS = checkBookPS.executeQuery();

                if(checkBookRS.next()){
                    con.rollback();
                    return "Author with ID " + authorID + " has books in library"; 
                }

                // prompt user to confirm deltion
                int response = JOptionPane.showConfirmDialog(dialogParent, "Confirm delete Author? \n Author ID: " + 
                authorID, "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if(response != JOptionPane.YES_OPTION){
                    con.rollback();
                    return "Transaction cancelled";
                }

                // delete the author record
                String deleteSQL = "DELETE FROM author WHERE authorID = ?";
                PreparedStatement deleteAuthorPS = con.prepareStatement(deleteSQL);
                deleteAuthorPS.setInt(1, authorID);
                deleteAuthorPS.executeUpdate();

                con.commit();
                return "Author with ID: " + authorID + " has been deleted";

            } catch(SQLException sqlex){
                try {
                    con.rollback();
                } catch (SQLException rollBackEx) {
                    System.out.println("Error rolling back transaction: " + rollBackEx.getMessage());
                }
                System.out.println("SQL Error: " + sqlex.getMessage());
                return  "Error deleting author";
            } finally {
                try {
                   con.setAutoCommit(true); 
                } catch (SQLException autoCommitEx) {
                    System.out.println("error setting auto-commit to true: " + autoCommitEx.getMessage());
                }
            }
        }

        public String deleteBook(int isbn) {
            try {
                con.setAutoCommit(false);
                
                // check if Book exists and lock it 
                String checkBookSQL = "SELECT * from book WHERE isbn = ? FOR UPDATE";
                PreparedStatement checkBookPS = con.prepareStatement(checkBookSQL);
                checkBookPS.setInt(1, isbn);
                ResultSet bookCheckRS = checkBookPS.executeQuery();

                if(!bookCheckRS.next()){
                    con.rollback();
                    return "Book does not exist";
                }

                // Check if the book is borrowed
                String checkBorSQL = "SELECT * FROM book WHERE ISBN = ? AND numofcop = numleft";
                PreparedStatement checkBorrowedPS = con.prepareStatement(checkBorSQL);
                checkBorrowedPS.setInt(1, isbn);
                ResultSet borrowedCheckRS = checkBorrowedPS.executeQuery();

                if(!borrowedCheckRS.next()){
                    con.rollback();
                    return "Book with ISBN: " + isbn + " has been borrowed";
                }

                //Prompt user to confirm deletion
                int response = JOptionPane.showConfirmDialog(dialogParent, "Confirm Delete Book?\n Book ISBN: " + isbn, "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (response != JOptionPane.YES_OPTION) {
                    con.rollback();
                    return "Transaction cancelled";
                }

                // Delete the book from the table
                String deleteBookSQL = "DELETE FROM book WHERE isbn = ?";
                PreparedStatement deleteBookPS = con.prepareStatement(deleteBookSQL);
                deleteBookPS.setInt(1, isbn);
                deleteBookPS.executeUpdate();

                con.commit();
                return "Book with ISBN: " + isbn + " deleted from the library";
            } catch(SQLException sqlex){
                try {
                    con.rollback();
                } catch (SQLException rollBackEx) {
                    System.out.println("Error rolling back transaction: " + rollBackEx.getMessage());
                }
                System.out.println("SQL Error: " + sqlex.getMessage());
                return  "Error deleting book";
            } finally {
                try {
                   con.setAutoCommit(true); 
                } catch (SQLException autoCommitEx) {
                    System.out.println("error setting auto-commit to true: " + autoCommitEx.getMessage());
                }
            }

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

