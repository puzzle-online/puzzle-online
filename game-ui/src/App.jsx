import {useEffect, useRef, useState} from 'react'
import './App.css'

import React from 'react';
import HomePage from "./components/pages/HomePage.jsx";
import Header from "./components/Header.jsx";
import RoomsPage from "./components/pages/RoomsPage.jsx";
import RoomPage from "./components/pages/RoomPage.jsx";

function Pages() {
    const [page, setPage] = useState('home');

    const handleRoomsButtonClick = () => {
        setPage('rooms');
    }

    const handleBackButtonClick = () => {
        setPage('home');
    }

    const handleJoinButtonClick = (gameId) => {
        sendRequest('join', { gameId: gameId });
        setPage('room');
    }

    return <>
        {page === 'home' && <HomePage onRoomsButtonClick={handleRoomsButtonClick}/>}
        {page === 'rooms' && <RoomsPage onBackButtonClick={handleBackButtonClick} onPlayButtonClick={handlePlayButtonClick}/>}
        {page === 'room' && <RoomPage onBackButtonClick={handleBackButtonClick}/>}
    </>;
}

function App() {
    return (
        <div className="App">
            <Header/>
            <ChatApp/>
            <Pages/>
        </div>
    )
}

export default App
