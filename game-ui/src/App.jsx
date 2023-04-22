import {useEffect, useRef, useState} from 'react'
import './App.css'

import React from 'react';
import HomePage from "./components/pages/HomePage.jsx";
import Header from "./components/Header.jsx";
import RoomsPage from "./components/pages/RoomsPage.jsx";
import RoomPage from "./components/pages/RoomPage.jsx";

function Pages() {
    const [page, setPage] = useState('home');

    const [clientId, setClientId] = useState('');
    const [ws, setWs] = useState(null);
    const handlers = useRef({})

    const handleConnect = (response) => {
        setClientId(response.clientId);
        console.log(`Connected with client ID: ${response.clientId}`);
    };

    const handleUnknown = (response) => {
        console.log('Unknown message received:', response);
    };

    const handleIncomingMessage = (event) => {
        const response = JSON.parse(event.data);
        console.log('Received message:', response);
        console.table(handlers)
        const handler = handlers[response.method] || handleUnknown;
        handler(response);
    };

    useEffect(() => {
        connect();
        handlers['connect'] = handleConnect;
        console.log('added handleConnect');
    }, []);

    const connect = () => {
        const newWs = new WebSocket('ws://localhost:3000/chat');
        console.log('WebSocket connecting...');
        newWs.onopen = () => {
            console.log('WebSocket connected');
        };
        newWs.onmessage = handleIncomingMessage
        newWs.onclose = () => {
            console.log('WebSocket disconnected');
            connect();
        };
        setWs(newWs);
    };

    const sendRequest = (method, data) => /*(e) => */{
        console.log(`sending request with method ${method} and data:`)
        console.log(data)
        // e.preventDefault();
        ws.send(JSON.stringify({
            method: method,
            clientId: clientId,
            ...data,
        }));
    };


    const handleRoomsButtonClick = () => {
        setPage('rooms');
    }

    const handleBackButtonClick = () => {
        setPage('home');
    }

    const handleLeaveButtonClick = (gameId) => {
        sendRequest('leave', { gameId: gameId });
        setPage('home');
    }

    const handleJoinButtonClick = (gameId) => {
        sendRequest('join', { gameId: gameId });
        setPage('room');
    }

    const handleCreateButtonClick = () => {
        sendRequest('create', {});
        setPage('room');
    }

    return <>
        {page === 'home' && <HomePage onRoomsButtonClick={handleRoomsButtonClick}/>}
        {page === 'rooms' && <RoomsPage handlers={handlers} sendRequest={sendRequest} onBackButtonClick={handleBackButtonClick} onJoinButtonClick={handleJoinButtonClick} onCreateButtonClick={handleCreateButtonClick}/>}
        {page === 'room' && <RoomPage handlers={handlers} sendRequest={sendRequest} onLeaveRoomButtonClick={handleLeaveButtonClick}/>}
    </>;
}

function App() {
    return (
        <div className="App">
            <Header/>
            <Pages/>
        </div>
    )
}

export default App
