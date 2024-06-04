import React, {useEffect, useRef, useState} from 'react'
import './App.css'
import HomePage from "./components/pages/HomePage.jsx";
import RoomsPage from "./components/pages/RoomsPage.jsx";
import RoomPage from "./components/pages/RoomPage.jsx";

function Pages() {
    const [page, setPage] = useState('home');

    const [clientId, setClientId] = useState('');
    const [ws, setWs] = useState(null);
    const handlers = useRef({})
    const [nickname, setNickname] = useState('');

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
            console.error('WebSocket disconnected');
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


    const handleRoomsButtonClick = (nickname) => {
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
        sendRequest('join', {roomId: roomId, nickname: nickname});
        setPage('room');
    }

    const handleCreateButtonClick = () => {
        sendRequest('create', {
            boxes: [...Array(16)].map((_, i) => {
                let x = Math.random() * 300;
                let y = Math.random() * 700;
                return ({
                    id: i,
                    x: Math.floor((Math.random() < 0.5 ? x : x + 400 + 400)),
                    y: Math.floor(y),
                    z: i,
                    state: "released", // TODO: reconsider
                });
            }),
            nickname: nickname,
        });
        setPage('room');
    }

    return <>
        {page === 'home' && <HomePage
            onRoomsButtonClick={handleRoomsButtonClick}
            nickname={nickname}
            setNickname={setNickname}
        />}
        {page === 'rooms' && <RoomsPage
            handlers={handlers}
            sendRequest={sendRequest}
            onBackButtonClick={handleBackButtonClick}
            onJoinButtonClick={handleJoinButtonClick}
            onCreateButtonClick={handleCreateButtonClick}
            nickname={nickname}
        />}
        {page === 'room' && <RoomPage
            handlers={handlers}
            sendRequest={sendRequest}
            onLeaveRoomButtonClick={handleLeaveButtonClick}
            clientId={clientId}
            nickname={nickname}
        />}
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
