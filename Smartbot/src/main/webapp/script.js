window.onload = function () {
    const input = document.getElementById("msg");

    input.addEventListener("keydown", function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            send();
        }
    });
};

function send() {
    let msg = document.getElementById("msg").value;

    if (msg.trim() === "") return;

    addMessage(msg, "user");

    fetch("chat", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: "msg=" + encodeURIComponent(msg)
    })
    .then(res => res.text())
    .then(data => {
        addMessage(data, "bot");
    });

    document.getElementById("msg").value = "";
}

function addMessage(text, type) {
    let box = document.getElementById("chat-box");

    let div = document.createElement("div");
    div.className = "message " + type;
    div.innerText = text;

    box.appendChild(div);
    box.scrollTop = box.scrollHeight;
}