import java.io.File;
import java.sql.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException {

        String password = FileReader.readFile("/F:/password.txt").get(0);

        Connection con = connect(password);
        Statement stmt = con.createStatement();

        String[] directories = getDirectories("/F:/Movies");

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

                searchForMovie(stmt, file.substring(0, file.length()-7));
                verifyMovie(stmt, file);
                System.out.println(file);

            }
        }

    }

    public static Connection connect(String password) {

        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://192.168.1.128:3306/media","desktop", password);
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

        String movieName = directoryName.substring(0, directoryName.length()-7);
        String movieYear = directoryName.substring(directoryName.length()-5, directoryName.length()-1);

        int movieCount = getMovieCountByName(stmt, movieName);

        ResultSet result = searchForMovie(stmt, movieName);

        if(movieCount == 0) {
            System.out.println("No movie found with name " + movieName );
        }

        if(movieCount == 1) {
            result.next();
            ResultSet movieMatch = searchForMovie(stmt, movieName);
            movieMatch.next();
            if(movieMatch.getString(3).substring(0,4).equals(movieYear)) {
                System.out.println("Perfect match found for " + movieName + " which released in " + movieYear);
            } else {
                System.out.println("Movie match found for " + movieName + ", however the file claims to be from " +
                        movieYear + " while the database has it as " + movieMatch.getString(3).substring(0,4));
            }

            System.out.println("One movie found named " + movieName);
        }

        if(movieCount > 1) {
            System.out.println(movieCount + " movies found with name " + movieName);
        }


        //if(searchForMovie(movieName))

        return true;


    }



    public static String[] getDirectories(String filepath) {

        File movieDir = new File(filepath);

        return movieDir.list((dir, name) -> new File(dir, name).isDirectory());
    }

}