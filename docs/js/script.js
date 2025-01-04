// Lista de canciones por defecto (se puede llenar dinámicamente desde el backend)
let songs = [ //TODO: Ten en cuenta que si los nombres de las canciones tienen caracteres raros como apostrofes igual falla
    'Bohemian Rhapsody',
    'Shape of You',
    'Sweet Child O Mine',
    'Lose Yourself'
];
const urlParams = new URLSearchParams(window.location.search);
const serverUrl = urlParams.get('server');

let timeRemaining = 60; // Tiempo de votación en segundos
let interval;
let sessionId;
let votingEnabled = true;

// Cargar las canciones en la vista
function loadSongs() {
    const songListDiv = document.getElementById('song-list');
    songListDiv.innerHTML = ''; // Limpiar la lista antes de recargar

    songs.forEach(song => {
        const card = document.createElement('div');
        card.classList.add('card');
        card.setAttribute('onclick', `vote('${song}')`); // Añadir evento de clic a la tarjeta

        card.innerHTML = `
            <div class="card-body">
                <div class="song-container">
                    <p class="song-title">${song}</p>
                </div>
            </div>
        `;
        songListDiv.appendChild(card);
    });
}

function generateSessionId() {
	if (!localStorage.getItem('sessionId')) {
	  localStorage.setItem('sessionId', crypto.randomUUID());
	}
	return localStorage.getItem('sessionId');
}

// Función para votar por una canción
function vote(songName) {
    if (!votingEnabled) {
        alert("El tiempo de votación ha terminado.");
        return;
    }
	fetch(`${serverUrl}/api/vote`, {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify({
			songId: songName,
			songName: songName, //TODO Cambiar por el id de la canción
			sessionId: sessionId
		}),
	  }).then(response => {
		if (response.ok) {
		  alert('Voto registrado exitosamente.Tu id de sesión: ' + sessionId);
		} else {
		  alert('Error al registrar el voto. Response status ' + response.status);
		}
	  }).catch(error => {
		alert('Error al configurar el servidor. Tu id de sesión: ' + error);
	  });
    // Aquí enviaría el voto al backend para registrar la votación.
}

// Función para añadir una nueva canción
function addSong() {
    const newSongInput = document.getElementById('new-song');
    const newSong = newSongInput.value.trim();

    if (newSong) {
        songs.push(newSong);
        loadSongs();
        newSongInput.value = ''; // Limpiar el campo de texto
    } else {
        alert("Por favor ingresa un nombre de canción.");
    }
}

// Actualizar el tiempo restante y deshabilitar votación cuando termine
function updateTime() {
    if (timeRemaining <= 0) {
        clearInterval(interval);
        votingEnabled = false; // Desactivar votación
        document.getElementById('time-remaining').innerText = 'El tiempo ha terminado. No puedes votar más.';
    } else {
        document.getElementById('time-remaining').innerText = `Tiempo: ${formatTime(timeRemaining)}`;
        timeRemaining--;
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
			method: 'GET',
			headers: { 'Content-Type': 'application/json' },
		  }).then(response => {
			if (response.ok) {
			  return response.json();
			} else {
			  throw new Error('Error al obtener el ganador: ' + response.status + response.statusText);
			}
		  }).then(data => {
			const winner = data;
			alert(`El ganador es: ${winner.songName} con ${winner.votes} votos.`);
		  }).catch(error => {
			alert('Error al obtener el ganador.' + error);
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
		method: 'DELETE',
		headers: { 'Content-Type': 'application/json' },
	  }).then(response => {
		if (response.ok) {
		  alert('Votos reseteados exitosamente.');
		}
		else {
		  alert('Error al resetear los votos. Response status ' + response.status);
		}
		}).catch(error => {
		alert('Error al configurar el servidor. Tu id de sesión: ' + error);
	  });

}

// Formatear el tiempo a MM:SS
function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return `${String(minutes).padStart(2, '0')}:${String(sec).padStart(2, '0')}`;
}

// Iniciar el contador de tiempo
function startVotingTimer() {
    interval = setInterval(updateTime, 1000);
}

// Llamar a la función para cargar las canciones y comenzar el temporizador al cargar la página
window.onload = function() {
    loadSongs();
    startVotingTimer();
	sessionId = generateSessionId();
};
