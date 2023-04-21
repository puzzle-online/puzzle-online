import React from "react";
import ArrowBack from "@mui/icons-material/ArrowBack.js";
import IconButton from "@mui/joy/IconButton";

function RoomPage({onBackButtonClick}) {
    return (
        <>
            <IconButton onClick={onBackButtonClick}>
                <ArrowBack/>
            </IconButton>
        </>
    )
}


export default RoomPage;