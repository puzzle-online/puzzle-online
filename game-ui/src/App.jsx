import {useEffect, useRef, useState} from 'react'
import './App.css'

import React from 'react';
import HomePage from "./components/pages/HomePage.jsx";
import Header from "./components/Header.jsx";
import RoomsPage from "./components/pages/RoomsPage.jsx";
import RoomPage from "./components/pages/RoomPage.jsx";
import Game from "./components/Game.jsx";

function Pages() {
    const [page, setPage] = useState('home');

    const [clientId, setClientId] = useState('');
    const [ws, setWs] = useState(null);
    const handlers = useRef({})

    const defaultBoxes = {
        boxes: [
            {id: 0, x: 0, y: 0},
            {id: 1, x: 100, y: 0},
            {id: 2, x: 200, y: 0},
            {id: 3, x: 300, y: 0},
            {id: 4, x: 400, y: 0},
            {id: 5, x: 500, y: 0},
            {id: 6, x: 600, y: 0},
            {id: 7, x: 700, y: 0},
            {id: 8, x: 0, y: 100},
            {id: 9, x: 100, y: 100},
            {id: 10, x: 200, y: 100},
            {id: 11, x: 300, y: 100},
            {id: 12, x: 400, y: 100},
            {id: 13, x: 500, y: 100},
            {id: 14, x: 600, y: 100},
            {id: 15, x: 700, y: 100}
        ]
    };

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
        const newWs = new WebSocket('ws://localhost:3000/game');
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

    const sendRequest = (method, data) => /*(e) => */ {
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

    const handleLeaveButtonClick = (roomId) => {
        sendRequest('leave', {roomId: roomId});
        setPage('home');
    }

    const handleJoinButtonClick = (roomId) => {
        sendRequest('join', {roomId: roomId});
        setPage('room');
    }

    const handleCreateButtonClick = () => {
        sendRequest('create', defaultBoxes);
        setPage('room');
    }

    return <>
        {page === 'home' && <HomePage onRoomsButtonClick={handleRoomsButtonClick}/>}
        {page === 'rooms' &&
            <RoomsPage handlers={handlers} sendRequest={sendRequest} onBackButtonClick={handleBackButtonClick}
                       onJoinButtonClick={handleJoinButtonClick} onCreateButtonClick={handleCreateButtonClick}/>}
        {page === 'room' &&
            <RoomPage handlers={handlers} sendRequest={sendRequest} onLeaveRoomButtonClick={handleLeaveButtonClick} defaultBoxes={defaultBoxes} clientId={clientId}/>}
    </>;
}

function App() {
    return (
        <div className="App">
            {/*<Header/>*/}
            <Pages/>
            {/*<Game/>*/}
        </div>
    )
}

export default App
