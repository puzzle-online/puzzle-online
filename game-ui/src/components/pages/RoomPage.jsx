import React, {useEffect, useState} from "react";
import ArrowBack from "@mui/icons-material/ArrowBack";
import IconButton from "@mui/joy/IconButton";
import Game from "../Game.jsx";

function Ball({color}) {
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

function RoomPage({handlers, sendRequest, onLeaveRoomButtonClick}) {
    const [roomId, setRoomId] = useState('');
    const [balls, setBalls] = useState([]);
    const [clientList, setClientList] = useState([]);

    const [playBallIdRed, setPlayBallIdRed] = useState('');
    const [playBallIdBlue, setPlayBallIdBlue] = useState('');
    const [playBallIdGreen, setPlayBallIdGreen] = useState('');

    const handleUpdate = (response) => {
        setBalls(response.balls);
        setClientList(response.clientIds);
        console.log(`Updated room: ${roomId}, balls: ${response.balls}, clients: ${response.clientIds}`);
    };

    const handleCreate = (response) => {
        setRoomId(response.roomId);
        setBalls(response.balls);
        setClientList(response.clientIds);
        console.log(`Created room: ${response.roomId}, balls: ${response.balls}, clients: ${response.clientIds}`);
    };

    const handleJoin = (response) => {
        setRoomId(response.roomId);
        setBalls(response.balls);
        setClientList(response.clientIds);
        console.log(`Joined room: ${response.roomId}, balls: ${response.balls}, clients: ${response.clientIds}`);
    };

    useEffect(() => {
        handlers['update'] = handleUpdate;
        handlers['create'] = handleCreate;
        handlers['join'] = handleJoin;
        console.log('added handleCreate, handleJoin, handleUpdate');
    }, []);

    const playBall = (color) => (e) => {
        e.preventDefault();
        sendRequest('play', {
            roomId: roomId,
            ball: {
                ballId: parseInt(color === 'red' ? playBallIdRed : color === 'blue' ? playBallIdBlue : playBallIdGreen),
                color: color,
            },
        });
    };
    const playRed = playBall('red');
    const playBlue = playBall('blue');
    const playGreen = playBall('green');

    return (
        <>
            <IconButton onClick={() => onLeaveRoomButtonClick(roomId)}>
                <ArrowBack/>
            </IconButton>
            <div>
                Current room ID: {roomId}
            </div>
            <div>
                Current clients: {clientList.map((clientId) => (
                <div key={clientId}>{clientId}</div>
            ))}
            </div>
            <div>
                {balls && balls.length ? (
                    <>
                        {balls.map((ball) => (
                            <Ball key={ball.ballId} color={ball.color}/>
                        ))}
                    </>
                ) : (
                    <p>No balls yet</p>
                )}
            </div>
            <form onSubmit={playRed}>
                <input value={playBallIdRed} onChange={(e) => setPlayBallIdRed(e.target.value)} type="text"
                       placeholder="Type your message here"/>
                <button type="submit">Make red</button>
            </form>
            <form onSubmit={playBlue}>
                <input value={playBallIdBlue} onChange={(e) => setPlayBallIdBlue(e.target.value)} type="text"
                       placeholder="Type your message here"/>
                <button type="submit">Make blue</button>
            </form>
            <form onSubmit={playGreen}>
                <input value={playBallIdGreen} onChange={(e) => setPlayBallIdGreen(e.target.value)} type="text"
                       placeholder="Type your message here"/>
                <button type="submit">Make green</button>
            </form>
            <Game sendRequest={sendRequest}/>
        </>
    )
}


export default RoomPage;