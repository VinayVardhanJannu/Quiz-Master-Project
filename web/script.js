let questions = [];

let currentIndex = 0;

let answers = {};

let selectedOption = null;

let timerInterval;

let timeLeft = 20;

const startScreen = document.getElementById("start-screen");
const quizScreen = document.getElementById("quiz-screen");
const resultScreen = document.getElementById("result-screen");

const startBtn = document.getElementById("start-btn");
const nextBtn = document.getElementById("next-btn");
const restartBtn = document.getElementById("restart-btn");
const historyBtn = document.getElementById("history-btn");

// 1. Start quiz
startBtn.addEventListener("click", async function () {

    try {
        document.getElementById("error-message").textContent = "";

        const response = await fetch("/api/questions");

        if (!response.ok) {
            throw new Error("Unable to load questions");
        }

        questions = await response.json();

        if (questions.length === 0) {
            throw new Error("No questions available");
        }

        currentIndex = 0;
        answers = {};

        startScreen.classList.add("hidden");
        resultScreen.classList.add("hidden");
        quizScreen.classList.remove("hidden");

        showQuestion();

    } catch (error) {
        document.getElementById("error-message").textContent =
            error.message;
    }
});

// 2. Display question
function showQuestion() {

    clearInterval(timerInterval);

    selectedOption = null;
    timeLeft = 20;

    const question = questions[currentIndex];

    document.getElementById("question-number").textContent =
        `Question ${currentIndex + 1} of ${questions.length}`;

    document.getElementById("question-text").textContent =
        question.question;

    document.getElementById("timer").textContent =
        `${timeLeft} seconds`;

    document.getElementById("progress-bar").style.width =
        `${(currentIndex / questions.length) * 100}%`;

    const optionsContainer =
        document.getElementById("options-container");

    optionsContainer.innerHTML = "";

    const labels = ["A", "B", "C", "D"];

    question.options.forEach((option, index) => {

        const button = document.createElement("button");

        button.className = "option";

        button.textContent = `${labels[index]}. ${option}`;

        button.addEventListener("click", function () {

            selectedOption = labels[index];

            document.querySelectorAll(".option").forEach(btn => {
                btn.classList.remove("selected");
            });

            button.classList.add("selected");

            nextBtn.disabled = false;
        });

        optionsContainer.appendChild(button);
    });

    nextBtn.disabled = true;

    nextBtn.textContent =
        currentIndex === questions.length - 1
            ? "Submit Quiz"
            : "Next Question";

    startTimer();
}

// 3. Start countdown timer
function startTimer() {

    timerInterval = setInterval(function () {

        timeLeft--;

        document.getElementById("timer").textContent =
            `${timeLeft} seconds`;

        if (timeLeft <= 0) {

            clearInterval(timerInterval);

            answers[questions[currentIndex].id] =
                selectedOption || "-";

            moveNext();
        }

    }, 1000);
}

// 4. Next question
nextBtn.addEventListener("click", function () {

    if (selectedOption === null) {
        return;
    }

    answers[questions[currentIndex].id] = selectedOption;

    moveNext();
});

function moveNext() {

    clearInterval(timerInterval);

    currentIndex++;

    if (currentIndex < questions.length) {

        showQuestion();

    } else {

        submitQuiz();
    }
}

// 5. Submit answers to Java backend
async function submitQuiz() {

    quizScreen.classList.add("hidden");

    document.getElementById("error-message").textContent =
        "Calculating your result...";

    try {

        const answerData = Object.entries(answers)
            .map(([id, answer]) => `${id}:${answer}`)
            .join(",");

        const formData = new URLSearchParams({
            answers: answerData
        });

        const response = await fetch("/api/submit", {

            method: "POST",

            headers: {
                "Content-Type":
                    "application/x-www-form-urlencoded"
            },

            body: formData.toString()
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.error || "Submission failed");
        }

        document.getElementById("final-score").textContent =
            `${result.score}/${result.total}`;

        document.getElementById("percentage").textContent =
            `${result.percentage}%`;

        document.getElementById("feedback").textContent =
            result.feedback;

        document.getElementById("error-message").textContent = "";

        resultScreen.classList.remove("hidden");

        document.getElementById("progress-bar").style.width = "100%";

    } catch (error) {

        quizScreen.classList.remove("hidden");

        document.getElementById("error-message").textContent =
            error.message;
    }
}

// 6. Restart quiz
restartBtn.addEventListener("click", function () {

    clearInterval(timerInterval);

    resultScreen.classList.add("hidden");

    document.getElementById("history").innerHTML = "";

    startScreen.classList.remove("hidden");
});

// 7. View result history
historyBtn.addEventListener("click", async function () {

    try {

        const response = await fetch("/api/history");

        if (!response.ok) {
            throw new Error("Unable to load history");
        }

        const history = await response.json();

        const container = document.getElementById("history");

        container.innerHTML = "";

        const heading = document.createElement("h3");
        heading.textContent = "Previous Quiz Results";
        container.appendChild(heading);

        if (history.length === 0) {
            const message = document.createElement("p");
            message.textContent = "No previous results.";
            container.appendChild(message);
        }

        history.forEach(item => {
            const entry = document.createElement("p");
            entry.textContent = item;
            container.appendChild(entry);
        });

    } catch (error) {

        document.getElementById("history").textContent =
            error.message;
    }
});