import {useEffect, useState} from 'react'
import './App.css'

import React from 'react';
import HomePage from "./components/pages/HomePage.jsx";
import {Header} from "./components/Header.jsx";
import RoomsPage from "./components/pages/RoomsPage.jsx";

function Ball({ color }) {
    const ballStyle = {
        width: '50px',
        height: '50px',
        borderRadius: '50%',
        display: 'inline-block',
        margin: '10px',
        backgroundColor: 'white',
    };

    if (color === 'red') {
        ballStyle.backgroundColor = 'red';
    } else if (color === 'blue') {
        ballStyle.backgroundColor = 'blue';
    } else if (color === 'green') {
        ballStyle.backgroundColor = 'green';
    }

    return <div style={ballStyle}></div>;
}


function ChatApp() {
    const [ws, setWs] = useState(null);
    const [clientId, setClientId] = useState('');
    const [gameId, setGameId] = useState('');
    const [balls, setBalls] = useState([]);
    const [clientList, setClientList] = useState([]);
    const [gameJoinInput, setGameJoinInput] = useState('');
    const [playBallIdRed, setPlayBallIdRed] = useState('');
    const [playBallIdBlue, setPlayBallIdBlue] = useState('');
    const [playBallIdGreen, setPlayBallIdGreen] = useState('');

    useEffect(() => {
        connect();
    }, []);

    const handleConnect = (response) => {
        setClientId(response.clientId);
        console.log(`Connected with client ID: ${response.clientId}`);
    };

    const handleCreate = (response) => {
        setGameId(response.gameId);
        console.log(`Created game: ${response.gameId}`);
    };

    const handleJoin = (response) => {
        setGameId(response.gameId);
        setBalls(response.balls);
        setClientList(response.clientIds);
        console.log(`Joined game: ${response.gameId}, balls: ${response.balls}, clients: ${response.clientIds}`);
    };

    const handleUpdate = (response) => {
        setBalls(response.balls);
        setClientList(response.clientIds);
        console.log(`Updated game: ${gameId}, balls: ${response.balls}, clients: ${response.clientIds}`);
    };

    const handleUnknown = (response) => {
        console.log('Unknown message received:', response);
    };

    const handleIncomingMessage = (event) => {
        const response = JSON.parse(event.data);
        console.log('Received message:', response);
        const handlers = {
            connect: handleConnect,
            create: handleCreate,
            join: handleJoin,
            update: handleUpdate,
        };
        const handler = handlers[response.method] || handleUnknown;
        handler(response);
    };

    const connect = () => {
        const newWs = new WebSocket('ws://localhost:3000/chat');
        console.log('WebSocket connecting...');
        newWs.onopen = () => {
            console.log('WebSocket connected');
        };
        newWs.onmessage = handleIncomingMessage;
        newWs.onclose = () => {
            console.log('WebSocket disconnected');
            connect();
        };
        setWs(newWs);
    };

    const sendRequest = (method, data) => (e) => {
        e.preventDefault();
        ws.send(JSON.stringify({
            method: method,
            clientId: clientId,
            ...data,
        }));
    };

    const createGame = sendRequest('create', {});
    const joinGame = sendRequest('join', { gameId: gameJoinInput });
    const playBall = (color) => sendRequest('play', {
        gameId: gameId,
        ball: {
            ballId: parseInt(color === 'red' ? playBallIdRed : color === 'blue' ? playBallIdBlue : playBallIdGreen),
            color: color,
        },
    });
    const playRed = playBall('red');
    const playBlue = playBall('blue');
    const playGreen = playBall('green');

    return (
        <div>
            <div>
                Current session: {clientId}
            </div>
            <div>
                Current game ID: {gameId}
            </div>
            <div>
                Current clients: {clientList.join(', ')}
            </div>
            {balls && balls.length ? (
                <>
                    {balls.map((ball) => (
                        <Ball key={ball.ballId} color={ball.color} />
                    ))}
                </>
            ) : (
                <p>No balls yet</p>
            )}
            <form onSubmit={createGame}>
                <button type="submit">Create Game</button>
            </form>
            <form onSubmit={joinGame}>
                <input value={gameJoinInput} onChange={(e) => setGameJoinInput(e.target.value)} type="text" placeholder="Type your message here" />
                <button type="submit">Join Game</button>
            </form>
            <form onSubmit={playRed}>
                <input value={playBallIdRed} onChange={(e) => setPlayBallIdRed(e.target.value)} type="text" placeholder="Type your message here" />
                <button type="submit">Make red</button>
            </form>
            <form onSubmit={playBlue}>
                <input value={playBallIdBlue} onChange={(e) => setPlayBallIdBlue(e.target.value)} type="text" placeholder="Type your message here" />
                <button type="submit">Make blue</button>
            </form>
            <form onSubmit={playGreen}>
                <input value={playBallIdGreen} onChange={(e) => setPlayBallIdGreen(e.target.value)} type="text" placeholder="Type your message here" />
                <button type="submit">Make green</button>
            </form>
        </div>
    );
}


function HelloWorld() {
    return <>
        <Header/>
        {/*<ChatApp/>*/}
        {/*<HomePage/>*/}
        <RoomsPage/>
    </>;
}

function App() {
    return (
        <div className="App">
            <HelloWorld/>
        </div>
    )
}

export default App
