import React, {Fragment} from "react";

import PlayArrow from "@mui/icons-material/PlayArrow";
import Home from "@mui/icons-material/Home";
import Search from "@mui/icons-material/Search";
import ArrowBack from "@mui/icons-material/ArrowBack";

import Stack from "@mui/joy/Stack";
import Button from "@mui/joy/Button";
import Input from "@mui/joy/Input";
import Typography from "@mui/joy/Typography";
import Select from "@mui/joy/Select";
import Option from "@mui/joy/Option";
import Card from "@mui/joy/Card";
import CardOverflow from "@mui/joy/CardOverflow";
import IconButton from "@mui/joy/IconButton";
import Grid from "@mui/joy/Grid";
import Sheet from "@mui/joy/Sheet";
import CardContent from "@mui/joy/CardContent";
// import CardMedia from '@mui/joy/CardMedia';
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
        // boxShadow: 'none'
        <Select placeholder="Difficulty">
            <Option>Easy</Option>
            <Option>Medium</Option>
            <Option>Hard</Option>
        </Select>
    );
}

function TopBar() {
    // return (
    //     <Stack
    //         direction="row"
    //         justifyContent="space-between"
    //         alignItems="center"
    //         spacing={2}
    //         sx={{width: 1000}}
    //     >
    //         {/*    content: [[back-button] [searchbar]][ROOMS][difficulty]*/}
    //         <Stack
    //             direction="row"
    //             justifyContent="space-between"
    //             alignItems="center"
    //             spacing={2}
    //             sx={{ width: 200 }}
    //         >
    //             <IconButton>
    //                 <ArrowBack/>
    //             </IconButton>
    //             <Input
    //                 startDecorator={<Search/>}
    //                 placeholder="Search"
    //                 />
    //         </Stack>
    //         <PageTitle>
    //             ROOMS
    //         </PageTitle>
    //         <Options
    //             sx={{ width: 300}}
    //         />
    //     </Stack>
    // );
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
                    <IconButton>
                        <ArrowBack/>
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

function Rooms() {
    // const gridItems = Array.from({ length: 6 }).map(() => (
    //     <Grid item xs={12} sm={6} md={4} key={Math.random()}>
    //         <Card variant="solid">
    //             <CardOverflow>
    //                 <img src="https://thecatapi.com/api/images/get?format=src&type=gif" alt="A random cat" />
    //             </CardOverflow>
    //         </Card>
    //     </Grid>
    // ));

    return (
        <Grid
            container
            spacing={{ xs: 2, md: 2 }}
            columns={{ xs: 2, sm: 2, md: 12 }}
            sx={{ flexGrow: 1 }}
        >
            {Array.from(Array(6)).map((_, index) => (
                <Grid xs={2} sm={4} md={4} key={index}>
                    <Card variant="outlined" sx={{ width: 300 }}>
                        <AspectRatio>
                            <div>
                                <img src="https://thecatapi.com/api/images/get?format=src&type=gif" alt="A random cat" />
                            </div>
                        </AspectRatio>
                        <Typography mt={2}>Title</Typography>
                        <Typography level="body2">Description of the card.</Typography>
                    </Card>
                </Grid>
            ))}
        </Grid>
    );
    // return (
    //     <Grid container spacing={3} style={{ backgroundColor: 'gray' }} direction="row" justifyContent="space-between" alignItems="center">
    //         <Grid xs={12}>
    //             <Card
    //                 variant="solid"
    //             >
    //                 <CardOverflow>
    //                     <img id="photo" src="https://thecatapi.com/api/images/get?format=src&type=gif" alt=""/>
    //                 </CardOverflow>
    //             </Card>
    //         </Grid>
    //         <Grid xs={12}>
    //             <Card
    //                 variant="solid"
    //             >
    //                 <CardOverflow>
    //                     <img id="photo" src="https://thecatapi.com/api/images/get?format=src&type=gif" alt=""/>
    //                 </CardOverflow>
    //             </Card>
    //         </Grid>
    //     </Grid>
    // );
    // return (
    //     <Sheet color="neutral" variant="soft" sx={{ width: 1000, height: 500 }}>
    //         <Card
    //             variant="solid"
    //         >
    //             <CardOverflow>
    //                 <img id="photo" src="https://thecatapi.com/api/images/get?format=src&type=gif" alt=""/>
    //             </CardOverflow>
    //         </Card>
    //         <Card
    //             variant="solid"
    //         >
    //             <CardOverflow>
    //                 <img id="photo" src="https://thecatapi.com/api/images/get?format=src&type=gif" alt=""/>
    //             </CardOverflow>
    //         </Card>
    //     </Sheet>
    // );
}

function Actions() {
    return (
        <Stack
            direction="row"
            justifyContent="center"
            alignItems="center"
            spacing={2}
        >
            <Button startDecorator={<Home/>}>New Room</Button>
            <Button startDecorator={<PlayArrow/>}>Play</Button>
        </Stack>
    )
}

function RoomPage() {
    return (

        <Stack
            direction="column"
            justifyContent="center"
            alignItems="center"
            spacing={2}
        >
            <TopBar/>
            <Rooms/>
            <Actions/>
        </Stack>
    )
}

export default RoomPage;