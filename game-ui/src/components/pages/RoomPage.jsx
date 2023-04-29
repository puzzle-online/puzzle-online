import React, {useEffect, useState} from "react";
import ArrowBack from "@mui/icons-material/ArrowBack";
import IconButton from "@mui/joy/IconButton";
import Game from "../Game.jsx";

function RoomPage({handlers, sendRequest, onLeaveRoomButtonClick, clientId}) {
    const [roomId, setRoomId] = useState('');
    const [boxes, setBoxes] = useState([]);
    const [clients, setClients] = useState([]);

    // const [playBallIdRed, setPlayBallIdRed] = useState('');
    // const [playBallIdBlue, setPlayBallIdBlue] = useState('');
    // const [playBallIdGreen, setPlayBallIdGreen] = useState('');

    const handleUpdate = (response) => {
        setBoxes(response.boxes);
        setClients(response.clients);
        console.log(`Updated room: ${roomId}, clients: ${response.clients}`);
    };

    const handleCreate = (response) => {
        setRoomId(response.roomId);
        setBoxes(response.boxes);
        setClients(response.clients);
        console.log(`Created room: ${response.roomId}, clients: ${response.clients}`);
    };

    const handleJoin = (response) => {
        setRoomId(response.roomId);
        setBoxes(response.boxes);
        setClients(response.clients);
        console.log(`Joined room: ${response.roomId}, balls: ${response.boxes}, clients: ${response.clients}`);
    };

    useEffect(() => {
        handlers['update'] = handleUpdate;
        handlers['create'] = handleCreate;
        handlers['join'] = handleJoin;
        console.log('added handleCreate, handleJoin, handleUpdate');
    }, [roomId]);

    return (
        <>
            <IconButton onClick={() => onLeaveRoomButtonClick(roomId)}>
                <ArrowBack/>
            </IconButton>
            <div>
                Current room ID: {roomId}
            </div>
            {roomId !== '' ?
                <Game sendRequest={sendRequest} roomId={roomId} boxes={boxes} clients={clients} clientId={clientId}/> :
                <div>Creating room...</div>
            }
        </>
    )
}


export default RoomPage;