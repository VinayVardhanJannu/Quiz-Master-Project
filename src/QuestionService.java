import java.util.ArrayList;
import java.util.List;

public class QuestionService {

    private List<Question> questions = new ArrayList<>();

    public QuestionService() {
        loadQuestions();
    }

    private void loadQuestions() {

        questions.add(new Question(
            1,
            "Which concept allows a Java class to acquire properties of another class?",
            new String[]{
                "Encapsulation",
                "Inheritance",
                "Polymorphism",
                "Abstraction"
            },
            "B"
        ));

        questions.add(new Question(
            2,
            "Which keyword prevents a method from being overridden?",
            new String[]{
                "static",
                "private",
                "final",
                "abstract"
            },
            "C"
        ));

        questions.add(new Question(
            3,
            "Which collection does not allow duplicate elements?",
            new String[]{
                "ArrayList",
                "LinkedList",
                "HashSet",
                "Vector"
            },
            "C"
        ));

        questions.add(new Question(
            4,
            "What is the default value of an instance int variable in Java?",
            new String[]{
                "null",
                "0",
                "1",
                "Undefined"
            },
            "B"
        ));

        questions.add(new Question(
            5,
            "Which exception occurs when dividing an integer by zero?",
            new String[]{
                "IOException",
                "NullPointerException",
                "ArithmeticException",
                "ClassNotFoundException"
            },
            "C"
        ));

        questions.add(new Question(
            6,
            "Which Java feature allows the same method name with different parameters?",
            new String[]{
                "Method Overriding",
                "Method Overloading",
                "Inheritance",
                "Encapsulation"
            },
            "B"
        ));

        questions.add(new Question(
            7,
            "Which interface is the parent of all collection types in Java?",
            new String[]{
                "List",
                "Set",
                "Collection",
                "Map"
            },
            "C"
        ));

        questions.add(new Question(
            8,
            "Which keyword is used to implement an interface in Java?",
            new String[]{
                "extends",
                "implements",
                "interface",
                "import"
            },
            "B"
        ));

        questions.add(new Question(
            9,
            "Which of these is used to create a new thread in Java?",
            new String[]{
                "Thread class",
                "Scanner class",
                "String class",
                "System class"
            },
            "A"
        ));

        questions.add(new Question(
            10,
            "Which SQL command is used to retrieve data from a table?",
            new String[]{
                "INSERT",
                "UPDATE",
                "DELETE",
                "SELECT"
            },
            "D"
        ));
    }

    public List<Question> getAllQuestions() {
        return questions;
    }

    public Question getQuestionById(int id) {
        for (Question q : questions) {
            if (q.getId() == id) {
                return q;
            }
        }
        return null;
    }
}