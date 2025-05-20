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