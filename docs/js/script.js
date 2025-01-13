const urlParams = new URLSearchParams(window.location.search);
const serverUrl = urlParams.get('server');
//const serverUrl = 'https://e6b0-90-167-202-198.ngrok-free.app';

let timeRemaining = 60; // Tiempo de votación en segundos
let interval;
let sessionId;
let votingEnabled = true;

// Cargar las canciones en la vista
function loadSongs(playlistId) {
    const songListDiv = document.getElementById('song-list');
    fetch(`${serverUrl}/songs`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
			"ngrok-skip-browser-warning": "1231"
        },
    })
    .then(response => {
        console.log('Response:', response);
        return response.json();
    })
    .then(songsData => {
        // Ensure songsData is an array
        const songsArray = Array.isArray(songsData) ? songsData : [songsData];

        songListDiv.innerHTML = ''; // Clear existing content

        songsArray.forEach(song => {
            const card = document.createElement('div');
            card.classList.add('card');
            card.setAttribute('onclick', `vote('${song.place}')`);
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
    .catch(error => {
        console.error('Error loading songs:', error);
        songListDiv.innerHTML = '<p>Error loading songs from playlist</p>';
    });
}

function generateSessionId() {
    if (!localStorage.getItem('sessionId')) {
        const array = new Uint32Array(4);
        window.crypto.getRandomValues(array);
        const sessionId = Array.from(array, dec => ('0' + dec.toString(16)).substr(-2)).join('');
        localStorage.setItem('sessionId', sessionId);
    }
    return localStorage.getItem('sessionId');
}

// Función para votar por una canción
function vote(placeUi) {
    if (!votingEnabled) {
        alert("El tiempo de votación ha terminado.");
        return;
    }
	var voteUI = {
		place: placeUi,
		sessionId: sessionId
	}
    fetch(`${serverUrl}/api/vote`, {
        method: 'POST',

        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(voteUI)
    })
    .then(response => {
        if (response.ok) {
            alert('Voto registrado exitosamente.');
        } else {
            return response.text().then(errorText => {
                const errorMessage = errorText ? JSON.parse(errorText).message : 'Error desconocido';
                alert(errorMessage);
            });
        }
    })
    .catch(error => {
        alert('Error al registrar el voto: ' + error);
    });
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
        document.getElementById('time-remaining').innerText = 'Votación finalizada';
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
	const playlistId = '46BC7zm67B71WZu19pYo9Q'; // Replace with actual playlist ID
    loadSongs(playlistId);
    startVotingTimer();
    sessionId = generateSessionId();
};
