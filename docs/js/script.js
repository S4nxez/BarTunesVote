const urlParams = new URLSearchParams(window.location.search);
const serverUrl = urlParams.get("server");


let interval;
let sessionId;
let votingEnabled = true;

const socket = new SockJS(`${serverUrl}/ws`, null, { withCredentials: false });
const stompClient = Stomp.over(socket);

// Conexión al servidor WebSocket
stompClient.connect({}, function (frame) {
  console.log("Connected: " + frame);

  // Suscripción al canal de mensajes
  stompClient.subscribe("/topic/updates", function (message) {
    console.log("Server message:", message.body);
    loadSongs();
  });
});

// Cargar las canciones en la vista
function loadSongs() {
  const songListDiv = document.getElementById("song-list");
  fetch(`${serverUrl}/songs`, {
    method: "GET",
    headers: {
      "Content-Type": "application/json",
      "ngrok-skip-browser-warning": "1231",
    },
  })
    .then((response) => {
      console.log("Response:", response);
      return response.json();
    })
    .then((songsData) => {
      // Ensure songsData is an array
      const songsArray = Array.isArray(songsData) ? songsData : [songsData];

      songListDiv.innerHTML = ""; // Clear existing content

      songsArray.forEach((song) => {
        const card = document.createElement("div");
        card.classList.add("card");
        card.setAttribute("onclick", `vote('${song.place}')`);
        card.innerHTML = `
                <div class="card-body">
                    <div class="song-container">
                        <p class="song-title">${song.songName}</p>
                    </div>
                </div>
            `;
        songListDiv.appendChild(card);
      });
    })
    .catch((error) => {
      console.error("Error loading songs:", error);
      songListDiv.innerHTML = "<p>Error loading songs from playlist</p>";
    });
}

function generateSessionId() {
  if (!localStorage.getItem("sessionId")) {
    const array = new Uint32Array(4);
    window.crypto.getRandomValues(array);
    const sessionId = Array.from(array, (dec) =>
      ("0" + dec.toString(16)).substr(-2)
    ).join("");
    localStorage.setItem("sessionId", sessionId);
  }
  return localStorage.getItem("sessionId");
}

// Función para votar por una canción
function vote(placeUi) {
  if (!votingEnabled) {
    alert("El tiempo de votación ha terminado.");
    return;
  }
  var voteUI = {
    place: placeUi,
    sessionId: sessionId,
  };
  fetch(`${serverUrl}/api/vote`, {
    method: "POST",

    headers: {
      "Content-Type": "application/json",
      "ngrok-skip-browser-warning": "1231",
    },
    body: JSON.stringify(voteUI),
  })
    .then((response) => {
      if (response.ok) {
        alert("Voto registrado exitosamente.");
      } else {
        return response.text().then((errorText) => {
          const errorMessage = errorText
            ? JSON.parse(errorText).message
            : "Error desconocido";
          alert(errorMessage);
        });
      }
    })
    .catch((error) => {
      alert("Error al registrar el voto: " + error);
    });
}

// Función para añadir una nueva canción
function addSong() {
  const newSongInput = document.getElementById("new-song");
  const newSong = newSongInput.value.trim();

  if (newSong) {
    songs.push(newSong);
    loadSongs();
    newSongInput.value = ""; // Limpiar el campo de texto
  } else {
    alert("Por favor ingresa un nombre de canción.");
  }
}

function getWinner() {
  /*@CrossOrigin(origins = "https://s4nxez.github.io/BarTunesVote/")
    @GetMapping
    public String getWinner(){
        return voteService.getWinner();
    }
		este es el metodo que se llama desde el backend para obtener el ganador
		*/
  fetch(`${serverUrl}/api/vote`, {
    method: "GET",

    headers: {
      "Content-Type": "application/json",
      "ngrok-skip-browser-warning": "1231",
    },
  })
    .then((response) => {
      if (response.ok) {
        return response.json();
      } else {
        throw new Error(
          "Error al obtener el ganador: " +
            response.status +
            response.statusText
        );
      }
    })
    .catch((error) => {
      alert("Error al obtener el ganador." + error);
    });
}

function resetVotes() {
  /* Metodo del backend:
	@ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void resetVotes() {
        voteService.resetVotes();
    }
	*/
  fetch(`${serverUrl}/api/vote`, {
    method: "DELETE",
    headers: {
      "Content-Type": "application/json",
      "ngrok-skip-browser-warning": "1231",
    },
  })
    .then((response) => {
      if (response.ok) {
        alert("Votos reseteados exitosamente.");
      } else {
        alert(
          "Error al resetear los votos. Response status " + response.status
        );
      }
    })
    .catch((error) => {
      alert("Error al configurar el servidor. Tu id de sesión: " + error);
    });
}


// Llamar a la función para cargar las canciones y comenzar el temporizador al cargar la página
window.onload = function () {
  loadSongs();
  sessionId = generateSessionId();
};
