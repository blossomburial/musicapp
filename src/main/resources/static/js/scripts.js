console.log("Script loaded!");

function createPlaylist(event) {
        event.preventDefault();
        const name = document.getElementById('newPlaylistName').value;

        fetch('/api/playlist/create', {
            method: 'POST',
            headers: {
                "Content-Type": "application/json",
            },
            credentials: "include",
            body: JSON.stringify({ name })
        })
        .then(response => response.json())
        .then(data => {
        console.log(data);
            alert("Плейлист создан: " + data.name);
        });
    }

function addToPlaylist(element) {
    const trackId = element.getAttribute('data-track-id');
    const playlistId = element.getAttribute('data-playlist-id');

    fetch('/api/playlist/add-track', {
        method: 'POST',
        headers: {
            "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ trackId, playlistId })
    })
    .then(response => {
        if (response.ok) {
            alert("Трек добавлен!");
        } else {
            alert("Ошибка добавления трека");
        }
    });
}

function searchTracks() {
    const query = document.getElementById('searchQuery').value;

    fetch(`/api/search?query=${encodeURIComponent(query)}`)
        .then(res => res.json())
        .then(tracks => {
            const container = document.getElementById('results');
            container.innerHTML = '';
            tracks.forEach(track => {
                const div = document.createElement('div');
                div.innerHTML = `

                    <strong>${track.title}</strong> — ${track.artist} (${track.album})
                    <button type="submit" class="add-button">➕ В плейлист</button>
                `;
                container.appendChild(div);
            });
        });
}