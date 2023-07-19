import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;

import java.io.File;
import java.sql.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) throws SQLException {

        // System.out.println(specifyMovieID("FAkeMovie"));



        String movieDirectory = "/F:/Movies";

        String password = FileReader.readFile("/F:/password.txt").get(0);

        Connection con = connect(password);
        Statement stmt = con.createStatement();

        String[] directories = getDirectories(movieDirectory);

        verifyAllMovies(stmt, directories, "2 Fast 2 Furious (2003)");


        // Other stuff, pls ignore.
        ResultSet rs = stmt.executeQuery("SELECT * FROM Movies");

        rs.next();
        ResultSet hi =
        searchForMovie(stmt, "L'arrivée d'un train à La Ciotat");

        hi.next();

        System.out.println(hi.getInt(1));

        System.out.println(hi);

    }

    public static void verifyAllMovies(Statement stmt, String[] directories, String startFile) throws SQLException {

        boolean startFound = false;

        for (String file : directories) {

            if(!startFound) {

                startFound = file.equals(startFile);

            }

            if(startFound) {

                verifyMovie(stmt, file);
                System.out.println(file);

            }
        }

    }

    public static Connection connect(String password) {

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://192.168.1.128:3306/test_media","desktop", password);
            /*Statement stmt=con.createStatement();
            ResultSet rs=stmt.executeQuery("select * from Movies");
            while(rs.next())
                System.out.println(rs.getInt(1)+"  "+rs.getString(2)+"  "+rs.getString(3));
            con.close();*/
            return con;
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ResultSet searchForMovie(Statement stmt, String name) throws SQLException {
        return stmt.executeQuery("SELECT * FROM Movies WHERE Name = \"" + name + "\"" );
    }

    public static int getMovieCountByName(Statement stmt, String name) throws SQLException {
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Movies WHERE Name = \"" + name + "\"" );
        rs.next();
        return rs.getInt(1);
    }

    public static boolean verifyMovie(Statement stmt, String directoryName) throws SQLException {

        System.out.println();

        Pattern moviePattern = Pattern.compile(".+\\([0-9]{4}\\)");
        Matcher matcher = moviePattern.matcher(directoryName);

        if(!matcher.find()) {
            System.out.println("The directory " + directoryName + " does not match the expected naming convention");
            System.out.println("Would you like to rename it? (Y/N)");

            if(input().equalsIgnoreCase("Y")) {
                System.out.println("What would you like to change it to? (Press X to eXit)");
                String newName = input();
                if(!newName.equalsIgnoreCase("X")) {
                    File oldDirName = new File("/F:/Movies/" + directoryName);
                    File newDirName = new File("/F:/Movies/" + newName);
                    changeFileName(oldDirName, newDirName);
                }
            }

        } else {


            String movieName = directoryName.substring(0, directoryName.length()-7);
            String movieYear = directoryName.substring(directoryName.length()-5, directoryName.length()-1);

            // Get the number of movies in the database that match the name in the file.
            int movieCount = getMovieCountByName(stmt, movieName);

            ResultSet result = searchForMovie(stmt, movieName);

            // If no movie is found matching the directory name.
            if(movieCount == 0) {
                System.out.println("No movie found with name " + movieName );

                System.out.println("Would you like to change the name of the directory? (Y/N)");

                if(input().equalsIgnoreCase("Y")) {
                    System.out.println("What would you like to change it to?");
                    File oldName = new File("/F:/Movies/" + directoryName);
                    File newName = new File("/F:/Movies/" + input());
                    if(changeFileName(oldName, newName)) {
                        System.out.println("Directory name changed to " + newName);
                    } else {
                        System.out.println("Failed to rename directory");
                    }

                }

            }

            if(movieCount == 1) {
                result.next();
                ResultSet movieMatch = searchForMovie(stmt, movieName);
                movieMatch.next();

                if(movieMatch.getString(3).substring(0,4).equals(movieYear)) {
                    System.out.println("Perfect match found for " + movieName + " which released in " + movieYear);

                    // If the directory and database show two contradictory years.
                } else {
                    System.out.println("Database match found for " + movieName + ", however the file claims to be from " +
                            movieYear + " while the database has it as " + movieMatch.getString(3).substring(0,4));
                    System.out.println("Would you like to change the file name to match the database? (Y/N)");

                    // If the user wants to rename the directory to match the database.
                    if(input().equalsIgnoreCase("Y")) {
                        File oldName = new File("/F:/Movies/" + directoryName);
                        File newName = new File("/F:/Movies/" + movieName + " (" + movieMatch.getString(3).substring(0,4) + ")");
                        if(changeFileName(oldName, newName)) {
                            System.out.println("Directory name changed to " + newName);
                        } else {
                            System.out.println("Failed to rename directory");
                        }

                    } else {
                        System.out.println("Would you like to change the database entry? (Y/N)");

                        // If the user wants to update the database.
                        if(input().equalsIgnoreCase("Y")) {
                            if(changeMovieDate(stmt, movieMatch.getInt(1))) {
                                System.out.println("Database entry changed");
                            } else {
                                System.out.println("Failed to update database. You probably input the date wrong");
                            }

                        }
                    }

                }

            }

            if(movieCount > 1) {
                System.out.println(movieCount + " movies found with name " + movieName);
            }

        }

        //if(searchForMovie(movieName))

        return true;


    }

    public static boolean changeFileName(File oldName, File newName) {

        return oldName.renameTo(newName);

    }

    public static boolean changeMovieDate(Statement stmt, int id) throws SQLException {

        System.out.println();
        System.out.println("Changing movie release date");

        System.out.println("What date would you like to change it to? (Press X to eXit)");
        String date = input();

        if (date.equalsIgnoreCase("X")) {
            System.out.println("Aborting");
            return false;
        } else {
            try {
                return stmt.execute("UPDATE Movies SET release_date = '" + date + "' WHERE id = " + id);
            } catch (MysqlDataTruncation e) {
                return false;
            }
        }


    }

    public static String input() {
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    public static int specifyMovieID(String name) {
        Scanner scanner = new Scanner(System.in);
        boolean confirmed = false;
        int id = -1;

        while(!confirmed) {

            System.out.println("What ID does the movie " + name + " have in the database?");
            while(!scanner.hasNextInt()) {

                System.out.println("Please respond with an int.");
                System.out.println("What ID does the movie " + name + " have in the database?");
                scanner.next();

            }

            id = scanner.nextInt();

            System.out.println("Is the ID for the movie " + name + " " + id + "? (Y/N)");

            // For some godforsaken reason I couldn't inline this. So here's a redundant variable.
            String response = scanner.next().toUpperCase();

            if(response.equals("Y")) {
                confirmed = true;
            }

        }

        return id;

    }



    public static String[] getDirectories(String filepath) {

        File movieDir = new File(filepath);

        return movieDir.list((dir, name) -> new File(dir, name).isDirectory());
    }

}