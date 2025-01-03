// Lista de canciones por defecto (se puede llenar dinámicamente desde el backend)
let songs = [ //TODO: Ten en cuenta que si los nombres de las canciones tienen caracteres raros como apostrofes igual falla
    'Bohemian Rhapsody',
    'Shape of You',
    'Sweet Child O Mine',
    'Lose Yourself'
];
let serverUrl = 'http://localhost:3000'; // URL del servidor backend

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
		body: JSON.stringify({ vote: songName, sessionId: sessionId }),
	  }).then(response => {
		if (response.ok) {
		  alert('Voto registrado exitosamente.Tu id de sesión: ' + sessionId);
		} else {
		  alert('Error al registrar el voto. Tu id de sesión: ' + sessionId);
		}
	  }).catch(error => {
		alert('Error al configurar el servidor. Tu id de sesión: ' + sessionId);
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
        document.getElementById('time-remaining').innerText = `Tiempo restante: ${formatTime(timeRemaining)}`;
        timeRemaining--;
    }
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
