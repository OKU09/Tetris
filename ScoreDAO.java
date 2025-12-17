import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ScoreDAO {
    // 接続設定
    private static final String URL = "jdbc:postgresql://localhost:5432/tetris_db";
    private static final String USER = "postgres";
    private static final String PASS = "oku09oku09"; // ★ここをご自身のパスワードに変更してください

    // スコアを保存する
    public void saveScore(String name, int score) {
        String sql = "INSERT INTO ranking (name, score) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setInt(2, score);
            pstmt.executeUpdate();
            System.out.println("Score saved!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ランキングトップ5を取得する
    public List<String> getTopRanking() {
        List<String> ranking = new ArrayList<>();
        String sql = "SELECT name, score FROM ranking ORDER BY score DESC LIMIT 5";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            int rank = 1;
            while (rs.next()) {
                String name = rs.getString("name");
                int score = rs.getInt("score");
                ranking.add(String.format("%d. %s : %d", rank++, name, score));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ranking.add("DB Error: " + e.getMessage());
        }
        return ranking;
    }
}