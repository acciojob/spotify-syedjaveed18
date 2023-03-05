package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);

        users.add(user);
        userPlaylistMap.put(user,new ArrayList<>());
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        artistAlbumMap.put(artist,new ArrayList<>());
        return artist;
    }

    public Artist checkArtistExists(String artistName){
        for(Artist artist : artists){
            if(artist.getName().equals(artistName)){
                return artist;
            }
        }
        return null;
    }
    public Album createAlbum(String title, String artistName) {
        Artist artist = checkArtistExists(artistName);

        if(artist == null){
            artist = createArtist(artistName);
            artists.add(artist);
        }

        Album album = new Album(title);
        albums.add(album);
        artistAlbumMap.get(artist).add(album);
        albumSongMap.put(album,new ArrayList<>());

        return album;
    }

    public Album checkAlbumPresent(String albumName){
        for(Album album : albums){
            if(album.getTitle().equals(albumName)){
                return album;
            }
        }
        return null;
    }
    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = checkAlbumPresent(albumName);

        if(album == null){
            throw new Exception("Album does not exist");
        }else{
            Song song = new Song(title,length);
            albumSongMap.get(album).add(song);
            songs.add(song);

            songLikeMap.put(song,new ArrayList<>());

            return song;
        }
    }

    public User userPresent(String mobile){
        for(User user:users){
            if(user.getMobile().equals(mobile)){
                return user;
            }
        }
        return null;
    }
    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        //Create a playlist with given title and add all songs having the given length in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        //checking if mobile exits
        User userCreator = null;
        boolean mobileExists = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userCreator = user;
                mobileExists = true;
                break;
            }
        }

        if(!mobileExists){
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        List<Song> givenLengthSongs = new ArrayList<>();
        for(Song song : songs){
            if(song.getLength() == length){
                givenLengthSongs.add(song);
            }
        }

        playlists.add(playlist);
        playlistSongMap.put(playlist,givenLengthSongs);
        creatorPlaylistMap.put(userCreator,playlist);
        userPlaylistMap.get(userCreator).add(playlist);

        List<User> listeners = new ArrayList<>();
        listeners.add(userCreator);
        playlistListenerMap.put(playlist,listeners);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        //Create a playlist with given title and add all songs having the given titles in the database to that playlist
        //The creater of the playlist will be the given user and will also be the only listener at the time of playlist creation
        //If the user does not exist, throw "User does not exist" exception

        User userCreator = null;
        boolean mobileExists = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userCreator = user;
                mobileExists = true;
                break;
            }
        }

        if(!mobileExists){
            throw new Exception("User does not exist");
        }

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        List<Song> givenNameSong = new ArrayList<>();
        for(String songTitle : songTitles){
            for(Song song : songs){
                if(song.getTitle().equals(songTitle)){
                    givenNameSong.add(song);
                }
            }
        }

        playlistSongMap.put(playlist,givenNameSong);

        creatorPlaylistMap.put(userCreator,playlist);
        userPlaylistMap.get(userCreator).add(playlist);

        List<User> listeners = new ArrayList<>();
        listeners.add(userCreator);
        playlistListenerMap.put(playlist,listeners);

        return playlist;
    }

    public boolean checkUserIsCreator(User user, Playlist playlist){
        if(creatorPlaylistMap.containsKey(user)){
            return creatorPlaylistMap.get(user).equals(playlist);
        }
        return false;
    }
    public boolean checkUserIsListener(User user,Playlist playlist){
        return playlistListenerMap.get(playlist).contains(user);
    }
    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        //Find the playlist with given title and add user as listener of that playlist and update user accordingly
        //If the user is creater or already a listener, do nothing
        //If the user does not exist, throw "User does not exist" exception
        //If the playlist does not exists, throw "Playlist does not exist" exception
        // Return the playlist after updating

        User userCreator = null;
        boolean mobileExists = false;
        for(User user : users){
            if(user.getMobile().equals(mobile)){
                userCreator = user;
                mobileExists = true;
                break;
            }
        }

        if(!mobileExists){
            throw new Exception("User does not exist");
        }

        Playlist currentPlayList = null;
        boolean playListExists = false;
        for(Playlist playlist : playlists){
            if(playlist.getTitle().equals(playlistTitle)){
                currentPlayList = playlist;
                playListExists = true;
                break;
            }
        }

        if(!playListExists){
            throw new Exception("Playlist does not exist");
        }

        boolean isUserCreatorListener = checkUserIsListener(userCreator,currentPlayList) || checkUserIsCreator(userCreator,currentPlayList);

        if(isUserCreatorListener){
            return currentPlayList;
        }

        playlistListenerMap.get(currentPlayList).add(userCreator);
        userPlaylistMap.get(userCreator).add(currentPlayList);

        return currentPlayList;
    }

    public Song songExists(String songTitle){
        for(Song song:songs){
            if(song.getTitle().equals(songTitle))
                return song;
        }
        return null;
    }
    public Song likeSong(String mobile, String songTitle) throws Exception {
        //The user likes the given song. The corresponding artist of the song gets auto-liked
        //A song can be liked by a user only once. If a user tried to like a song multiple times, do nothing
        //However, an artist can indirectly have multiple likes from a user, if the user has liked multiple songs of that artist.
        //If the user does not exist, throw "User does not exist" exception
        //If the song does not exist, throw "Song does not exist" exception
        //Return the song after updating

        User user = userPresent(mobile);

        if(user == null){
            throw new Exception("User does not exist");
        }

        Song song = songExists(songTitle);

        if(song == null){
            throw new Exception("Song does not exist");
        }

        int songLike = 0;
        int artistLike = 0;

        Album album = getAlbumSong(song);
        Artist artist = getArtistAlbum(album);

        if(songLikeMap.containsKey(song)){
            if(!(songLikeMap.get(song).contains(user))){
                songLikeMap.get(song).add(user);
                songLike = song.getLikes()+1;
                artistLike = artist.getLikes()+1;
            }
        }else{
            songLikeMap.put(song,new ArrayList<>());
            songLikeMap.get(song).add(user);
            songLike = song.getLikes()+1;
            artistLike = artist.getLikes()+1;
        }

        song.setLikes(songLike);
        artist.setLikes(artistLike);

        return song;
    }

    public Album getAlbumSong(Song song){
        for(Album album : albumSongMap.keySet()){
            if(albumSongMap.get(album).contains(song)){
                return album;
            }
        }
        return null;
    }
    public Artist getArtistAlbum(Album album){
        for(Artist artist : artistAlbumMap.keySet()){
            if(artistAlbumMap.get(artist).contains(album)){
                return artist;
            }
        }
        return null;
    }
    public String mostPopularArtist() {
        String artistName = null;
        int likes = 0;
        for(Artist artist : artists){
            if(artist.getLikes() >= likes){
                likes = artist.getLikes();
                artistName = artist.getName();
            }
        }
        return artistName;
    }

    public String mostPopularSong() {
        String songName = null;
        int likes = 0;
        for(Song song : songs){
            if(song.getLikes() >= likes){
                likes = song.getLikes();
                songName = song.getTitle();
            }
        }
        return songName;
    }
}
