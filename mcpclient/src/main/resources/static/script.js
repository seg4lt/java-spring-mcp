async function startChat() {
  const input = document.getElementById("chat-input");
  const response = document.getElementById("chat-response");
  const status = document.getElementById("chat-status");
  const message = input.value.trim();

  if (!message) {
    alert("Please enter a message");
    return;
  }

  status.innerHTML =
    '<div class="loading"><div class="spinner"></div> Thinking...</div>';
  response.textContent = "";

  try {
    const result = await fetch(
      `/api/v1/chat?userInput=${encodeURIComponent(message)}`
    );
    const reader = result.body.getReader();
    const decoder = new TextDecoder();

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      const chunk = decoder.decode(value, { stream: true });
      response.textContent += chunk;
      response.scrollTop = response.scrollHeight;
    }

    status.innerHTML = '<span style="color: #10b981;">Complete</span>';
  } catch (error) {
    response.textContent = `Error: ${error.message}`;
    status.innerHTML = '<span style="color: #ef4444;">Error</span>';
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
