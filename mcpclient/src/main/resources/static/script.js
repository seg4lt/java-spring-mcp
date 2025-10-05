document.addEventListener("DOMContentLoaded", () => {
  document.getElementById("chat-input").addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
      event.preventDefault();
      startChat();
    }
  });
  document.getElementById("chat-input").focus();
});

async function startChat() {
  const input = document.getElementById("chat-input");
  const response = document.getElementById("chat-response");
  const status = document.getElementById("chat-status");
  const chatContainer = response.parentElement; 
  const message = input.value.trim();

  if (!message) {
    alert("Please enter a message");
    return;
  }

  status.innerHTML =
    '<div class="loading"><div class="spinner"></div> Thinking...</div>';
  response.innerHTML+= `<br/><br/><span style="font-weight: bold;">You: </span><br/>` + message + `<br/><br/><span style="font-weight: bold;">AI: </span><br/>`;
  setTimeout(() => {
    chatContainer.scrollTop = chatContainer.scrollHeight;
  }, 10);

  try {
    const checkedRadio = document.querySelector('input[name="api-select"]:checked');
    let url; 
    switch(checkedRadio.value) {
      case 'tool-chat':
        url = `/api/v1/tool-call?userInput=${encodeURIComponent(message)}&toolName=local_weather`
        break;
      case 'chat':
      default:
        url = `/api/v1/chat?userInput=${encodeURIComponent(message)}`;
    }

    const result = await fetch(url);
    const reader = result.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      response.innerHTML += chunk;
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }

    status.innerHTML = '<span style="color: #10b981;">Complete</span>';
    setTimeout(() => {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 10);
  } catch (error) {
    response.innerHTML += `Error: ${error.message}`;
    status.innerHTML = '<span style="color: #ef4444;">Error</span>';
    setTimeout(() => {
      chatContainer.scrollTop = chatContainer.scrollHeight;
    }, 10);
  } finally {
    input.value = "";
    input.focus();
  }
}
function clearChat() {
  document.getElementById("chat-input").value = "";
  document.getElementById("chat-response").textContent =
    "yo.. what do you want to know??";
  document.getElementById("chat-status").innerHTML = "";
}

function getTools() {
  fetch('/api/v1/tools')
  .then(response => response.json())
  .then(data => {
    const toolsDiv = document.getElementById('tools');
    let toolsList = '<h3>Available Tools:</h3><ul>';
    data.forEach(tool => {
      toolsList += `<li><strong>${tool.name}:</strong> ${tool.description}</li>`;
    });
    toolsList += '</ul>';
    toolsDiv.innerHTML = toolsList;
  })
  .catch(error => {
    console.error('Error fetching tools:', error);
  });
}
