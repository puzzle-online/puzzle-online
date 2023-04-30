import React, {useState} from "react";
import {Button, Input, Stack, Typography} from "@mui/joy";
import reactLogo from "../../assets/teamwork.png";
import Person from "@mui/icons-material/Person";
import PlayArrow from "@mui/icons-material/PlayArrow";
import Home from "@mui/icons-material/Home";

function HomePage({onRoomsButtonClick, nickname, setNickname}) {
    return (
        <div>
            <a href="https://reactjs.org" target="_blank">
                <img src={reactLogo} className="logo react" alt="React logo"/>
            </a>
            <Stack spacing={2}>
                <Stack
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    spacing={2}
                >
                    <Person/>
                    <Typography color="textPrimary">
                        NICKNAME
                    </Typography>
                    <Input
                        label="Nickname"
                        variant="soft"
                        placeholder="Enter your nickname"
                        value={nickname}
                        onChange={e => setNickname(e.target.value)}
                    />
                </Stack>
                <Stack
                    direction="row"
                    justifyContent="space-between"
                    alignItems="center"
                    spacing={2}
                >
                    <Button variant="solid" sx={{width: "100%"}} onClick={onRoomsButtonClick} disabled={!nickname.trim()}>
                        <Home/>
                        <Typography color="textPrimary" sx={{width: "100%"}}>
                            ROOMS
                        </Typography>
                    </Button>
                    <Button variant="solid" sx={{width: "100%"}} disabled>
                        <PlayArrow/>
                        <Typography color="textPrimary" sx={{width: "100%"}}>
                            PLAY
                        </Typography>
                    </Button>
                </Stack>
            </Stack>
        </div>
    );
}

export default HomePage;
