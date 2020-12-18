package controllers;

import server.Main;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;


@Path("user/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)


public class User {

    @POST
    @Path("login")
    public String loginUser(@FormDataParam("username") String username, @FormDataParam("password") String password){
        System.out.println("Invoked loginUser() on path user/login");
        try {
            PreparedStatement ps1 = Main.db.prepareStatement("SELECT Password, Admin FROM Users WHERE Username = ?");
            ps1.setString(1, username);
            ResultSet loginResults = ps1.executeQuery();
            if (loginResults.next() == true) {
                String correctPassword = loginResults.getString(1);
                int isAdmin = loginResults.getInt(2);

                int userID = getUserID(username, password);
                System.out.println("UserID is " + userID);
                if (userID == -1) {
                    return "{\"Error\": \"Username or password is incorrect.  Are you sure you've registered? \"}";
                }

                if (password.equals(correctPassword)) {
                    String uuid = UUID.randomUUID().toString();
                    String result = updateUUIDinDB(userID, uuid);
                    PreparedStatement ps2 = Main.db.prepareStatement("UPDATE Users SET UUID = ? WHERE Username = ?");
                    ps2.setString(1, uuid);
                    ps2.setString(2, username);
                    ps2.executeUpdate();
                    JSONObject userDetails = new JSONObject();
                    userDetails.put("username", username);
                    userDetails.put("token", uuid);
                    userDetails.put("isAdmin", isAdmin);
                    return userDetails.toString();
                }
                else {
                    return "{\"Error\": \"Incorrect password!\"}";
                }
            } else {
                return "{\"Error\": \"Username and password are incorrect.\"}";
            }
        } catch (Exception exception) {
            System.out.println("Database error during /user/login: " + exception.getMessage());
            return "{\"Error\": \"Server side error!\"}";
        }

    }

    public static int validateSessionCookie(Cookie sessionCookie) {     //returns the userID that of the record with the cookie value

        String uuid = sessionCookie.getValue();
        System.out.println("Invoked User.validateSessionCookie(), cookie value " + uuid);

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT UserID FROM Users WHERE UUID = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("userID is " + resultSet.getInt("UserID"));
            return resultSet.getInt("UserID");  //Retrieve by column name  (should really test we only get one result back!)
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;  //rogue value indicating error

        }
    }


    public static String updateUUIDinDB(int userID, String UUID) {

        System.out.println("Invoked User.updateUUIDinDB()");

        try {
            PreparedStatement statement = Main.db.prepareStatement(
                    "UPDATE Users SET UUID = ? WHERE UUID = ?"
            );
            statement.setString(1, UUID);
            statement.setInt(2, userID);
            statement.executeUpdate();
            return "OK";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "Error";
        }
    }


    public static int getUserID(String username, String password) {

        System.out.println("Invoked User.getUserID()");
        try {
            PreparedStatement statement = Main.db.prepareStatement("SELECT UserID FROM Users WHERE Username = ? AND Password = ?");
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            //ResultSet doesn't provide any isEmpty(), if next() return false it means ResultSet is empty
            if (resultSet.next()==false){
                return -1 ;
            } else {
                return resultSet.getInt("UserID");
            }



            //            Statement statement = DatabaseConnection.connection.createStatement();        //to test this make connection public in DBConnection class
//            String query = "SELECT UserID FROM Users WHERE Password = '"+ password+"'" ;
            //now user can enter      b' or '1'='1    evaluates to true so all records turned and they get logged in as the last result in resultsSet - ha ha ha
            //this won't work with prepared statement as all of       b' or '1'='1    is taken as the password
//            ResultSet resultSet = statement.executeQuery(query);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @POST
    @Path("add")
    public String UserAdd(@FormDataParam("username") String username , @FormDataParam("password") String password) {
        System.out.println("Invoked User.UserAdd()");
        try {
            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO Users (Username, Password) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.execute();
            return "{\"OK\": \"Added User.\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"UserName already taken\"}";
        }

    }


    @POST
    @Path("updateachievements/{Username}")
    public String updateQuiz(@PathParam("Username") String Username) {

        System.out.println("Invoked user.updateachievements()");

        try {
            System.out.println("Invoked User.updateAchievements");
            PreparedStatement ps = Main.db.prepareStatement("UPDATE Users SET Achievements = Achievements + 1 WHERE Username = ?");
            ps.setString(1, Username);
            ps.execute();
            return "{\"OK\": \"Acheivements updated\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }


    @GET
    @Path("name")
    public String userName(@CookieParam("sessionToken") Cookie sessionCookie) {

        System.out.println("Invoked User.userName()");

        if (sessionCookie == null) {
            return "Error: Something has gone wrong.  Please contact the administrator with the error code UC-UN";
        }

        try {
            String uuid = sessionCookie.getValue();
            PreparedStatement statement = Main.db.prepareStatement(
                    "SELECT Username FROM Users WHERE UUID = ?"
            );
            statement.setString(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.getString("FirstName");
        } catch (Exception e) {
            return "{\"Error\": \"Something has gone wrong.  Please contact the administrator with the error code UC-UN. \"}";
        }

    }

    @GET
    @Path("listAchievements/{Username}")
    public String achievementList(@PathParam("Username") String Username) {
        System.out.println("Invoked User.listAchievements(");
        JSONObject row = new JSONObject();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT Achievements FROM Users WHERE Username = ?");
            ps.setString(1, Username);
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                row.put("Achievements", results.getInt(1));
            }
            return row.toString();

        } catch (
                Exception exception) {
            System.out.println("Database Error: " + exception.getMessage());
            return "Error";
        }
    }

    @POST
    @Path("addScore/{QuizID}")
    public String scoreAdd(@FormDataParam("QuizID") Integer QuizID, @FormDataParam("UserID") Integer UserID, @FormDataParam("Score") Integer Score) {
        System.out.println("Invoked User.scoreAdd()");
        try {
            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO Scores (QuizID, UserID, Score ) VALUES ( ?, ?, ?)");
            ps.setInt(1, QuizID);
            ps.setInt(2, UserID);
            ps.setInt(3, Score);
            ps.execute();
            return "{\"OK\": \"Added quiz.\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to create new item, please see server console for more info.\"}";
        }

    }

    @GET
    @Path("listCustomAchievements/{Username}")
    public String CustomAchievementList(@PathParam("Username") String Username) {
        System.out.println("Invoked User.listCustomAchievements(");
        JSONObject row = new JSONObject();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT CustomAchievements FROM Users WHERE Username = ?");
            ps.setString(1, Username);
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                row.put("CustomAchievements", results.getInt(1));
            }
            return row.toString();

        } catch (
                Exception exception) {
            System.out.println("Database Error: " + exception.getMessage());
            return "Error";
        }
    }

}