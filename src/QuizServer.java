import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class QuizServer {

    static QuestionService questionService =
        new QuestionService();

    static QuizService quizService =
        new QuizService(questionService);

    static void send(HttpExchange exchange, int status,
                     String response) throws IOException {

        byte[] data = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
            "Content-Type", "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, data.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(data);
        }
    }

    static String escape(String value) {
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r");
    }

    // API 1: Send questions without correct answers
    static void getQuestions(HttpExchange exchange)
            throws IOException {

        StringBuilder json = new StringBuilder("[");

        List<Question> questions =
            questionService.getAllQuestions();

        for (int i = 0; i < questions.size(); i++) {

            Question q = questions.get(i);

            json.append("{")
                .append("\"id\":").append(q.getId()).append(",")
                .append("\"question\":\"")
                .append(escape(q.getQuestion())).append("\",")
                .append("\"options\":[");

            String[] options = q.getOptions();

            for (int j = 0; j < options.length; j++) {

                json.append("\"")
                    .append(escape(options[j]))
                    .append("\"");

                if (j < options.length - 1) {
                    json.append(",");
                }
            }

            json.append("]}");

            if (i < questions.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        send(exchange, 200, json.toString());
    }

    // Convert submitted form data into a Java Map
    static Map<String, String> parseForm(String body)
            throws IOException {

        Map<String, String> data = new HashMap<>();

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                parts[0], "UTF-8"
            );

            String value = parts.length > 1
                ? URLDecoder.decode(parts[1], "UTF-8")
                : "";

            data.put(key, value);
        }

        return data;
    }

    // API 2: Submit answers and calculate result
    static void submitQuiz(HttpExchange exchange)
            throws IOException {

        String body = new String(
            exchange.getRequestBody().readAllBytes(),
            StandardCharsets.UTF_8
        );

        try {

            Map<String, String> form = parseForm(body);

            String answerData = form.get("answers");

            if (answerData == null || answerData.isEmpty()) {
                send(exchange, 400,
                    "{\"error\":\"Please submit your answers\"}");
                return;
            }

            Map<Integer, String> answers = new HashMap<>();

            for (String item : answerData.split(",")) {

                String[] parts = item.split(":", 2);

                if (parts.length != 2) {
                    throw new IllegalArgumentException();
                }

                int id = Integer.parseInt(parts[0]);
                String answer = parts[1].toUpperCase();

                Question q = questionService.getQuestionById(id);

                if (q == null ||
                    answers.containsKey(id) ||
                    !answer.matches("[A-D]")) {
                    throw new IllegalArgumentException();
                }

                answers.put(id, answer);
            }

            if (answers.size() != quizService.getTotalQuestions()) {
                send(exchange, 400,
                    "{\"error\":\"Submit all quiz answers\"}");
                return;
            }

            int score = quizService.calculateScore(answers);

            int total = quizService.getTotalQuestions();

            double percentage =
                quizService.calculatePercentage(score);

            String feedback = quizService.getFeedback(score);

            quizService.saveResult(score);

            String response =
                "{\"score\":" + score +
                ",\"total\":" + total +
                ",\"percentage\":" + percentage +
                ",\"feedback\":\"" + escape(feedback) + "\"}";

            send(exchange, 200, response);

        } catch (Exception e) {

            send(exchange, 400,
                "{\"error\":\"Invalid quiz submission\"}");
        }
    }

    // API 3: Display result history
    static void getHistory(HttpExchange exchange)
            throws IOException {

        StringBuilder json = new StringBuilder("[");

        List<String> history = quizService.getResultHistory();

        for (int i = 0; i < history.size(); i++) {

            json.append("\"")
                .append(escape(history.get(i)))
                .append("\"");

            if (i < history.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        send(exchange, 200, json.toString());
    }

    // Serve HTML, CSS and JavaScript files
    static void serveFrontend(HttpExchange exchange)
            throws IOException {

        String path = exchange.getRequestURI().getPath();

        if (path.equals("/")) {
            path = "/index.html";
        }

        String filename = path.substring(1);

        if (!filename.equals("index.html") &&
            !filename.equals("style.css") &&
            !filename.equals("script.js")) {

            byte[] error = "Not Found".getBytes(
                StandardCharsets.UTF_8
            );

            exchange.sendResponseHeaders(404, error.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error);
            }

            return;
        }

        File file = new File("web", filename);

        if (!file.isFile()) {

            byte[] error = "File Not Found".getBytes(
                StandardCharsets.UTF_8
            );

            exchange.sendResponseHeaders(404, error.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(error);
            }

            return;
        }

        String contentType;

        if (filename.endsWith(".css")) {
            contentType = "text/css; charset=UTF-8";
        } else if (filename.endsWith(".js")) {
            contentType = "application/javascript; charset=UTF-8";
        } else {
            contentType = "text/html; charset=UTF-8";
        }

        byte[] content = Files.readAllBytes(file.toPath());

        exchange.getResponseHeaders().set(
            "Content-Type", contentType
        );

        exchange.sendResponseHeaders(200, content.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(content);
        }
    }

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/api/questions", exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {
                getQuestions(exchange);
            } else {
                send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            }
        });

        server.createContext("/api/submit", exchange -> {

            if ("POST".equals(exchange.getRequestMethod())) {
                submitQuiz(exchange);
            } else {
                send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            }
        });

        server.createContext("/api/history", exchange -> {

            if ("GET".equals(exchange.getRequestMethod())) {
                getHistory(exchange);
            } else {
                send(exchange, 405,
                    "{\"error\":\"Method not allowed\"}");
            }
        });

        server.createContext("/", QuizServer::serveFrontend);

        server.setExecutor(null);

        server.start();

        System.out.println("=================================");
        System.out.println("JAVA QUIZ MASTER STARTED!");
        System.out.println("Open: http://localhost:8080");
        System.out.println("=================================");
    }
}