package models;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import helpers.GameTimer;
import models.datastructures.DataScore;
import views.View;

/**
 * See klass tegeleb andmebaasi ühenduse ja "igasuguste" päringutega tabelitest.
 * Alguses on ainult ühenduse jaoks funktsionaalsus
 */
public class Database {
    /**
     * Algselt ühendust pole
     */
    private Connection connection = null;
    /**
     * Andmebaasi ühenduse string
     */
    private String databaseUrl;
    /**
     * Loodud mudel
     */
    private Model model;
    private View view;

    /**
     * Klassi andmebaas konstruktor
     * @param model loodud mudel
     */
    public Database(Model model) {
        this.model = model;
        this.databaseUrl = "jdbc:sqlite:" + model.getDatabaseFile();
        this.view = view;

        /**
         * meetod unikaalsete kategooriate saamiseks
         * */

        this.selectUniqueCategories();

    }

    /**
     * Loob andmebaasiga ühenduse
     * @return andmebaasi ühenduse
     */
    private Connection dbConnection() throws SQLException {
        // https://stackoverflow.com/questions/13891006/
        if(connection != null) {
            connection.close();
        }
        connection = DriverManager.getConnection(databaseUrl);
        return connection;
    }

    private void selectUniqueCategories() {
        String sql = "SELECT DISTINCT(category) as category FROM words ORDER BY category;";
        List<String> categories = new ArrayList<>();
        try{
            Connection connection = this.dbConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()) {
                String category = rs.getString("category");
                categories.add(category); // lisa kategooria listi
            }
            categories.add(0, model.getChooseCategory());
            String[] result = categories.toArray(new String[0]); // list<String> = string[]
            model.setCmbCategories(result); // seadista kategooriad mudelisse
            // System.out.println(categories.toString()); // test, kas kategooriad on olemas
            connection.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void selectScores() {
        String sql = "SELECT * FROM scores ORDER BY gametime, playertime DESC, playername;";
        List<DataScore> data = new ArrayList<>();
        try {
            Connection connection = this.dbConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            model.getDataScores().clear(); // tyhjenda mudeli listi sisu
            while (rs.next()) {
                String datetime = rs.getString("playertime");
                LocalDateTime playerTime = LocalDateTime.parse(datetime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                // System.out.println(datetime + " | " + playertime);
                String playerName = rs.getString("playername");
                String guessWord = rs.getString("guessword");
                String wrongChar = rs.getString("wrongcharacters");
                int timeSeconds = rs.getInt("gametime");
                data.add(new DataScore(playerTime, playerName, guessWord, wrongChar, timeSeconds));

            }
            model.setDataScores(data); // Muuda andmeid mudelis, kust saab info katte

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Päring juhuslik sõna andmebaasist kategooria alusel
    public void getWord(String chosenCategory) {
        // String sql = "SELECT * FROM words WHERE category = '" + chosenCategory + "';";
        String sql = "SELECT word FROM words WHERE category LIKE ? ORDER BY random() LIMIT 1;";
        // System.out.println("Valitud kategooria: " + chosenCategory);
        try {
            Connection connection = this.dbConnection();
            PreparedStatement randomWord = connection.prepareStatement(sql);
            if (chosenCategory.equals("Kõik kategooriad")){
                randomWord.setString(1,"%");
            } else {
                // valib kategooria hulgast juhusliku sõna
                randomWord.setString(1, "%" + chosenCategory + "%");
            }
                ResultSet rs = randomWord.executeQuery();
                String word = rs.getString("word");
                model.setRandomWord(word); // juhuslik sõna kirjutatakse modelisse
            // System.out.println("Juhuslik sõna DB klassist: " + word);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveScoreToDatabase(DataScore score) {
        // int gameTimer = view.getGameTimer();
        if (view == null) {
            System.err.println("Vaade on null Database.saveScoreToDatabase() meetodis");
            return;
        }

        String sql = "INSERT INTO scores (playertime, playername, guessword, wrongcharacters, gametime) VALUES (?,?,?,?,?)";

        try (Connection conn = this.dbConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // stmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            stmt.setString(2, model.getPlayerName());
            stmt.setString(3, model.getRandomWord());
            stmt.setString(4, model.getWrongGuesses()); // Assuming this method retrieves the wrong characters
            stmt.setInt(5, view.getGameTimer().getPlayedTimeInSeconds());

            int rowsInserted = stmt.executeUpdate(); // Execute the insert statement

            if (rowsInserted > 0) {
                System.out.println("A new score was inserted successfully.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting scores into database", e);
        }
    }

//    private int gametimer() {
//    }


//    private void getWords() {
//
//        String cmbCategories = model.getChooseCategory();
//        System.out.println(cmbCategories);
//
//        String sql = "SELECT word FROM words;";
//        List<String> words = new ArrayList<>();
//        try {
//            Connection connection = this.dbConnection();
//            Statement stmt = connection.createStatement();
//            ResultSet rs = stmt.executeQuery(sql);
//
//            while (rs.next()) {
//                words.add(rs.getString("word"));
//            }
//
//            model.getDatabaseWords(words);
//            System.out.println("sõnad andmebaasist" + words);
//            connection.close();
//
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
//    }

//    public void getWords(String category) {
//        List<String> words = new ArrayList<>();
//        String query;
//
//        try (Connection connection = dbConnection()) {
//            if (category.equalsIgnoreCase(model.getChooseCategory())) {
//                query = "SELECT word FROM words";
//            } else {
//                query = "SELECT word FROM words WHERE category = ?";
//            }
//
//            PreparedStatement stmt = connection.prepareStatement(query);
//            if (!category.equalsIgnoreCase(model.getChooseCategory())) {
//                stmt.setString(1, category);
//            }
//
//            ResultSet rs = stmt.executeQuery();
//
//            while (rs.next()) {
//                String word = rs.getString("word");
//                words.add(word);
//            }
//
//            // Print statements for debugging
//            String selectedCategory = model.getSelectedCategory();
//            System.out.println("Selected category: " + selectedCategory);
//            System.out.println("Words from database: " + words);
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return words;
//    }

}
