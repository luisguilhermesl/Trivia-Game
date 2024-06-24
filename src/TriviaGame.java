/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Luis
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class TriviaGame {
    private static final String URL = "jdbc:mysql://localhost:3306/trivia_game";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    private static int score = 0;
    private static boolean answered = false;
    
    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String playerName = scanner.nextLine();

            String playAgain;
            do {
                System.out.println("Escolha sua Categoria: Geografia, Matemática, Ciências, Literatura, Astronomia");
                String category = scanner.nextLine();

                // Get a random question from the database based on the chosen category
                String query = "SELECT id, perguntas, respostas FROM perguntas WHERE categoria = ? ORDER BY RAND() LIMIT 1";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, category);
                    ResultSet resultSet = statement.executeQuery();

                    if (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String question = resultSet.getString("perguntas");
                        String correctAnswer = resultSet.getString("respostas");

                        System.out.println(question);

                        Timer timer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                if (!answered) {
                                    System.out.println("\nO Tempo Acabou! A resposta correta é: " + correctAnswer);
                                    saveScore(connection, playerName, score);
                                    System.exit(0);
                                }
                            }
                        };
                        timer.schedule(task, 10000); // 10 seconds for each question

                        System.out.print("Your answer: ");
                        String playerAnswer = scanner.nextLine();
                        answered = true;
                        timer.cancel();

                        if (playerAnswer.equalsIgnoreCase(correctAnswer)) {
                            System.out.println("Correto!");
                            score++;
                        } else {
                            System.out.println("Incorreto. A resposta correta é: " + correctAnswer);
                        }
                    }
                }

                System.out.print("Do you want to play again? (yes/no): ");
                playAgain = scanner.nextLine();

            } while (playAgain.equalsIgnoreCase("yes"));

            saveScore(connection, playerName, score);
            System.out.println("Your final score is: " + score);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveScore(Connection connection, String playerName, int score) {
        String insertScoreQuery = "INSERT INTO scores (player_name, score) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertScoreQuery)) {
            statement.setString(1, playerName);
            statement.setInt(2, score);
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
