const startButton = document.getElementById('start-btn')
const nextButton = document.getElementById('next-btn')
const EndButton = document.getElementById('end-btn')
let btn = document.getElementsByClassName("btn")
const questionContainerElement = document.getElementById('question-container')
startButton.addEventListener('click', startGame)
let score = 0;
let points = 0;
let limit = false;
const questionElement = document.getElementById('question')
const answerButtonsElement = document.getElementById('answer-buttons')

let shuffledQuestions, currentQuestionIndex

nextButton.addEventListener('click', () =>{
    currentQuestionIndex++
    limit = 0;
    setNextQuestion()
})

function startGame(){
    console.log('Started')
    startButton.classList.add('hide') //Hides the start button when clicked
    shuffledQuestions = questions.sort(() => Math.random() - .5)
    currentQuestionIndex = 0
    questionContainerElement.classList.remove('hide')
    setNextQuestion()
}

function setNextQuestion(){
    resetState()
    showQuestion(shuffledQuestions[currentQuestionIndex])
}

function showQuestion(question){
    questionElement.innerText = question.question
    question.answers.forEach(answer => {
        const button = document.createElement('button')
        button.innerText = answer.text
        button.classList.add('btn')
        if (answer.correct){
            button.dataset.correct = answer.correct
        }
        button.addEventListener('click', selectAnswer)
        answerButtonsElement.appendChild(button)
    })
}

function resetState(){
    clearStatusClass(document.body)
    nextButton.classList.add('hide')
    while(answerButtonsElement.firstChild){
        answerButtonsElement.removeChild
        (answerButtonsElement.firstChild)
    }
}


function selectAnswer(e){
    const selectedButton = e.target
    const correct = selectedButton.dataset.correct
    setStatusClass(document.body, correct)
    Array.from(answerButtonsElement.children).forEach(button =>{
        setStatusClass(button, button.dataset.correct)
    })
    if (!selectedButton.dataset.correct){
        limit = true;
        console.log(score);
    }
    if (selectedButton.dataset.correct && limit == false){
        score += 1;
        limit = true;
        console.log(score);
    }
    if (shuffledQuestions.length > currentQuestionIndex + 1) {
        nextButton.classList.remove('hide')
    } else{
        EndButton.classList.remove("hide")

    }

}

function endQuiz(){
    alert("You got:" + score/points * 100 + "%");
    let Username = sessionStorage.getItem("Username");
    const url = "User/updateAchievements/";

    fetch(url + Username, {
        method: "POST",
    }).then(response => {
        return response.json();
    }).then(response =>{
        if (response.hasOwnProperty("Error")){
            alert(JSON.stringify(response));
        } else{
            console.log(response);
        }
    })

    window.open("Home.html", "_self");
}

function setStatusClass(element, correct){
    clearStatusClass(element)
    if (correct){
        element.classList.add('correct')
    }   else {
        element.classList.add('wrong')
    }
}

function clearStatusClass(element){
    element.classList.remove('correct')
    element.classList.remove('wrong')
}

function pageLoad()
{
    getQuestionList();
    getPoints();
}


function getQuestionList(){
    console.log("Invoked getQuestionList()");

    let QuizID = sessionStorage.getItem("QuizID");
    const url = "/quiz/listQuestions/";

    fetch(url + QuizID, {
        method: "GET",
    }).then(response => {
        return response.json();
    }).then(response =>{
        if (response.hasOwnProperty("Error")){
            alert(JSON.stringify(response));
        } else{
            console.log(response);
        }
        questions = response;
    })
}

function getPoints(){
    console.log("Invoked getPoints()");

    let QuizID = sessionStorage.getItem("QuizID");
    const url = "/quiz/listPoints/";

    fetch(url + QuizID, {
        method: "GET",
    }).then(response => {
        return response.json();
    }).then(response =>{
        if (response.hasOwnProperty("Error")){
            alert(JSON.stringify(response));
        } else{
            console.log(response);
            //formatQuestionList(response);
        }
        points = response.Points;
    })
}


let questions = [
    {
        question: 'Who made "Shape of you"?',
        answers: [
            {text: 'Kanye West', correct: 0},
            {text: 'Ed Sheeran', correct: 1}
        ],
    },
    {
        question: 'How many days in a year"?',
        answers: [
            {text: '28', correct: false},
            {text: '365', correct: true}
        ]
    }
]


