import React, {useEffect, useRef, useState} from "react";

import PlayArrow from "@mui/icons-material/PlayArrow";
import Home from "@mui/icons-material/Home";
import Search from "@mui/icons-material/Search";
import ArrowBack from "@mui/icons-material/ArrowBack";
import Refresh from '@mui/icons-material/Refresh';

import Stack from "@mui/joy/Stack";
import Button from "@mui/joy/Button";
import Input from "@mui/joy/Input";
import Typography from "@mui/joy/Typography";
import Select from "@mui/joy/Select";
import Option from "@mui/joy/Option";
import Card from "@mui/joy/Card";
import IconButton from "@mui/joy/IconButton";
import Grid from "@mui/joy/Grid";
import AspectRatio from '@mui/joy/AspectRatio';

function PageTitle({children}) {
    return (
        <Typography
            level="display2"
            color="textPrimary"
        >
            {children}
        </Typography>
    );
}

function Options() {
    return (
        // TODO: boxShadow: 'none'
        <Select placeholder="Difficulty">
            <Option>Easy</Option>
            <Option>Medium</Option>
            <Option>Hard</Option>
        </Select>
    );
}

function TopBar({onBackButtonClick, onRefreshButtonClick}) {
    return (
        <Grid container
              spacing={2}
              justifyContent="space-between"
              alignItems="center"
        >
            <Grid xs={3.5}>
                <Stack
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    spacing={2}
                >
                    <IconButton onClick={onBackButtonClick}>
                        <ArrowBack/>
                    </IconButton>
                    {/*TODO: add refresh button*/}
                    <IconButton onClick={onRefreshButtonClick}>
                        <Refresh/>
                    </IconButton>
                    <Input
                        startDecorator={<Search/>}
                        placeholder="Search"
                    />
                </Stack>
            </Grid>
            <Grid xs={4}>
                <PageTitle>
                    ROOMS
                </PageTitle>
            </Grid>
            <Grid xs={3.5}>
                <Options/>
            </Grid>
        </Grid>
    );
}

function Rooms({rooms, selectedGameId, updateSelectedGameId}) {
    return (
        <Grid
            container
            spacing={2}
            columns={2}
            justifyContent="center"
            alignItems="center"
        >
            {rooms.map((room, index) => (
                <Grid key={index}>
                    <Card
                        variant="solid"
                        color={(selectedGameId === room.gameId) ? "primary" : "default"}
                        sx={{width: 300}}
                        onClick={() => {
                            updateSelectedGameId(room.gameId);
                        }}
                        // TODO: make proper hover and selected states
                    >
                        <AspectRatio>
                            <div>
                                <img src={"https://thecatapi.com/api/images/get?format=src&type=gif&id=" + index}
                                     alt="A random cat"/>
                            </div>
                        </AspectRatio>
                        {/*TODO: fix text coloring*/}
                        <Typography
                            mt={2}
                        >{room.gameId.substring(0, 10)}...</Typography>
                        <Typography
                            level="body2"
                            color={(selectedGameId === room.gameId) ? "textPrimary" : "neutral"}
                        >Players: {room.clientAmount}</Typography>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
}

function Actions({selectedGameId, onJoinButtonClick, onCreateButtonClick}) {
    return (
        <Stack
            direction="row"
            justifyContent="center"
            alignItems="center"
            spacing={2}
        >
            <Button startDecorator={<Home/>} onClick={onCreateButtonClick}>New Room</Button>
            <Button startDecorator={<PlayArrow/>} onClick={() => onJoinButtonClick(selectedGameId)} disabled={selectedGameId === ''}>Join</Button>
        </Stack>
    )
}

function RoomsPage({handlers, sendRequest, onBackButtonClick, onJoinButtonClick, onCreateButtonClick}) {
    const [selectedGameId, setSelectedGameId] = useState('');
    const [rooms, setRooms] = useState([]);

    // TODO: save event handlers if ws socket loss occurs and add all listeners on ws socket open
    useEffect(() => {
        sendRequest('rooms', {});
        handlers['rooms'] = handleRooms;
        console.log('added handleRooms');
    }, []);

    const onRefreshButtonClick = () => {
        sendRequest('rooms', {});
    }

    const handleRooms = (response) => {
        setRooms(response.rooms);
        console.log(`Rooms: ${response.rooms}`);
    }

    return (
        <Stack
            direction="column"
            justifyContent="center"
            alignItems="center"
            spacing={2}
        >
            <TopBar onBackButtonClick={onBackButtonClick} onRefreshButtonClick={onRefreshButtonClick}/>
            <Rooms rooms={rooms} selectedGameId={selectedGameId} updateSelectedGameId={setSelectedGameId}/>
            <Actions selectedGameId={selectedGameId} onJoinButtonClick={onJoinButtonClick} onCreateButtonClick={onCreateButtonClick}/>
        </Stack>
    )
}

export default RoomsPage;