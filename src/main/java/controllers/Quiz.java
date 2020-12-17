package controllers;

import org.glassfish.jersey.media.multipart.FormDataParam;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.Main;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Path("/quiz/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)

public class Quiz {
    @GET
    @Path("list")
    public String quizList() {
        System.out.println("Invoked quiz.quizList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT QuizID, Title, Description, Points FROM Quizzes WHERE Custom = 0");
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                JSONObject row = new JSONObject();
                row.put("QuizID", results.getInt(1));
                row.put("Title", results.getString(2));
                row.put("Description", results.getString(3));
                row.put("Points", results.getInt(4));
                response.add(row);
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";
        }
    }

    @GET
    @Path("customList")
    public String customQuizList() {
        System.out.println("Invoked quiz.customquizList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT QuizID, Title, Description, Points FROM Quizzes WHERE Custom = 1");
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                JSONObject row = new JSONObject();
                row.put("QuizID", results.getInt(1));
                row.put("Title", results.getString(2));
                row.put("Description", results.getString(3));
                row.put("Points", results.getInt(4));
                response.add(row);
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";
        }
    }


    @GET
    @Path("listQuestions/{QuizID}")
    public String questionList(@PathParam("QuizID") Integer QuizID) {
        System.out.println("Invoked Questions.QuestionList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT  Title, QuestionID FROM Questions WHERE QuizID = ?");
            ps.setInt(1, QuizID);
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                JSONObject row = new JSONObject();
                row.put("question", results.getString(1));
                int questionID = results.getInt(2);
                row.put("answers", getAnswers(QuizID, questionID));
                response.add(row);
            }
            return response.toString();
        } catch (
                Exception exception) {
            System.out.println("Database Error: " + exception.getMessage());
            return "{\"Error\": \"Unable to list items.  Error code xx.\"}";
        }
    }

    @GET
    @Path("listPoints/{QuizID}")
    public String scoreList(@PathParam("QuizID") String QuizID) {
        System.out.println("Invoked Quiz.listPoints(");
        JSONObject row = new JSONObject();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT Points FROM Quizzes WHERE QuizID = ?");
            ps.setString(1, QuizID);
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                row.put("Points", results.getInt(1));
            }
            return row.toString();

        } catch (
                Exception exception) {
            System.out.println("Database Error: " + exception.getMessage());
            return "Error";
        }
    }


    public JSONArray getAnswers(int quizID, int questionID) {
        System.out.println("Invoked Answers.AnswerList()");
        JSONArray response = new JSONArray();
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT Description, Correct FROM Answers WHERE QuizID = ? AND QuestionID = ?");
            ps.setInt(1, quizID);
            ps.setInt(2, questionID);
            ResultSet results = ps.executeQuery();
            while (results.next() == true) {
                JSONObject row = new JSONObject();
                row.put("text", results.getString(1));
                row.put("correct", results.getBoolean(2));
                response.add(row);
            }
            return response;

        } catch (
                Exception exception) {
            System.out.println("Database Error: " + exception.getMessage());
            return response ;
        }
    }



    @GET
    @Path("get/{QuizID}")
    public String getFood(@PathParam("QuizID") Integer QuizID) {
        System.out.println("Invoked Quizzes.getQuiz() with QuizID " + QuizID);
        try {
            PreparedStatement ps = Main.db.prepareStatement("SELECT QuizID, Title, Description, Points FROM Quizzes WHERE QuizID = ?");
            ps.setInt(1, QuizID);
            ResultSet results = ps.executeQuery();
            JSONObject response = new JSONObject();
            if (results.next() == true) {
                response.put("QuizID", QuizID);
                response.put("Title", results.getString(2));
                response.put("Description", results.getString(3));
                response.put("Points", results.getInt(4));
            }
            return response.toString();
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to get item, please see server console for more info.\"}";
        }

    }

    @POST
    @Path("add")
    public String quizAdd(@FormDataParam("QuizID") Integer QuizID, @FormDataParam("Title") String Title, @FormDataParam("Description") String Description, @FormDataParam("Points") Integer Points) {
        System.out.println("Invoked Quizzes.QuizAdd()");
        try {
            PreparedStatement ps = Main.db.prepareStatement("INSERT INTO Quizzes (Title, Description, Points ) VALUES ( ?, ?, ?)");
            ps.setString(1, Title);
            ps.setString(2, Description);
            ps.setInt(3, Points);
            ps.execute();
            return "{\"OK\": \"Added quiz.\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to create new item, please see server console for more info.\"}";
        }

    }


    @POST
    @Path("update")
    public String updateQuiz(@FormDataParam("QuizID") Integer QuizID, @FormDataParam("Title") String Title, @FormDataParam("Description") String Description, @FormDataParam("Points") Integer Points) {
        try {
            System.out.println("Invoked Quiz.updateQuiz/update id=" + QuizID);
            PreparedStatement ps = Main.db.prepareStatement("UPDATE Quizzes SET Title = ?, Description = ?, Points = ? WHERE QuizID = ?");
            ps.setString(1, Title);
            ps.setString(2, Description);
            ps.setInt(3, Points);
            ps.setInt(4, QuizID);
            ps.execute();
            return "{\"OK\": \"Quiz updated\"}";
        } catch (Exception exception) {
            System.out.println("Database error: " + exception.getMessage());
            return "{\"Error\": \"Unable to update item, please see server console for more info.\"}";
        }
    }
}