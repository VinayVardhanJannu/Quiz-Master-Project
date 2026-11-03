import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QuizService {

    private QuestionService questionService;

    private List<String> resultHistory = new ArrayList<>();

    public QuizService(QuestionService questionService) {
        this.questionService = questionService;
    }

    public int calculateScore(Map<Integer, String> answers) {

        int score = 0;

        for (Question q : questionService.getAllQuestions()) {

            String userAnswer = answers.get(q.getId());

            if (userAnswer != null &&
                userAnswer.equalsIgnoreCase(q.getCorrectAnswer())) {

                score++;
            }
        }

        return score;
    }

    public int getTotalQuestions() {
        return questionService.getAllQuestions().size();
    }

    public double calculatePercentage(int score) {
        return (score * 100.0) / getTotalQuestions();
    }

    public String getFeedback(int score) {

        if (score >= 8) {
            return "Excellent! You have strong programming knowledge.";
        } else if (score >= 5) {
            return "Good job! Keep practising.";
        } else {
            return "Keep learning! Practice makes you better.";
        }
    }

    public void saveResult(int score) {

        resultHistory.add(
            "Score: " + score + "/" + getTotalQuestions()
        );
    }

    public List<String> getResultHistory() {
        return resultHistory;
    }
}