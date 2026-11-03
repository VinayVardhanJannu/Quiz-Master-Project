public class Question {

    private int id;
    private String question;
    private String[] options;
    private String correctAnswer;

    public Question(int id, String question,
                    String[] options, String correctAnswer) {

        this.id = id;
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public int getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}