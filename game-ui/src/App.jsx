import {useEffect, useState} from 'react'
import reactLogo from './assets/react.svg'
import viteLogo from '/vite.svg'
import './App.css'

function Ball(props) {
    const {color} = props;

    return (
        <div className={`ball ${color}-ball`}/>
    );
}

function ChatApp() {
    const [ws, setWs] = useState(null);
    const [messageText, setMessageText] = useState('');
    const [gameJoinInput, setGameJoinInput] = useState('');
    const [messages, setMessages] = useState([]);
    const [clientId, setClientId] = useState('');
    const [game, setGame] = useState({});
    const [playBallIdRed, setPlayBallIdRed] = useState('');
    const [playBallIdBlue, setPlayBallIdBlue] = useState('');
    const [playBallIdGreen, setPlayBallIdGreen] = useState('');

    useEffect(() => {
        connect();
    }, []);

    const connect = () => {
        const newWs = new WebSocket('ws://localhost:3000/chat');
        console.log('WebSocket connecting...');
        newWs.onopen = () => {
            console.log('WebSocket connected');
        };
        newWs.onmessage = (event) => {
            const response = JSON.parse(event.data);
            console.log('WebSocket message received:', response);
            if (response.method === 'connect') {
                setClientId(response.clientId);
            } else if (response.method === 'create') {
                setGame(response.game);
            } else if (response.method === 'chat') {
                setMessages((prevMessages) => [
                    ...prevMessages,
                    `${response.message.sender}: ${response.message.content}`,
                ]);
            } else if (response.method === 'join') {
                setGame(response.game);
                console.log('Joined game', JSON.stringify(game, null, 2));
            } else if (response.method === 'update') {
                setGame(response.game);
            } else {
                setMessages((prevMessages) => [
                    ...prevMessages,
                    "Unknown message: " + JSON.stringify(response, null, 2),
                ]);
            }
        };
        newWs.onclose = () => {
            console.log('WebSocket disconnected');
            connect();
        };
        setWs(newWs);
    };

    const sendMessage = (e) => {
        e.preventDefault();
        if (!messageText) {
            return;
        }
        ws.send(
            JSON.stringify({
                method: 'chat',
                sender: 'Client',
                content: messageText,
                timestamp: new Date().toLocaleTimeString(),
            })
        );
        setMessageText('');
    };

    const createGame = (e) => {
        e.preventDefault();
        ws.send(
            JSON.stringify({
                method: 'create',
                clientId: clientId,
            })
        );
    };

    const joinGame = (e) => {
        e.preventDefault();
        ws.send(
            JSON.stringify({
                method: 'join',
                clientId: clientId,
                gameId: gameJoinInput,
            })
        );
    };

    const playRed = (e) => {
        e.preventDefault();
        ws.send(
            JSON.stringify({
                method: 'play',
                clientId: clientId,
                gameId: game.gameId,
                ball: {
                    ballId: parseInt(playBallIdRed),
                    color: 'red',
                },
            })
        );
    };

    const playBlue = (e) => {
        e.preventDefault();
        ws.send(
            JSON.stringify({
                method: 'play',
                clientId: clientId,
                gameId: game.gameId,
                ball: {
                    ballId: parseInt(playBallIdBlue),
                    color: 'blue',
                },
            })
        );
    };

    const playGreen = (e) => {
        e.preventDefault();
        ws.send(
            JSON.stringify({
                method: 'play',
                clientId: clientId,
                gameId: game.gameId,
                ball: {
                    ballId: parseInt(playBallIdGreen),
                    color: 'green',
                },
            })
        );
    };

    return (
        <div>
            <h1>Chat App</h1>
            <div>
                Current session: {clientId}
            </div>
            <div>
                Current game ID: {game.gameId}, players: {game.clients}
            </div>
            {messages.map((message, index) => (
                <div key={index}>
                    {message}
                </div>
            ))}
            {game.balls && game.balls.length ? (
                <>
                    {game.balls.map((ball) => (
                        <Ball key={ball} color={ball} />
                    ))}
                </>
            ) : (
                <p>No balls yet</p>
            )}
            <form onSubmit={sendMessage}>
                <input value={messageText} onChange={(e) => setMessageText(e.target.value)} type="text" placeholder="Type your message here" />
                <button type="submit">Send</button>
            </form>
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
        <h1>Chat App</h1>
        <ChatApp/>
    </>;
}

function App() {
    return (
        <div className="App">
            <div>
                <a href="https://vitejs.dev" target="_blank">
                    <img src={viteLogo} className="logo" alt="Vite logo"/>
                </a>
                <a href="https://reactjs.org" target="_blank">
                    <img src={reactLogo} className="logo react" alt="React logo"/>
                </a>
            </div>
            <HelloWorld/>
        </div>
    )
}

export default App
